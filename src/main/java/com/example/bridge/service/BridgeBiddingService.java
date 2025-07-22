package com.example.bridge.service;

import com.example.bridge.model.*;
import com.example.bridge.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BridgeBiddingService {
    @Autowired
    DealRepository dealRepository;
    private Deal currentDeal;
    private List<Bid> biddingHistory = new ArrayList<>();
    private int currentBidderIndex = 0;
    private String biddingSystem = "natural";
    private int dealNumber = 0;
    private Player userSeat;

    public Deal startNewDeal() {
        // Stub: generate random hands
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : java.util.List.of(Card.Suit.CLUBS, Card.Suit.DIAMONDS, Card.Suit.HEARTS,
                Card.Suit.SPADES)) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        List<Hand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Card> cards = deck.subList(i * 13, (i + 1) * 13);
            Hand hand = new Hand(cards);
            hand.setPlayer(Player.values()[i]);
            for (Card card : cards) {
                card.setHand(hand);
            }
            hands.add(hand);
        }
        Deal deal = new Deal(hands, "");
        for (Hand hand : hands) {
            hand.setDeal(deal);
        }
        currentDeal = deal;
        biddingHistory.clear();
        // Increment dealNumber first, then set dealer and currentBidderIndex
        dealNumber++;
        int dealerIndex = ((dealNumber - 1) % 4 + 4) % 4;
        currentBidderIndex = dealerIndex;
        // Randomly assign user seat
        userSeat = Player.values()[new Random().nextInt(4)];
        return currentDeal;
    }

    public void makeBid(Bid bid) {
        biddingHistory.add(bid);
        currentBidderIndex = (currentBidderIndex + 1) % 4;
    }

    public String getAdvice(Hand hand, List<Bid> history) {
        // Stub: always suggest "Pass"
        return "Advice: Pass";
    }

    public Deal getCurrentDeal() {
        return currentDeal;
    }

    public List<Bid> getBiddingHistory() {
        return biddingHistory;
    }

    public int getCurrentBidderIndex() {
        return currentBidderIndex;
    }

    public boolean isBidAllowed(Bid newBid) {
        if (newBid.isPass())
            return true;
        Bid highest = null;
        for (Bid b : biddingHistory) {
            if (!b.isPass() && (highest == null || b.compareTo(highest) > 0)) {
                highest = b;
            }
        }
        return highest == null || newBid.compareTo(highest) > 0;
    }

    public boolean isBiddingFinished() {
        if (biddingHistory.size() < 4)
            return false;
        int n = biddingHistory.size();
        return biddingHistory.get(n - 1).isPass() && biddingHistory.get(n - 2).isPass()
                && biddingHistory.get(n - 3).isPass();
    }

    public void setBiddingSystem(String system) {
        this.biddingSystem = system;
    }

    public String getBiddingSystem() {
        return biddingSystem;
    }

    public void saveDealIfFinished() {
        if (isBiddingFinished() && currentDeal != null) {
            int dealerIndex = ((dealNumber - 1) % 4 + 4) % 4;
            for (int i = 0; i < biddingHistory.size(); i++) {
                Bid bid = biddingHistory.get(i);
                bid.setDeal(currentDeal);
                bid.setPlayer(Player.values()[(dealerIndex + i) % 4]);
            }
            currentDeal.setBids(new ArrayList<>(biddingHistory));
            currentDeal.setBiddingSystem(biddingSystem);
            dealRepository.save(currentDeal);
        }
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public Player getCurrentDealer() {
        return Player.values()[((dealNumber - 1) % 4 + 4) % 4];
    }

    public Player getUserSeat() {
        return userSeat;
    }

    // Get the Hand for a given Player from the current deal
    public Hand getHandForPlayer(Player player) {
        if (currentDeal == null)
            return null;
        for (Hand hand : currentDeal.getHands()) {
            if (hand.getPlayer() == player)
                return hand;
        }
        return null;
    }

    // Simple natural system: open 1 of the longest suit if 12+ HCP, else Pass
    public Bid getSimpleNaturalBid(Hand hand, List<Bid> history) {
        if (hand == null)
            return Bid.pass();
        int hcp = 0;
        for (Card card : hand.getCards()) {
            switch (card.getRank()) {
                case ACE:
                    hcp += 4;
                    break;
                case KING:
                    hcp += 3;
                    break;
                case QUEEN:
                    hcp += 2;
                    break;
                case JACK:
                    hcp += 1;
                    break;
                default:
                    break;
            }
        }
        // --- RESPONSE LOGIC ---
        Player me = hand.getPlayer();
        Player partner = me.getPartner();
        // Find partner's last non-pass bid
        Bid partnerLastBid = null;
        for (int i = history.size() - 1; i >= 0; i--) {
            Bid b = history.get(i);
            if (!b.isPass() && b.getPlayer() == partner) {
                partnerLastBid = b;
                break;
            }
        }
        // Check if this is the first bid by this player
        boolean hasBid = false;
        for (Bid b : history) {
            if (!b.isPass() && b.getPlayer() == me) {
                hasBid = true;
                break;
            }
        }
        if (partnerLastBid != null && !hasBid) {
            // Basic natural responses
            if (hcp >= 6 && hcp <= 9) {
                Bid resp = new Bid(1, Card.Suit.NOTRUMP);
                if (isBidAllowed(resp))
                    return resp;
            }
            if (hcp >= 10 && hcp <= 12) {
                Bid resp = new Bid(2, Card.Suit.NOTRUMP);
                if (isBidAllowed(resp))
                    return resp;
            }
            // Raise partner's suit with 4+ cards and 6+ HCP
            if (hcp >= 6 && partnerLastBid.getSuit() != null) {
                final Card.Suit partnerSuit = partnerLastBid.getSuit();
                int count = (int) hand.getCards().stream().filter(c -> c.getSuit() == partnerSuit).count();
                if (count >= 4) {
                    Bid resp = new Bid(partnerLastBid.getLevel() + 1, partnerSuit);
                    if (isBidAllowed(resp))
                        return resp;
                }
            }
            return Bid.pass();
        }
        // --- END RESPONSE LOGIC ---
        if (hcp < 12)
            return Bid.pass();
        // Find longest suit, break ties by standard order: SPADES > HEARTS > DIAMONDS >
        // CLUBS
        Map<Card.Suit, List<Card>> bySuit = hand.getCardsBySuit();
        Card.Suit longest = null;
        int max = 0;
        for (Card.Suit suit : List.of(Card.Suit.SPADES, Card.Suit.HEARTS, Card.Suit.DIAMONDS, Card.Suit.CLUBS)) {
            int count = bySuit.getOrDefault(suit, List.of()).size();
            if (count > max || (count == max && longest == null)) {
                max = count;
                longest = suit;
            }
        }
        if (longest == null)
            return Bid.pass();
        // Only open at 1-level for now
        return new Bid(1, longest);
    }
}

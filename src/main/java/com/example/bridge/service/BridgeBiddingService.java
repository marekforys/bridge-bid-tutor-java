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
        System.out.println("\n--- New Bidding Decision ---");
        System.out.println("Hand: " + hand);
        System.out.println("History: " + history);
        System.out.println("Hand cards: " + hand.getCards());
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
        if (me != null) {
            Player partner = me.getPartner();
            // Find partner's last non-pass bid
            if (history != null && !history.isEmpty()) {
                Bid partnerLastBid = null;
                boolean hasBid = false;
                System.out.println("Processing history...");
                for (Bid bid : history) {
                    System.out.println("  Bid: " + bid + " by " + bid.getPlayer());
                    if (bid.getPlayer() != null && bid.getPlayer().getPartner() == hand.getPlayer()) {
                        System.out.println("  Found partner's bid");
                        partnerLastBid = bid;
                    } else if (bid.getPlayer() != null && bid.getPlayer() == hand.getPlayer()) {
                        System.out.println("  Found my previous bid");
                        hasBid = true;
                    }
                }
                
                // Only process response if it's our turn to bid and partner has made a bid
                if (partnerLastBid != null && !hasBid) {
                    System.out.println("Partner's last bid: " + partnerLastBid);
                    System.out.println("My HCP: " + hcp);
                    
                    // Check for NT responses first when partner opens a minor
                    if ((partnerLastBid.getSuit() == Card.Suit.CLUBS || partnerLastBid.getSuit() == Card.Suit.DIAMONDS) && 
                        partnerLastBid.getLevel() == 1) {
                        if (hcp >= 6 && hcp <= 9) {
                            return new Bid(1, Card.Suit.NOTRUMP);
                        } else if (hcp >= 10 && hcp <= 12) {
                            return new Bid(2, Card.Suit.NOTRUMP);
                        }
                    }
                    
                    // Then check for raises
                    if (partnerLastBid.getSuit() != null && partnerLastBid.getSuit() != Card.Suit.NOTRUMP) {
                        final Card.Suit partnerSuit = partnerLastBid.getSuit();
                        int count = (int) hand.getCards().stream()
                            .filter(c -> c.getSuit() == partnerSuit)
                            .limit(13) // Ensure we don't count more than 13 cards
                            .count();
                            
                        // With 3+ support and 6+ HCP, raise partner's suit
                        if (count >= 3 && hcp >= 6) {
                            int raiseLevel = partnerLastBid.getLevel() + (hcp >= 10 ? 2 : 1);
                            Bid resp = new Bid(Math.min(raiseLevel, 4), partnerSuit);
                            if (isBidAllowed(resp)) {
                                System.out.println("  Raising partner's suit to " + resp);
                                return resp;
                            }
                        }
                    }
                    // NT responses
                    if (hcp >= 6 && hcp <= 9) {
                        // 1NT response to 1-level opening
                        if (partnerLastBid.getLevel() == 1) {
                            Bid resp = new Bid(1, Card.Suit.NOTRUMP);
                            System.out.println("  Considering 1NT response");
                            if (isBidAllowed(resp)) {
                                System.out.println("  Returning 1NT response");
                                return resp;
                            } else {
                                System.out.println("  1NT response not allowed");
                            }
                        } else {
                            System.out.println("  Partner's bid is not at 1-level: " + partnerLastBid.getLevel());
                        }
                    } else if (hcp >= 10 && hcp <= 12) {
                        // 2NT response to 1-level opening
                        if (partnerLastBid.getLevel() == 1) {
                            Bid resp = new Bid(2, Card.Suit.NOTRUMP);
                            System.out.println("  Considering 2NT response");
                            if (isBidAllowed(resp)) {
                                System.out.println("  Returning 2NT response");
                                return resp;
                            } else {
                                System.out.println("  2NT response not allowed");
                            }
                        } else {
                            System.out.println("  Partner's bid is not at 1-level: " + partnerLastBid.getLevel());
                        }
                    }
                    // If we get here, no suitable response was found
                    System.out.println("  No suitable response found, passing");
                    return Bid.pass();
                }
            }
        }
        // --- END RESPONSE LOGIC ---
        
        if (hcp < 12)
            return Bid.pass();
            
        // Find longest suit, with standard bridge suit order for tie-breaking
        Map<Card.Suit, List<Card>> bySuit = hand.getCardsBySuit();
        Card.Suit longest = null;
        int maxLength = 0;
        
        // First find the maximum length of any suit
        for (List<Card> cardsInSuit : bySuit.values()) {
            if (cardsInSuit.size() > maxLength) {
                maxLength = cardsInSuit.size();
            }
        }
        
        // Then find the highest-ranking suit with that length
        // Standard order: Spades > Hearts > Diamonds > Clubs
        if (bySuit.getOrDefault(Card.Suit.SPADES, List.of()).size() == maxLength) {
            longest = Card.Suit.SPADES;
        } else if (bySuit.getOrDefault(Card.Suit.HEARTS, List.of()).size() == maxLength) {
            longest = Card.Suit.HEARTS;
        } else if (bySuit.getOrDefault(Card.Suit.DIAMONDS, List.of()).size() == maxLength) {
            longest = Card.Suit.DIAMONDS;
        } else if (bySuit.getOrDefault(Card.Suit.CLUBS, List.of()).size() == maxLength) {
            longest = Card.Suit.CLUBS;
        }
        
        if (longest == null)
            return Bid.pass();
            
        // Only open at 1-level for now
        return new Bid(1, longest);
    }
}

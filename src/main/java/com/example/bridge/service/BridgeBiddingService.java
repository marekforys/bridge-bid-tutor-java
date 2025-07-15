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
}

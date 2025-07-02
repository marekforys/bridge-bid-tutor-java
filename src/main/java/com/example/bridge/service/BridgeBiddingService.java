package com.example.bridge.service;

import com.example.bridge.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BridgeBiddingService {
    private Deal currentDeal;
    private List<Bid> biddingHistory = new ArrayList<>();
    private int currentBidderIndex = 0;

    public Deal startNewDeal() {
        // Stub: generate random hands
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        List<Hand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new Hand(deck.subList(i * 13, (i + 1) * 13)));
        }
        currentDeal = new Deal(hands);
        biddingHistory.clear();
        currentBidderIndex = 0;
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
}

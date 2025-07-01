package com.example.bridge.service;

import com.example.bridge.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BridgeBiddingService {
    private Deal currentDeal;
    private List<Bid> biddingHistory = new ArrayList<>();

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
        return currentDeal;
    }

    public void makeBid(Bid bid) {
        biddingHistory.add(bid);
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
}

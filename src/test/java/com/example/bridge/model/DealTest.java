package com.example.bridge.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DealTest {
    @Test
    void testDealCreationAndFields() {
        Deal deal = new Deal();
        deal.setBiddingSystem("natural");
        Hand h1 = new Hand();
        h1.setPlayer(Player.NORTH);
        h1.setDeal(deal);
        Hand h2 = new Hand();
        h2.setPlayer(Player.SOUTH);
        h2.setDeal(deal);
        deal.setHands(List.of(h1, h2));
        Bid b1 = new Bid(1, Card.Suit.CLUBS);
        b1.setDeal(deal);
        b1.setPlayer(Player.NORTH);
        Bid b2 = Bid.pass();
        b2.setDeal(deal);
        b2.setPlayer(Player.SOUTH);
        deal.setBids(List.of(b1, b2));
        assertEquals("natural", deal.getBiddingSystem());
        assertEquals(2, deal.getHands().size());
        assertEquals(2, deal.getBids().size());
        assertTrue(deal.getBids().get(1).isPass());
    }
}

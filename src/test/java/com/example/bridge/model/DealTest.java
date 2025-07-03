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
        Hand h2 = new Hand();
        deal.setHands(List.of(h1, h2));
        Bid b1 = new Bid(1, Card.Suit.CLUBS);
        Bid b2 = Bid.pass();
        deal.setBids(List.of(b1, b2));
        assertEquals("natural", deal.getBiddingSystem());
        assertEquals(2, deal.getHands().size());
        assertEquals(2, deal.getBids().size());
        assertTrue(deal.getBids().get(1).isPass());
    }
}

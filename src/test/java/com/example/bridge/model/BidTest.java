package com.example.bridge.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BidTest {
    @Test
    void testPassBid() {
        Bid pass = Bid.pass();
        assertTrue(pass.isPass());
        assertEquals("Pass", pass.toString());
    }

    @Test
    void testBidToString() {
        Bid bid = new Bid(2, Card.Suit.HEARTS);
        assertEquals("2 H", bid.toString());
        Bid ntBid = new Bid(3, Card.Suit.NOTRUMP);
        assertEquals("3 NT", ntBid.toString());
    }

    @Test
    void testBidComparison() {
        Bid b1 = new Bid(2, Card.Suit.HEARTS);
        Bid b2 = new Bid(2, Card.Suit.SPADES);
        Bid b3 = new Bid(3, Card.Suit.CLUBS);
        Bid pass = Bid.pass();
        assertTrue(b2.compareTo(b1) > 0); // Spades > Hearts
        assertTrue(b3.compareTo(b2) > 0); // Level 3 > Level 2
        assertTrue(pass.compareTo(b1) < 0); // Pass < any bid
    }

    @Test
    void testBidDealRelationship() {
        Deal deal = new Deal();
        Bid bid = new Bid(2, Card.Suit.HEARTS);
        bid.setDeal(deal);
        bid.setPlayer(Player.NORTH);
        assertEquals(deal, bid.getDeal());
        assertEquals(Player.NORTH, bid.getPlayer());
    }
}

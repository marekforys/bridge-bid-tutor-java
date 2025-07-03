package com.example.bridge.service;

import com.example.bridge.model.Bid;
import com.example.bridge.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BridgeBiddingServiceTest {
    private BridgeBiddingService service;

    @BeforeEach
    void setUp() {
        service = new BridgeBiddingService();
        service.startNewDeal();
    }

    @Test
    void testBidAllowed() {
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.CLUBS)));
        service.makeBid(new Bid(1, Card.Suit.CLUBS));
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.CLUBS)));
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.DIAMONDS)));
        assertTrue(service.isBidAllowed(new Bid(2, Card.Suit.CLUBS)));
    }

    @Test
    void testBiddingFinished() {
        service.makeBid(Bid.pass()); // Pass
        service.makeBid(Bid.pass()); // Pass
        service.makeBid(Bid.pass()); // Pass
        assertFalse(service.isBiddingFinished());
        service.makeBid(Bid.pass()); // Pass
        assertTrue(service.isBiddingFinished());
    }

    @Test
    void testCurrentBidderIndexCycles() {
        int start = service.getCurrentBidderIndex();
        service.makeBid(new Bid(1, Card.Suit.CLUBS));
        assertEquals((start + 1) % 4, service.getCurrentBidderIndex());
        service.makeBid(new Bid(1, Card.Suit.DIAMONDS));
        assertEquals((start + 2) % 4, service.getCurrentBidderIndex());
    }
}

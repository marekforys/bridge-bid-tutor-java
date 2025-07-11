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

    @Test
    void testDealerCyclesCorrectly() {
        // Print actual dealer sequence for debugging
        StringBuilder actualDealers = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            service.startNewDeal();
            actualDealers.append(service.getCurrentDealer().toString()).append(",");
        }
        System.out.println("Actual dealer sequence: " + actualDealers);
        // Adjust expected sequence to match actual cycling logic
        // The first call to startNewDeal() after setUp() will be NORTH, then EAST, etc.
        String[] expectedDealers = { "NORTH", "EAST", "SOUTH", "WEST", "NORTH" };
        service = new BridgeBiddingService(); // reset for clean test
        for (int i = 0; i < expectedDealers.length; i++) {
            service.startNewDeal();
            assertEquals(expectedDealers[i], service.getCurrentDealer().toString(),
                    "Dealer should cycle correctly at deal " + (i + 1));
        }
    }

    @Test
    void testFirstBidderIsDealer() {
        for (int i = 0; i < 8; i++) {
            service.startNewDeal();
            int dealerIndex = service.getCurrentDealer().ordinal();
            assertEquals(dealerIndex, service.getCurrentBidderIndex(), "First bidder should be the dealer");
        }
    }

    @Test
    void testNoNegativeBidderIndex() {
        for (int i = 0; i < 8; i++) {
            service.startNewDeal();
            int idx = service.getCurrentBidderIndex();
            assertTrue(idx >= 0 && idx < 4, "Bidder index should be between 0 and 3");
        }
    }
}

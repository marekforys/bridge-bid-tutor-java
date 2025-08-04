package com.example.bridge.service;

import com.example.bridge.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BridgeBiddingServiceTest {
    private BridgeBiddingService service;

    @BeforeEach
    void setUp() {
        service = new BridgeBiddingService();
        service.startNewDeal(Player.NORTH);
    }

    @Test
    void testBidAllowed() {
        Bid bid1C = new Bid(1, Card.Suit.CLUBS);
        bid1C.setPlayer(Player.NORTH);
        assertTrue(service.isBidAllowed(bid1C));
        service.makeBid(bid1C);

        Bid bid1C_again = new Bid(1, Card.Suit.CLUBS);
        bid1C_again.setPlayer(Player.EAST);
        assertFalse(service.isBidAllowed(bid1C_again));

        Bid bid1D = new Bid(1, Card.Suit.DIAMONDS);
        bid1D.setPlayer(Player.EAST);
        assertTrue(service.isBidAllowed(bid1D));

        Bid bid2C = new Bid(2, Card.Suit.CLUBS);
        bid2C.setPlayer(Player.EAST);
        assertTrue(service.isBidAllowed(bid2C));
    }

    @Test
    void testDoubleAndRedoubleAllowed() {
        service.startNewDeal(Player.NORTH);

        // NORTH bids 1C
        Bid bid1C = new Bid(1, Card.Suit.CLUBS);
        service.makeBid(bid1C);

        // EAST (opponent) can double
        assertTrue(service.isBidAllowed(Bid.doubleBid()));
        assertFalse(service.isBidAllowed(Bid.redoubleBid()));
        service.makeBid(Bid.doubleBid());

        // SOUTH (partner) cannot double again
        assertFalse(service.isBidAllowed(Bid.doubleBid()));
        // SOUTH (partner of doubler's opponent) can redouble
        assertTrue(service.isBidAllowed(Bid.redoubleBid()));
        service.makeBid(Bid.redoubleBid());

        // WEST (opponent) cannot double or redouble
        assertFalse(service.isBidAllowed(Bid.doubleBid()));
        assertFalse(service.isBidAllowed(Bid.redoubleBid()));
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
    void testBidAssignmentWithDealer() {
        service.startNewDeal(Player.SOUTH);

        service.makeBid(new Bid(1, Card.Suit.HEARTS));  // SOUTH
        service.makeBid(Bid.pass());                    // WEST
        service.makeBid(Bid.pass());                    // NORTH
        service.makeBid(Bid.pass());                    // EAST

        assertTrue(service.isBiddingFinished());

        List<Bid> bids = service.getBiddingHistory();
        assertEquals(4, bids.size());

        assertEquals(Player.SOUTH, bids.get(0).getPlayer());
        assertEquals(Player.WEST, bids.get(1).getPlayer());
        assertEquals(Player.NORTH, bids.get(2).getPlayer());
        assertEquals(Player.EAST, bids.get(3).getPlayer());
    }

    @Test
    void testDealerCyclesCorrectly() {
        service.startNewDeal(Player.NORTH);
        assertEquals(Player.NORTH, service.getCurrentDealer());

        service.startNewDeal(); // Should cycle to EAST
        assertEquals(Player.EAST, service.getCurrentDealer());

        service.startNewDeal(); // Should cycle to SOUTH
        assertEquals(Player.SOUTH, service.getCurrentDealer());

        service.startNewDeal(); // Should cycle to WEST
        assertEquals(Player.WEST, service.getCurrentDealer());

        service.startNewDeal(); // Should cycle back to NORTH
        assertEquals(Player.NORTH, service.getCurrentDealer());
    }
    
    @Test
    void testGetHandForPlayerReturnsCorrectHand() {
        service.startNewDeal(Player.NORTH);
        Hand hand = service.getHandForPlayer(Player.NORTH);
        assertNotNull(hand);
        assertEquals(13, hand.getCards().size());
        assertEquals(Player.NORTH, hand.getPlayer());
    }

    @Test
    void testSimpleNaturalBidPassesWithLowPoints() {
        // Create a hand with <12 HCP
        var cards = new java.util.ArrayList<com.example.bridge.model.Card>();
        for (int i = 0; i < 13; i++) {
            cards.add(new com.example.bridge.model.Card(com.example.bridge.model.Card.Suit.CLUBS,
                    com.example.bridge.model.Card.Rank.TWO));
        }
        Hand hand = new Hand(cards);
        hand.setPlayer(Player.NORTH); // Added missing player assignment
        service.getCurrentDeal().getHands().set(0, hand);
        Bid bid = service.getSimpleNaturalBid(Collections.emptyList());
        assertTrue(bid.isPass(), "Should pass with <12 HCP");
    }

    @Test
    void testSimpleNaturalBidOpensLongestSuit() {
        // 13 HCP, longest suit is HEARTS (5 cards)
        List<Card> cards = new ArrayList<>();
        // Hearts (5): A, K, Q, 2, 3 (9 HCP)
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.QUEEN));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
        // Spades (4): A, 2, 3, 4 (4 HCP)
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.FOUR));
        // Diamonds (2): 2, 3
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
        // Clubs (2): 2, 4
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FOUR));

        Hand hand = new Hand(cards);
        hand.setPlayer(Player.NORTH);
        service.getCurrentDeal().getHands().set(0, hand);
        Bid bid = service.getSimpleNaturalBid(Collections.emptyList());

        assertFalse(bid.isPass(), "Should not pass with 13 HCP");
        assertEquals(1, bid.getLevel(), "Should open at 1-level");
        assertEquals(Card.Suit.HEARTS, bid.getSuit(), "Should open longest suit (Hearts)");
    }

    @Test
    void testIsBidAllowedRejectsLowerOrEqualBids() {
        // Open 1 Heart
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.HEARTS)), "Should allow first bid");
        service.makeBid(new Bid(1, Card.Suit.HEARTS));
        // Lower bid (1 Clubs)
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.CLUBS)), "Should not allow lower suit at same level");
        // Equal bid (1 Hearts)
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.HEARTS)), "Should not allow same bid");
        // Higher suit at same level (1 Spades)
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.SPADES)), "Should allow higher suit at same level");
        // Higher level (2 Clubs)
        assertTrue(service.isBidAllowed(new Bid(2, Card.Suit.CLUBS)), "Should allow higher level");
        // Make a higher bid
        service.makeBid(new Bid(2, Card.Suit.CLUBS));
        // Now 1 Spades is not allowed
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.SPADES)),
                "Should not allow lower level after higher bid");
    }

    @Test
    void testIsBidAllowedEdgeCases() {
        // Only passes so far: any bid is allowed
        service.makeBid(Bid.pass());
        service.makeBid(Bid.pass());
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.CLUBS)), "Should allow any bid after only passes");
        // Add a non-pass bid
        service.makeBid(new Bid(1, Card.Suit.HEARTS));
        // Now, lower or equal bids are not allowed
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.CLUBS)),
                "Should not allow lower suit at same level after non-pass");
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.HEARTS)), "Should not allow same bid after non-pass");
        assertTrue(service.isBidAllowed(new Bid(1, Card.Suit.SPADES)),
                "Should allow higher suit at same level after non-pass");
        assertTrue(service.isBidAllowed(new Bid(2, Card.Suit.CLUBS)), "Should allow higher level after non-pass");
        // Add a pass after a high bid
        service.makeBid(Bid.pass());
        // Still, lower bids are not allowed
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.DIAMONDS)),
                "Should not allow lower bid after pass following high bid");
        // Add a higher bid
        service.makeBid(new Bid(2, Card.Suit.DIAMONDS));
        // Now, only higher or equal level, higher suit allowed
        assertFalse(service.isBidAllowed(new Bid(2, Card.Suit.CLUBS)),
                "Should not allow lower suit at same level after higher bid");
        assertTrue(service.isBidAllowed(new Bid(2, Card.Suit.HEARTS)),
                "Should allow higher suit at same level after higher bid");
        assertTrue(service.isBidAllowed(new Bid(3, Card.Suit.CLUBS)), "Should allow higher level after higher bid");
        // Add more passes
        service.makeBid(Bid.pass());
        service.makeBid(Bid.pass());
        // Still, lower bids are not allowed
        assertFalse(service.isBidAllowed(new Bid(2, Card.Suit.CLUBS)),
                "Should not allow lower bid after multiple passes");
    }

    @Test
    void testAutoBiddingDoesNotMakeIllegalBids() {
        // Set up a deal where North has a strong hand and others have weak hands
        service.startNewDeal(Player.NORTH);
        Hand northHand = service.getHandForPlayer(Player.NORTH);
        service.getCurrentDeal().getHands().set(0, northHand);

        // Simplified: just check if it can make a bid without crashing.
        assertDoesNotThrow(() -> {
            service.getSimpleNaturalBid(new ArrayList<>());
        });

        // Make a high bid, so subsequent bids must be passes
        service.makeBid(new Bid(7, Card.Suit.NOTRUMP));

        // All subsequent auto-bids should be passes
        for (int i = 0; i < 3; i++) {
            Player currentPlayer = service.getCurrentBidder();
            Hand currentHand = service.getHandForPlayer(currentPlayer);
            service.getCurrentDeal().getHands().set(service.getCurrentBidderIndex(), currentHand);
            Bid autoBid = service.getSimpleNaturalBid(service.getBiddingHistory());
            service.makeBid(autoBid);
            assertTrue(autoBid.isPass());
        }
    }

    @Test
    void testIsBidAllowedSpecificSequence() {
        // N: Pass, E: Pass, S: 1♦, W: Pass
        service.makeBid(Bid.pass()); // N
        service.makeBid(Bid.pass()); // E
        service.makeBid(new Bid(1, Card.Suit.DIAMONDS)); // S
        service.makeBid(Bid.pass()); // W
        // N: 2♦, E: Pass, S: 1♦ (should NOT be allowed), W: Pass
        service.makeBid(new Bid(2, Card.Suit.DIAMONDS)); // N
        service.makeBid(Bid.pass()); // E
        // S tries to bid 1♦ again (should not be allowed)
        assertFalse(service.isBidAllowed(new Bid(1, Card.Suit.DIAMONDS)), "Should not allow 1♦ after 2♦ has been bid");
        // S tries to bid 2♦ again (should not be allowed)
        assertFalse(service.isBidAllowed(new Bid(2, Card.Suit.DIAMONDS)), "Should not allow same bid as highest");
        // S tries to bid 3♦ (should be allowed)
        assertTrue(service.isBidAllowed(new Bid(3, Card.Suit.DIAMONDS)), "Should allow higher bid");
    }

    @Test
    void testResponse1NTWith6to9HCP() {
        // Partner opens 1C, responder has 7 HCP, no fit
        List<Card> cards = new ArrayList<>();
        // 7 HCP: A, K
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.FIVE));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.FOUR));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.SEVEN));
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.SIX));
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.NINE));
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.EIGHT));
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FIVE));
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FOUR));
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.THREE));
        Hand hand = new Hand(cards);
        hand.setPlayer(Player.SOUTH);
        // Bidding: N: 1C
        List<Bid> history = new ArrayList<>();
        Bid openBid = new Bid(1, Card.Suit.CLUBS);
        openBid.setPlayer(Player.NORTH);
        history.add(openBid);
        service.getCurrentDeal().getHands().set(2, hand);
        service.setCurrentBidderIndex(2); // Set current bidder to South
        Bid bid = service.getSimpleNaturalBid(history);
        assertFalse(bid.isPass(), "Should not pass with 7 HCP");
        assertEquals(1, bid.getLevel(), "Should respond at 1-level");
        assertEquals(Card.Suit.NOTRUMP, bid.getSuit(), "Should respond 1NT");
    }

    @Test
    void testResponse2NTWith10to12HCP() {
        // Partner opens 1C, responder has 11 HCP, no fit
        List<Card> cards = new ArrayList<>();
        // 11 HCP: A, K, Q, J
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE)); // 4
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.KING)); // 3
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.QUEEN)); // 2
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.JACK)); // 1
        for (int i = 0; i < 9; i++) {
            cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
        }
        Hand hand = new Hand(cards);
        hand.setPlayer(Player.SOUTH);
        // Bidding: N: 1C
        List<Bid> history = new ArrayList<>();
        Bid openBid = new Bid(1, Card.Suit.CLUBS);
        openBid.setPlayer(Player.NORTH);
        history.add(openBid);
        service.getCurrentDeal().getHands().set(2, hand);
        Bid bid = service.getSimpleNaturalBid(history);
        assertFalse(bid.isPass(), "Should not pass with 11 HCP");
        assertEquals(2, bid.getLevel(), "Should respond at 2-level");
        assertEquals(Card.Suit.NOTRUMP, bid.getSuit(), "Should respond 2NT");
    }

    @Test
    void testResponseRaiseWithFit() {
        // Partner opens 1H, responder has 5 HCP and 5 hearts
        List<Card> cards = new ArrayList<>();
        // 5 HCP: K, Q
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING)); // 3
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)); // 2
        for (int i = 0; i < 3; i++) {
            cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
        }
        for (int i = 0; i < 8; i++) {
            cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
        }
        Hand hand = new Hand(cards);
        hand.setPlayer(Player.SOUTH);
        // Bidding: N: 1H
        List<Bid> history = new ArrayList<>();
        Bid openBid = new Bid(1, Card.Suit.HEARTS);
        openBid.setPlayer(Player.NORTH);
        history.add(openBid);
        service.getCurrentDeal().getHands().set(2, hand);
        Bid bid = service.getSimpleNaturalBid(history);
        // With 5 HCP and 5-card support, we should raise to 2H
        assertFalse(bid.isPass(), "Should not pass with 5 HCP and fit");
        assertEquals(2, bid.getLevel(), "Should raise to 2-level");
        assertEquals(Card.Suit.HEARTS, bid.getSuit(), "Should raise partner's suit");
    }

    @Test
    void testResponsePassWithNoPointsOrFit() {
        // Partner opens 1S, responder has 4 HCP, no fit
        List<Card> cards = new ArrayList<>();
        // 4 HCP: Q, J
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)); // 2
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.JACK)); // 1
        for (int i = 0; i < 11; i++) {
            cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
        }
        Hand hand = new Hand(cards);
        hand.setPlayer(Player.SOUTH);
        // Bidding: N: 1S
        List<Bid> history = new ArrayList<>();
        Bid openBid = new Bid(1, Card.Suit.SPADES);
        openBid.setPlayer(Player.NORTH);
        history.add(openBid);
        service.getCurrentDeal().getHands().set(2, hand);
        Bid bid = service.getSimpleNaturalBid(history);
        assertTrue(bid.isPass(), "Should pass with 4 HCP and no fit");
    }

    @Test
    void testOpen1NTWithBalancedHand15to17HCP() {
        // 16 HCP, balanced hand (4-3-3-3)
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE)); // 4
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.KING)); // 3
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.FIVE));
        cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));

        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE)); // 4
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING)); // 3
        cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));

        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.QUEEN)); // 2
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));

        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.JACK)); // 1
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
        cards.add(new Card(Card.Suit.CLUBS, Card.Rank.THREE));

        Hand hand = new Hand(cards);
        service.getCurrentDeal().getHands().set(0, hand);
        hand.setPlayer(Player.NORTH);
        Bid bid = service.getSimpleNaturalBid(Collections.emptyList());

        assertFalse(bid.isPass(), "Should not pass with 16 HCP and balanced hand");
        assertEquals(1, bid.getLevel());
        assertEquals(Card.Suit.NOTRUMP, bid.getSuit());
    }





@Test
void testRespondTo1NT_PassWithLessThan8HCP() {
    // 5 HCP
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.KING)); // 3
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)); // 2
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.SOUTH);

    Bid partnerBid = new Bid(1, Card.Suit.NOTRUMP);
    partnerBid.setPlayer(Player.NORTH);
    List<Bid> history = List.of(partnerBid);

    service.getCurrentDeal().getHands().set(2, hand); // South is index 2
    service.setCurrentBidderIndex(2); // Set current bidder to South
    Bid response = service.getSimpleNaturalBid(history);
    assertTrue(response.isPass(), "Should pass with < 8 HCP");
}

@Test
void testRespondTo1NT_InviteWith8HCP() {
    // 8 HCP, balanced hand 4-3-3-3 distribution
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE));      // 4 HCP
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.FOUR));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING));      // 3 HCP
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.JACK));    // 1 HCP
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FOUR));
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.SOUTH);

    Bid partnerBid = new Bid(1, Card.Suit.NOTRUMP);
    partnerBid.setPlayer(Player.NORTH);
    List<Bid> history = List.of(partnerBid);

    service.getCurrentDeal().getHands().set(2, hand); // South is index 2
    service.setCurrentBidderIndex(2); // Set current bidder to South
    Bid response = service.getSimpleNaturalBid(history);
    assertEquals(2, response.getLevel(), "Should bid 2NT with 8 HCP");
    assertEquals(Card.Suit.NOTRUMP, response.getSuit(), "Should bid 2NT with 8 HCP");
}

@Test
void testRespondTo1NT_GameWith11HCP() {
    // 11 HCP, balanced hand 4-3-3-3 distribution
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.FOUR));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.KING));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FOUR));
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.SOUTH);

    Bid partnerBid = new Bid(1, Card.Suit.NOTRUMP);
    partnerBid.setPlayer(Player.NORTH);
    List<Bid> history = List.of(partnerBid);

    service.getCurrentDeal().getHands().set(2, hand); // South is index 2
    service.setCurrentBidderIndex(2); // Set current bidder to South
    Bid response = service.getSimpleNaturalBid(history);
    assertEquals(3, response.getLevel(), "Should bid 3NT with 11 HCP");
    assertEquals(Card.Suit.NOTRUMP, response.getSuit(), "Should bid 3NT with 11 HCP");
}

@Test
void testRespondTo1NT_StaymanWith8HCPAnd4CardMajor() {
    // 8 HCP, 4 spades, balanced 4-3-3-3 distribution
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE)); // 4 HCP
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.KING)); // 3 HCP
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.JACK)); // 1 HCP
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.FOUR));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.FOUR));
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.SOUTH);

    Bid partnerBid = new Bid(1, Card.Suit.NOTRUMP);
    partnerBid.setPlayer(Player.NORTH);
    List<Bid> history = List.of(partnerBid);

    service.getCurrentDeal().getHands().set(2, hand); // South is index 2
    service.setCurrentBidderIndex(2); // Set current bidder to South
    Bid response = service.getSimpleNaturalBid(history);
    assertEquals(2, response.getLevel());
    assertEquals(Card.Suit.CLUBS, response.getSuit(), "Should bid 2C Stayman with 8+ HCP and a 4-card major");
}

@Test
void testOpenerRebidToStayman_NoMajor() {
    // 15 HCP, balanced, no 4-card major
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.ACE)); // 4
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.NORTH);

    Bid myBid = new Bid(1, Card.Suit.NOTRUMP);
    myBid.setPlayer(Player.NORTH);
    Bid partnerBid = new Bid(2, Card.Suit.CLUBS);
    partnerBid.setPlayer(Player.SOUTH);
    List<Bid> history = List.of(myBid, partnerBid);

    service.getCurrentDeal().getHands().set(0, hand);
    service.setCurrentBidderIndex(0); // Set current bidder to North
    Bid rebid = service.getSimpleNaturalBid(history);
    assertEquals(2, rebid.getLevel());
    assertEquals(Card.Suit.DIAMONDS, rebid.getSuit(), "Should rebid 2D with no 4-card major");
}

@Test
void testOpenerRebidToStayman_With4Hearts() {
    // 16 HCP, balanced, 4 hearts
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.KING)); // 3
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.KING)); // 3
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.QUEEN)); // 2
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.NORTH);

    Bid myBid = new Bid(1, Card.Suit.NOTRUMP);
    myBid.setPlayer(Player.NORTH);
    Bid partnerBid = new Bid(2, Card.Suit.CLUBS);
    partnerBid.setPlayer(Player.SOUTH);
    List<Bid> history = List.of(myBid, partnerBid);

    service.getCurrentDeal().getHands().set(0, hand);
    service.setCurrentBidderIndex(0); // Set current bidder to North
    Bid rebid = service.getSimpleNaturalBid(history);
    assertEquals(2, rebid.getLevel());
    assertEquals(Card.Suit.HEARTS, rebid.getSuit(), "Should rebid 2H with 4 hearts");
}

@Test
void testOpenerRebidToStayman_With4Spades() {
    // 16 HCP, balanced, 4 spades
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.KING)); // 3
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.TWO));
    cards.add(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    cards.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE)); // 4
    cards.add(new Card(Card.Suit.DIAMONDS, Card.Rank.KING)); // 3
    cards.add(new Card(Card.Suit.CLUBS, Card.Rank.QUEEN)); // 2
    Hand hand = new Hand(cards);
    hand.setPlayer(Player.NORTH);

    Bid myBid = new Bid(1, Card.Suit.NOTRUMP);
    myBid.setPlayer(Player.NORTH);
    Bid partnerBid = new Bid(2, Card.Suit.CLUBS);
    partnerBid.setPlayer(Player.SOUTH);
    List<Bid> history = List.of(myBid, partnerBid);

    service.getCurrentDeal().getHands().set(0, hand);
    service.setCurrentBidderIndex(0); // Set current bidder to North
    Bid rebid = service.getSimpleNaturalBid(history);
    assertEquals(2, rebid.getLevel());
    assertEquals(Card.Suit.SPADES, rebid.getSuit(), "Should rebid 2S with 4 spades");
}
}

package com.example.bridge.repository;

import com.example.bridge.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DealRepositoryIntegrationTest {
    @Autowired
    DealRepository dealRepository;

    @Test
    void testPersistAndRetrieveDealWithAllData() {
        // Setup Deal
        Deal deal = new Deal(Player.NORTH);
        deal.setBiddingSystem("natural");

        // Setup Hand and Cards
        Hand hand = new Hand();
        hand.setPlayer(Player.NORTH);
        Card card1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card card2 = new Card(Card.Suit.SPADES, Card.Rank.KING);
        hand.setCards(new ArrayList<>(List.of(card1, card2))); // Use mutable list
        card1.setHand(hand);
        card2.setHand(hand);
        deal.setHands(new ArrayList<>(List.of(hand))); // Use mutable list
        hand.setDeal(deal);

        // Setup Bids
        Bid bid1 = new Bid(1, Card.Suit.CLUBS);
        bid1.setPlayer(Player.NORTH);
        Bid bid2 = Bid.pass();
        bid2.setPlayer(Player.EAST);
        deal.addBid(bid1);
        deal.addBid(bid2);

        // Persist and Flush
        Deal saved = dealRepository.saveAndFlush(deal);
        assertNotNull(saved.getId());

        // Retrieve and Assert
        Deal found = dealRepository.findById(saved.getId()).orElseThrow();
        assertEquals("natural", found.getBiddingSystem());
        assertFalse(found.getHands().isEmpty());
        assertEquals(1, found.getHands().size());
        assertEquals(2, found.getHands().get(0).getCards().size());
        assertFalse(found.getBids().isEmpty());
        assertEquals(2, found.getBids().size());
        assertTrue(found.getBids().get(1).isPass());
    }
}

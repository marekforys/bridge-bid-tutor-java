package com.example.bridge.repository;

import com.example.bridge.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DealRepositoryIntegrationTest {
    @Autowired
    DealRepository dealRepository;

    @Test
    void testPersistAndRetrieveDealWithAllData() {
        Deal deal = new Deal();
        // Create cards
        Card c1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card c2 = new Card(Card.Suit.SPADES, Card.Rank.KING);
        // Create hands
        Hand h1 = new Hand();
        h1.setPlayer(Player.NORTH);
        h1.setDeal(deal);
        c1.setHand(h1);
        c2.setHand(h1);
        h1.setCards(List.of(c1, c2));
        // Create bids
        Bid b1 = new Bid(1, Card.Suit.CLUBS);
        b1.setDeal(deal);
        b1.setPlayer(Player.NORTH);
        Bid b2 = Bid.pass();
        b2.setDeal(deal);
        b2.setPlayer(Player.EAST);
        // Set relationships
        h1.setDeal(deal);
        c1.setHand(h1);
        c2.setHand(h1);
        b1.setDeal(deal);
        b2.setDeal(deal);
        // Create deal
        deal.setBiddingSystem("natural");
        deal.setHands(List.of(h1));
        deal.setBids(List.of(b1, b2));
        // Persist
        Deal saved = dealRepository.save(deal);
        assertNotNull(saved.getId());
        Deal found = dealRepository.findById(saved.getId()).orElseThrow();
        assertEquals("natural", found.getBiddingSystem());
        assertEquals(1, found.getHands().size());
        assertEquals(2, found.getHands().get(0).getCards().size());
        assertEquals(2, found.getBids().size());
        assertTrue(found.getBids().get(1).isPass());
    }
}

package com.example.bridge.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    @Test
    void testCardCreationAndFields() {
        Card card = new Card(Card.Suit.DIAMONDS, Card.Rank.JACK);
        assertEquals(Card.Suit.DIAMONDS, card.getSuit());
        assertEquals(Card.Rank.JACK, card.getRank());
    }

    @Test
    void testToString() {
        Card card = new Card(Card.Suit.SPADES, Card.Rank.ACE);
        assertEquals("A", card.toString());
    }
}

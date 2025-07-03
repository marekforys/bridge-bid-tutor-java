package com.example.bridge.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HandTest {
    @Test
    void testSetAndGetCards() {
        Card c1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card c2 = new Card(Card.Suit.SPADES, Card.Rank.KING);
        Hand hand = new Hand();
        hand.setPlayer(Player.NORTH);
        Deal deal = new Deal();
        hand.setDeal(deal);
        c1.setHand(hand);
        c2.setHand(hand);
        hand.setCards(List.of(c1, c2));
        assertEquals(2, hand.getCards().size());
        assertEquals(Card.Suit.HEARTS, hand.getCards().get(0).getSuit());
    }

    @Test
    void testGetCardsBySuit() {
        Card c1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card c2 = new Card(Card.Suit.HEARTS, Card.Rank.KING);
        Card c3 = new Card(Card.Suit.SPADES, Card.Rank.QUEEN);
        Hand hand = new Hand();
        hand.setCards(List.of(c1, c2, c3));
        Map<Card.Suit, List<Card>> bySuit = hand.getCardsBySuit();
        assertEquals(2, bySuit.get(Card.Suit.HEARTS).size());
        assertEquals(1, bySuit.get(Card.Suit.SPADES).size());
    }
}

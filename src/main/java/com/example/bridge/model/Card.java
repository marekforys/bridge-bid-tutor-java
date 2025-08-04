package com.example.bridge.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Card implements Comparable<Card> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hand hand;

    @Enumerated(EnumType.STRING)
    private Suit suit;
    @Enumerated(EnumType.STRING)
    private Rank rank;

    public Card() {
    }

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public static List<Card> getShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if (suit == Suit.NOTRUMP) continue;
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    @Override
    public String toString() {
        return rank.getShortName() + suit.getShortName();
    }

    @Override
    public int compareTo(Card other) {
        int suitCompare = this.suit.compareTo(other.suit);
        if (suitCompare != 0) {
            return suitCompare;
        }
        return this.rank.compareTo(other.rank);
    }

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES, NOTRUMP;

        public String getShortName() {
            if (this == NOTRUMP)
                return "NT";
            return name().substring(0, 1);
        }
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

        public String getShortName() {
            switch (this) {
                case TWO:
                    return "2";
                case THREE:
                    return "3";
                case FOUR:
                    return "4";
                case FIVE:
                    return "5";
                case SIX:
                    return "6";
                case SEVEN:
                    return "7";
                case EIGHT:
                    return "8";
                case NINE:
                    return "9";
                case TEN:
                    return "10";
                case JACK:
                    return "J";
                case QUEEN:
                    return "Q";
                case KING:
                    return "K";
                case ACE:
                    return "A";
            }
            throw new IllegalStateException();
        }
    }
}

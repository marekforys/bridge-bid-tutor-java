package com.example.bridge.model;

public class Card {
    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        public String getShortName() {
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

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    @Override
    public String toString() {
        return rank.getShortName();
    }
}

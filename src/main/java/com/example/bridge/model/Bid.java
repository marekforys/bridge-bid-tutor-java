package com.example.bridge.model;

import jakarta.persistence.*;

@Entity
public class Bid implements Comparable<Bid> {

    public enum BidType {
        STANDARD, PASS, DOUBLE, REDOUBLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Deal deal;

    private int level;
    @Enumerated(EnumType.STRING)
    private Card.Suit suit;
    @Enumerated(EnumType.STRING)
    private BidType bidType;
    @Enumerated(EnumType.STRING)
    private Player player;

    public Bid() {
        this.bidType = BidType.PASS;
    }

    public Bid(int level, Card.Suit suit) {
        this.level = level;
        this.suit = suit;
        this.bidType = BidType.STANDARD;
    }

    private Bid(BidType type) {
        this.bidType = type;
    }

    public static Bid pass() {
        return new Bid(BidType.PASS);
    }

    public static Bid doubleBid() {
        return new Bid(BidType.DOUBLE);
    }

    public static Bid redoubleBid() {
        return new Bid(BidType.REDOUBLE);
    }

    public int getLevel() { return level; }
    public Card.Suit getSuit() { return suit; }
    public BidType getBidType() { return bidType; }
    public boolean isPass() { return bidType == BidType.PASS; }
    public boolean isDouble() { return bidType == BidType.DOUBLE; }
    public boolean isRedouble() { return bidType == BidType.REDOUBLE; }
    public boolean isStandard() {
        return bidType == BidType.STANDARD;
    }

    public boolean isNoTrump() {
        return suit == Card.Suit.NOTRUMP;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    @Override
    public String toString() {
        switch (bidType) {
            case PASS: return "Pass";
            case DOUBLE: return "Double";
            case REDOUBLE: return "Redouble";
            case STANDARD:
            default:
                return level + " " + (suit == Card.Suit.NOTRUMP ? "NT" : suit.getShortName());
        }
    }

    public int compareTo(Bid other) {
        if (this.isPass()) {
            return other.isPass() ? 0 : -1;
        }
        if (other.isPass()) {
            return 1;
        }

        // For now, only compare standard bids. Doubles/Redoubles are not ranked against standard bids.
        if (!this.isStandard() || !other.isStandard()) {
            return 0;
        }

        if (this.level != other.level) {
            return Integer.compare(this.level, other.level);
        }
        return Integer.compare(this.suit.ordinal(), other.suit.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        if (bidType != bid.bidType) return false;
        if (!isStandard()) return true; // All passes/doubles/redoubles of the same type are equal
        return level == bid.level && suit == bid.suit;
    }

    @Override
    public int hashCode() {
        if (!isStandard()) {
            return java.util.Objects.hash(bidType);
        }
        return java.util.Objects.hash(bidType, level, suit);
    }
}

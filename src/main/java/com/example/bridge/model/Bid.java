package com.example.bridge.model;

public class Bid {
    private final int level; // 1-7, 0 for pass
    private final Card.Suit suit; // null for pass
    private final boolean isPass;

    public Bid(int level, Card.Suit suit) {
        this.level = level;
        this.suit = suit;
        this.isPass = false;
    }

    public Bid() { // Pass
        this.level = 0;
        this.suit = null;
        this.isPass = true;
    }

    public int getLevel() { return level; }
    public Card.Suit getSuit() { return suit; }
    public boolean isPass() { return isPass; }

    @Override
    public String toString() {
        return isPass ? "Pass" : (level + " " + suit);
    }
}

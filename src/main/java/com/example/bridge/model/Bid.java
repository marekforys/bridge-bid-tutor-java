package com.example.bridge.model;

import jakarta.persistence.*;

@Entity
public class Bid implements Comparable<Bid> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal;

    private int level; // 1-7, 0 for pass
    @Enumerated(EnumType.STRING)
    private Card.Suit suit; // null for pass
    private boolean isPass;

    public Bid() {
    }

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

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    @Override
    public String toString() {
        return isPass ? "Pass" : (level + " " + (suit == Card.Suit.NOTRUMP ? "NT" : suit.getShortName()));
    }

    public int compareTo(Bid other) {
        if (this.isPass)
            return -1;
        if (other.isPass)
            return 1;
        if (this.level != other.level)
            return Integer.compare(this.level, other.level);
        return Integer.compare(this.suit.ordinal(), other.suit.ordinal());
    }
}

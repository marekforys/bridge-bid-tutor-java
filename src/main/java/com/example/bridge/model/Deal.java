package com.example.bridge.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String biddingSystem;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "deal")
    private List<Hand> hands;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "deal", fetch = FetchType.EAGER)
    private List<Bid> bids;

    @Enumerated(EnumType.STRING)
    private Player dealer;

    public Deal() {
    }

    public Deal(List<Hand> hands, String biddingSystem) {
        this.hands = hands;
        this.biddingSystem = biddingSystem;
    }

    public Long getId() {
        return id;
    }

    public List<Hand> getHands() {
        return hands;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public String getBiddingSystem() {
        return biddingSystem;
    }

    public void setBiddingSystem(String biddingSystem) {
        this.biddingSystem = biddingSystem;
    }

    public Player getDealer() {
        return dealer;
    }

    public void setDealer(Player dealer) {
        this.dealer = dealer;
    }
}

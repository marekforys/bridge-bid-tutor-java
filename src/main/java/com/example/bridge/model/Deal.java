package com.example.bridge.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deal_id")
    private List<Hand> hands; // 4 hands

    public Deal() {
    }

    public Deal(List<Hand> hands) {
        this.hands = hands;
    }

    public Long getId() {
        return id;
    }
    public List<Hand> getHands() {
        return hands;
    }
}

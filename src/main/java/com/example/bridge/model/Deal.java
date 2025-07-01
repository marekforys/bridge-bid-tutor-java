package com.example.bridge.model;

import java.util.List;

public class Deal {
    private final List<Hand> hands; // 4 hands

    public Deal(List<Hand> hands) {
        this.hands = hands;
    }

    public List<Hand> getHands() {
        return hands;
    }
}

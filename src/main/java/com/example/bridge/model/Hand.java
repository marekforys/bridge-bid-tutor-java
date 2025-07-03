package com.example.bridge.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Hand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Deal deal;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "hand")
    private List<Card> cards;

    @Enumerated(EnumType.STRING)
    private Player player;

    public Hand() {
    }

    public Hand(List<Card> cards) {
        this.cards = cards;
    }

    public Long getId() {
        return id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    public Map<Card.Suit, List<Card>> getCardsBySuit() {
        return cards.stream().collect(Collectors.groupingBy(Card::getSuit));
    }

    public Map<String, List<Card>> getCardsBySuitName() {
        return cards.stream().collect(Collectors.groupingBy(card -> card.getSuit().name()));
    }

    public Map<String, List<Card>> getSortedCardsBySuitName() {
        return cards.stream()
                .collect(Collectors.groupingBy(
                        card -> card.getSuit().name(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted((c1, c2) -> c2.getRank().ordinal() - c1.getRank().ordinal())
                                        .collect(Collectors.toList()))));
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}

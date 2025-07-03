package com.example.bridge.model;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Hand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hand_id")
    private List<Card> cards;

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
}

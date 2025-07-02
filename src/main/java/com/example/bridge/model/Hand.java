package com.example.bridge.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Hand {
    private final List<Card> cards;

    public Hand(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
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

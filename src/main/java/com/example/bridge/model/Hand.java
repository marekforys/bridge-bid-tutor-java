package com.example.bridge.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Collections;
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

    public int getHighCardPoints() {
        if (cards == null) {
            return 0;
        }
        int hcp = 0;
        for (Card card : cards) {
            switch (card.getRank()) {
                case ACE:
                    hcp += 4;
                    break;
                case KING:
                    hcp += 3;
                    break;
                case QUEEN:
                    hcp += 2;
                    break;
                case JACK:
                    hcp += 1;
                    break;
                default:
                    break;
            }
        }
        return hcp;
    }

    public String getSuitDistribution() {
        if (cards == null) {
            return "0-0-0-0";
        }
        Map<Card.Suit, List<Card>> bySuit = getCardsBySuit();
        return String.format("%d-%d-%d-%d",
                bySuit.getOrDefault(Card.Suit.SPADES, Collections.emptyList()).size(),
                bySuit.getOrDefault(Card.Suit.HEARTS, Collections.emptyList()).size(),
                bySuit.getOrDefault(Card.Suit.DIAMONDS, Collections.emptyList()).size(),
                bySuit.getOrDefault(Card.Suit.CLUBS, Collections.emptyList()).size()
        );
    }
}

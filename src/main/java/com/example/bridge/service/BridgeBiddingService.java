package com.example.bridge.service;

import com.example.bridge.model.*;
import com.example.bridge.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BridgeBiddingService {
    private static final Logger logger = LoggerFactory.getLogger(BridgeBiddingService.class);
    @Autowired
    DealRepository dealRepository;
    private Deal currentDeal;
    private List<Bid> biddingHistory = new ArrayList<>();
    private int currentBidderIndex = 0;
    private String biddingSystem = "natural";
    private int dealNumber = 0;
    private Player userSeat;

    public Deal startNewDeal() {
        // Stub: generate random hands
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : java.util.List.of(Card.Suit.CLUBS, Card.Suit.DIAMONDS, Card.Suit.HEARTS,
                Card.Suit.SPADES)) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        List<Hand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Card> cards = deck.subList(i * 13, (i + 1) * 13);
            Hand hand = new Hand(cards);
            hand.setPlayer(Player.values()[i]);
            for (Card card : cards) {
                card.setHand(hand);
            }
            hands.add(hand);
        }
        Deal deal = new Deal(hands, "");
        for (Hand hand : hands) {
            hand.setDeal(deal);
        }
        currentDeal = deal;
        biddingHistory.clear();
        // Increment deal number and set dealer
        dealNumber++;
        // Dealer rotates through N, E, S, W with each deal
        int dealerIndex = (dealNumber - 1) % 4;
        // Set current bidder to the dealer (bidding starts with dealer)
        currentBidderIndex = dealerIndex;
        // Set the dealer in the deal for reference
        deal.setDealer(Player.values()[dealerIndex]);
        // Randomly assign user seat
        userSeat = Player.values()[new Random().nextInt(4)];
        return currentDeal;
    }

    public void makeBid(Bid bid) {
        biddingHistory.add(bid);
        currentBidderIndex = (currentBidderIndex + 1) % 4;
    }

    public String getAdvice(Hand hand, List<Bid> history) {
        // Stub: always suggest "Pass"
        return "Advice: Pass";
    }

    public Deal getCurrentDeal() {
        return currentDeal;
    }

    public List<Bid> getBiddingHistory() {
        return biddingHistory;
    }

    public int getCurrentBidderIndex() {
        return currentBidderIndex;
    }

    public boolean isBidAllowed(Bid newBid) {
        if (newBid.isPass())
            return true;
        Bid highest = null;
        for (Bid b : biddingHistory) {
            if (!b.isPass() && (highest == null || b.compareTo(highest) > 0)) {
                highest = b;
            }
        }
        return highest == null || newBid.compareTo(highest) > 0;
    }

    public void setBiddingSystem(String system) {
        this.biddingSystem = system;
    }

    public String getBiddingSystem() {
        return biddingSystem;
    }

    public boolean isBiddingFinished() {
        if (biddingHistory.size() < 4)
            return false;
        int n = biddingHistory.size();
        return biddingHistory.get(n - 1).isPass() && biddingHistory.get(n - 2).isPass()
                && biddingHistory.get(n - 3).isPass();
    }

    public void saveDealIfFinished() {
        if (isBiddingFinished() && currentDeal != null) {
            // Use the dealer that was set when the deal was created
            int dealerIndex = currentDeal.getDealer().ordinal();
            for (int i = 0; i < biddingHistory.size(); i++) {
                Bid bid = biddingHistory.get(i);
                bid.setDeal(currentDeal);
                bid.setPlayer(Player.values()[(dealerIndex + i) % 4]);
            }
            currentDeal.setBids(new ArrayList<>(biddingHistory));
            currentDeal.setBiddingSystem(biddingSystem);
            dealRepository.save(currentDeal);
        }
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public Player getCurrentDealer() {
        if (currentDeal != null && currentDeal.getDealer() != null) {
            return currentDeal.getDealer();
        }
        // Fallback to the old calculation if dealer is not set
        return Player.values()[((dealNumber - 1) % 4 + 4) % 4];
    }

    public Player getUserSeat() {
        return userSeat;
    }

    // Get the Hand for a given Player from the current deal
    public Hand getHandForPlayer(Player player) {
        if (currentDeal == null)
            return null;
        for (Hand hand : currentDeal.getHands()) {
            if (hand.getPlayer() == player)
                return hand;
        }
        return null;
    }

    // Simple natural system: open 1 of the longest suit if 12+ HCP, else Pass
    public Bid getSimpleNaturalBid(Hand hand, List<Bid> history) {
        if (hand == null) {
            return Bid.pass();
        }
        final int hcp = hand.getHighCardPoints(); // Make final to emphasize it's calculated once
        
        // --- RESPONSE LOGIC ---
        Player me = hand.getPlayer();
        if (me != null) {
            // Find partner's last non-pass bid
            if (history != null && !history.isEmpty()) {
                Bid partnerLastBid = null;
                Bid myLastBid = null;
                int myLastBidIndex = -1;
                int partnerLastBidIndex = -1;

                logger.info("--- Bidding Analysis for Player: {} ---", hand.getPlayer());
                logger.info("Hand HCP: {}, Distribution: {}", hand.getHighCardPoints(), hand.getSuitDistribution());

                for (int i = history.size() - 1; i >= 0; i--) {
                    Bid bid = history.get(i);
                    if (!bid.isPass() && bid.getPlayer() != null) {
                        if (bid.getPlayer() == hand.getPlayer()) {
                            if (myLastBid == null) {
                                myLastBid = bid;
                                myLastBidIndex = i;
                            }
                        } else if (bid.getPlayer().getPartner() == hand.getPlayer()) {
                            if (partnerLastBid == null) {
                                partnerLastBid = bid;
                                partnerLastBidIndex = i;
                            }
                        }
                    }
                }

                logger.info("My Last Bid: {} (index {}), Partner Last Bid: {} (index {})", myLastBid, myLastBidIndex, partnerLastBid, partnerLastBidIndex);

                // --- Bidding Logic Flow ---

                // 1. Check for Opener's Rebid
                logger.info("Checking Opener's Rebid logic...");
                if (myLastBid != null && partnerLastBid != null && myLastBidIndex < partnerLastBidIndex) {
                    // After partner's Stayman 2C bid
                    if (myLastBid.equals(new Bid(1, Card.Suit.NOTRUMP)) && partnerLastBid.equals(new Bid(2, Card.Suit.CLUBS))) {
                        logger.info("Condition met for Stayman rebid.");
                        long hearts = hand.getCards().stream().filter(c -> c.getSuit() == Card.Suit.HEARTS).count();
                        long spades = hand.getCards().stream().filter(c -> c.getSuit() == Card.Suit.SPADES).count();

                        if (hearts >= 4) { logger.info("Rebidding 2H"); return new Bid(2, Card.Suit.HEARTS); }
                        if (spades >= 4) { logger.info("Rebidding 2S"); return new Bid(2, Card.Suit.SPADES); }
                        logger.info("Rebidding 2D");
                        return new Bid(2, Card.Suit.DIAMONDS);
                    }
                    // Other rebid logic can be added here.
                    return Bid.pass(); // Default rebid is to pass if no other condition is met.
                }

                // 2. Check for Responder's First Bid
                logger.info("Checking Responder's First Bid logic...");
                boolean isMyTurnToRespond = partnerLastBid != null && (myLastBid == null || myLastBidIndex < partnerLastBidIndex);
                if (isMyTurnToRespond) {
                    logger.info("Condition met for Responder's logic.");

                    // Respond to partner's 1NT opening
                    if (partnerLastBid.getLevel() == 1 && partnerLastBid.getSuit() == Card.Suit.NOTRUMP) {
                        logger.info("Checking Responder's 1NT response logic...");
                        // Check for Stayman: 8+ HCP and a 4-card major
                        if (hcp >= 8) {
                            logger.info("Checking for Stayman...");
                            long hearts = hand.getCards().stream().filter(c -> c.getSuit() == Card.Suit.HEARTS).count();
                            long spades = hand.getCards().stream().filter(c -> c.getSuit() == Card.Suit.SPADES).count();
                            logger.info("Hearts: {}, Spades: {}", hearts, spades);
                            if (hearts >= 4 || spades >= 4) {
                                logger.info("Returning Stayman bid...");
                                return new Bid(2, Card.Suit.CLUBS); // Stayman
                            }
                        }

                        // Standard NT responses if Stayman is not applicable
                        if (hcp >= 10) return new Bid(3, Card.Suit.NOTRUMP); // Game
                        if (hcp >= 8) return new Bid(2, Card.Suit.NOTRUMP); // Invitational
                        return Bid.pass(); // 0-7 HCP
                    }

                    // Check for NT responses first when partner opens a minor
                    if ((partnerLastBid.getSuit() == Card.Suit.CLUBS || partnerLastBid.getSuit() == Card.Suit.DIAMONDS) &&
                            partnerLastBid.getLevel() == 1) {
                        if (hcp >= 6 && hcp <= 9) {
                            return new Bid(1, Card.Suit.NOTRUMP);
                        } else if (hcp >= 10 && hcp <= 12) {
                            return new Bid(2, Card.Suit.NOTRUMP);
                        }
                    }

                    // Then check for raises
                    if (partnerLastBid.getSuit() != null && partnerLastBid.getSuit() != Card.Suit.NOTRUMP) {
                        final Card.Suit partnerSuit = partnerLastBid.getSuit();
                        int count = (int) hand.getCards().stream()
                                .filter(c -> c.getSuit() == partnerSuit)
                                .limit(13) // Ensure we don't count more than 13 cards
                                .count();

                        // With 4+ support, raise partner's suit
                        if (count >= 4) {
                            // Single raise with 6-9 HCP
                            if (hcp >= 6 && hcp <= 9) {
                                int raiseLevel = partnerLastBid.getLevel() + 1;
                                Bid resp = new Bid(raiseLevel, partnerSuit);
                                if (isBidAllowed(resp)) {
                                    return resp;
                                }
                            }
                            // Double raise with 10-12 HCP
                            else if (hcp >= 10 && hcp <= 12) {
                                int raiseLevel = partnerLastBid.getLevel() + 2;
                                // Don't go beyond game level (4)
                                if (raiseLevel <= 4) {
                                    Bid resp = new Bid(raiseLevel, partnerSuit);
                                    if (isBidAllowed(resp)) {
                                        return resp;
                                    }
                                }
                            }
                            // With 5+ HCP and 4+ card support, make a simple raise
                            else if (hcp >= 5) {
                                int raiseLevel = partnerLastBid.getLevel() + 1;
                                if (raiseLevel <= 4) {
                                    Bid resp = new Bid(raiseLevel, partnerSuit);
                                    if (isBidAllowed(resp)) {
                                        return resp;
                                    }
                                }
                            }
                        }
                    }
                    // NT responses
                    if (hcp >= 6 && hcp <= 9) {
                        // 1NT response to 1-level opening
                        if (partnerLastBid.getLevel() == 1) {
                            Bid resp = new Bid(1, Card.Suit.NOTRUMP);
                            if (isBidAllowed(resp)) {
                                return resp;
                            } else {
                                System.out.println("  1NT response not allowed");
                            }
                        } else {
                            System.out.println("  Partner's bid is not at 1-level: " + partnerLastBid.getLevel());
                        }
                    } else if (hcp >= 10 && hcp <= 12) {
                        // 2NT response to 1-level opening
                        if (partnerLastBid.getLevel() == 1) {
                            Bid resp = new Bid(2, Card.Suit.NOTRUMP);
                            System.out.println("  Considering 2NT response");
                            if (isBidAllowed(resp)) {
                                System.out.println("  Returning 2NT response");
                                return resp;
                            } else {
                                System.out.println("  2NT response not allowed");
                            }
                        } else {
                            System.out.println("  Partner's bid is not at 1-level: " + partnerLastBid.getLevel());
                        }
                    }
                    // If we get here, no suitable response was found
                    System.out.println("  No suitable response found, passing");
                    return Bid.pass();
                }
            }
        }

        // --- END RESPONSE LOGIC ---

        // Opening bids logic
        logger.info("Evaluating for an opening bid...");
        // Check for 1NT opening: 15-17 HCP and a balanced hand
        if (hcp >= 15 && hcp <= 17 && isBalanced(hand)) {
            logger.info("Opening 1NT.");
            return new Bid(1, Card.Suit.NOTRUMP);
        }

        if (hcp < 12) {
            return Bid.pass();
        }

        // Find longest suit, with standard bridge suit order for tie-breaking
        Map<Card.Suit, List<Card>> bySuit = hand.getCardsBySuit();
        Card.Suit longest = null;
        int maxLength = 0;

        // First find the maximum length of any suit
        for (List<Card> cardsInSuit : bySuit.values()) {
            if (cardsInSuit.size() > maxLength) {
                maxLength = cardsInSuit.size();
            }
        }

        // Then find the highest-ranking suit with that length
        // Standard order: Spades > Hearts > Diamonds > Clubs
        if (bySuit.getOrDefault(Card.Suit.SPADES, List.of()).size() == maxLength) {
            longest = Card.Suit.SPADES;
        } else if (bySuit.getOrDefault(Card.Suit.HEARTS, List.of()).size() == maxLength) {
            longest = Card.Suit.HEARTS;
        } else if (bySuit.getOrDefault(Card.Suit.DIAMONDS, List.of()).size() == maxLength) {
            longest = Card.Suit.DIAMONDS;
        } else if (bySuit.getOrDefault(Card.Suit.CLUBS, List.of()).size() == maxLength) {
            longest = Card.Suit.CLUBS;
        }

        if (longest == null) {
            logger.info("No openable suit found. Passing.");
            return Bid.pass();
        }

        // Only open at 1-level for now
        logger.info("Opening 1 of longest suit: {}.");
        return new Bid(1, longest);
    }

    private boolean isBalanced(Hand hand) {
        Map<Card.Suit, List<Card>> bySuit = hand.getCardsBySuit();
        int doubletons = 0;
        for (Card.Suit suit : Card.Suit.values()) {
            if (suit == Card.Suit.NOTRUMP) continue;
            int count = bySuit.getOrDefault(suit, Collections.emptyList()).size();
            if (count < 2) return false; // Void or singleton
            if (count == 2) doubletons++;
        }
        return doubletons <= 1;
    }
}

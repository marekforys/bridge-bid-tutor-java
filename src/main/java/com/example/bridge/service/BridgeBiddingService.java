package com.example.bridge.service;

import com.example.bridge.model.Bid;
import com.example.bridge.model.Card;
import com.example.bridge.model.Deal;
import com.example.bridge.model.Hand;
import com.example.bridge.model.Player;
import com.example.bridge.repository.DealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BridgeBiddingService {

    private static final Logger logger = LoggerFactory.getLogger(BridgeBiddingService.class);

    private Deal currentDeal;
    private int currentBidderIndex = 0;
    private int currentDealerIndex = 0;
    private List<Bid> biddingHistory = new ArrayList<>();
    private String biddingSystem = "2/1 Game Forcing";

    @Autowired
    private DealRepository dealRepository;

    public Deal startNewDeal() {
        // Cycle to next dealer
        currentDealerIndex = (currentDealerIndex + 1) % 4;
        return startNewDeal(Player.values()[currentDealerIndex]);
    }

    public Deal startNewDeal(Player dealer) {
        this.currentDeal = new Deal(dealer);
        this.biddingHistory.clear();
        this.currentBidderIndex = dealer.ordinal();
        this.currentDealerIndex = dealer.ordinal();

        List<Card> deck = Card.getShuffledDeck();
        List<Hand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Hand hand = new Hand(new ArrayList<>(deck.subList(i * 13, (i + 1) * 13)), Player.values()[i]);
            hands.add(hand);
        }

        currentDeal.setHands(hands);
        currentDeal.setBiddingSystem(biddingSystem);
        for (Hand hand : hands) {
            hand.setDeal(currentDeal);
        }
        return currentDeal;
    }

    public void saveDeal() {
        if (currentDeal != null) {
            dealRepository.save(currentDeal);
        }
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public Hand getHandForPlayer(Player player) {
        if (currentDeal == null || currentDeal.getHands() == null) {
            return null;
        }
        return currentDeal.getHands().stream()
                .filter(h -> h.getPlayer() == player)
                .findFirst()
                .orElse(null);
    }

    public Player getCurrentBidder() {
        return Player.values()[currentBidderIndex];
    }

    public void makeBid(Bid bid) {
        if (isBiddingFinished()) {
            logger.warn("Bidding is already finished. No more bids allowed.");
            return;
        }

        if (isBidAllowed(bid)) {
            bid.setPlayer(getCurrentBidder());
            biddingHistory.add(bid);
            currentDeal.addBid(bid);
            if (!bid.isPass()) {
                currentDeal.setContract(bid.toString());
            }
            logger.info("Bid made: {} by {}", bid, bid.getPlayer());
            advanceToNextBidder();
        } else {
            logger.warn("Bid {} is not allowed.", bid);
        }
    }

    private void advanceToNextBidder() {
        currentBidderIndex = (currentBidderIndex + 1) % 4;
    }

    public boolean isBidAllowed(Bid bid) {
        if (bid.isPass()) {
            return true;
        }

        Optional<Bid> lastStandardBidOpt = biddingHistory.stream()
                .filter(b -> !b.isPass())
                .reduce((first, second) -> second);

        if (lastStandardBidOpt.isEmpty()) {
            return true;
        }

        Bid lastStandardBid = lastStandardBidOpt.get();

        if (bid.isDouble()) {
            return canDouble(lastStandardBid);
        }

        if (bid.isRedouble()) {
            return canRedouble(lastStandardBid);
        }

        return bid.getLevel() > lastStandardBid.getLevel() ||
                (bid.getLevel() == lastStandardBid.getLevel() && bid.getSuit().ordinal() > lastStandardBid.getSuit().ordinal());
    }

    private boolean canDouble(Bid lastStandardBid) {
        if (lastStandardBid.isDouble() || lastStandardBid.isRedouble()) {
            return false;
        }
        Player lastBidder = lastStandardBid.getPlayer();
        Player currentBidder = getCurrentBidder();
        return lastBidder.isOpponent(currentBidder);
    }

    private boolean canRedouble(Bid lastStandardBid) {
        if (!lastStandardBid.isDouble()) {
            return false;
        }
        
        // Find the original bid that was doubled (the bid before the double)
        Bid originalBid = null;
        for (int i = biddingHistory.size() - 2; i >= 0; i--) {
            Bid bid = biddingHistory.get(i);
            if (!bid.isPass()) {
                originalBid = bid;
                break;
            }
        }
        
        if (originalBid == null || originalBid.isDouble() || originalBid.isRedouble()) {
            return false;
        }
        
        // Only the partner of the original bidder can redouble
        Player originalBidder = originalBid.getPlayer();
        Player currentBidder = getCurrentBidder();
        return originalBidder.getPartner() == currentBidder;
    }

    public boolean isBiddingFinished() {
        if (biddingHistory.size() < 4) return false;

        boolean hasBeenStandardBid = biddingHistory.stream().anyMatch(Bid::isStandard);
        
        int passCount = 0;
        for (int i = biddingHistory.size() - 1; i >= 0; i--) {
            if (biddingHistory.get(i).isPass()) {
                passCount++;
            } else {
                break;
            }
        }
        
        // Bidding is finished if:
        // 1. 4 consecutive passes (no standard bids made), OR
        // 2. 3 consecutive passes after at least one standard bid
        return (!hasBeenStandardBid && passCount >= 4) || (hasBeenStandardBid && passCount >= 3);
    }

    public Bid getSimpleNaturalBid(List<Bid> biddingHistory) {
        Player currentBidder = getCurrentBidder();
        Hand hand = getHandForPlayer(currentBidder);
        if (hand == null) {
            logger.error("Hand is null for player {}", currentBidder);
            return Bid.pass();
        }

        int hcp = hand.getHighCardPoints();
        Map<Card.Suit, Integer> suitLengths = hand.getSuitLengths();
        boolean isOpening = isOpeningBid(biddingHistory);

        Bid finalBid = Bid.pass();

        if (isOpening) {
            if (hcp >= 15 && hcp <= 17 && hand.isBalanced()) {
                finalBid = new Bid(1, Card.Suit.NOTRUMP);
            } else if (hcp >= 13) {
                finalBid = openLongestSuit(hand, suitLengths);
            }
        } else { // Responding or rebidding
        Bid myLastBid = getLastSignificantBidByPlayer(currentBidder, biddingHistory);
        Bid partnerLastBid = getLastSignificantBidByPlayer(currentBidder.getPartner(), biddingHistory);

        // Opener's rebid to Stayman
        if (myLastBid != null && myLastBid.isNoTrump() && myLastBid.getLevel() == 1 &&
                partnerLastBid != null && partnerLastBid.getLevel() == 2 && partnerLastBid.getSuit() == Card.Suit.CLUBS) {
            if (suitLengths.getOrDefault(Card.Suit.HEARTS, 0) >= 4) {
                return new Bid(2, Card.Suit.HEARTS);
            } else if (suitLengths.getOrDefault(Card.Suit.SPADES, 0) >= 4) {
                return new Bid(2, Card.Suit.SPADES);
            } else {
                return new Bid(2, Card.Suit.DIAMONDS);
            }
        }

            Bid partnerBid = getLastSignificantBidByPlayer(currentBidder.getPartner(), biddingHistory);
            if (partnerBid != null) {
                if (partnerBid.isNoTrump() && partnerBid.getLevel() == 1) {
                    if (hcp <= 7) {
                        finalBid = Bid.pass(); // Weak hands pass
                    } else if (hcp >= 10) { // Game-forcing points
                        finalBid = new Bid(3, Card.Suit.NOTRUMP);
                    } else { // Invitational hands (8-9 points)
                        Bid staymanBid = getStaymanResponse(hand, partnerBid, biddingHistory, currentBidder);
                        if (staymanBid != null) {
                            finalBid = staymanBid;
                        } else if (hand.isBalanced()) {
                            finalBid = new Bid(2, Card.Suit.NOTRUMP); // Invitational 2NT
                        } else {
                            finalBid = openLongestSuit(hand, suitLengths); // Unbalanced, bid longest suit
                        }
                    }
                } else if (partnerBid.isStandard()) {
                    Card.Suit partnerSuit = partnerBid.getSuit();
                    int partnerSuitLength = suitLengths.getOrDefault(partnerSuit, 0);
                    logger.info("Partner bid: {} {}, HCP: {}, Partner suit length: {}", partnerBid.getLevel(), partnerSuit, hcp, partnerSuitLength);
                    
                    // For major suits, need 3+ cards; for minor suits, need 4+ cards to raise
                    boolean hasGoodSupport = (partnerSuit == Card.Suit.HEARTS || partnerSuit == Card.Suit.SPADES) 
                        ? partnerSuitLength >= 3 : partnerSuitLength >= 4;
                    
                    if (hasGoodSupport && hcp >= 5) { // Support partner with good fit
                        logger.info("Supporting partner's suit with {} cards", partnerSuitLength);
                        finalBid = new Bid(partnerBid.getLevel() + 1, partnerSuit);
                    } else if (hcp >= 6 && hcp <= 9) { // 6-9 HCP with no fit, respond 1NT
                        logger.info("Responding 1NT with {} HCP and no fit", hcp);
                        finalBid = new Bid(1, Card.Suit.NOTRUMP);
                    } else if (hcp >= 10 && hcp <= 12) { // 10-12 HCP with no fit, respond 2NT
                        logger.info("Responding 2NT with {} HCP and no fit", hcp);
                        finalBid = new Bid(2, Card.Suit.NOTRUMP);
                    } else if (hcp < 6) { // Weak hands should pass
                        logger.info("Passing with weak hand ({} HCP) when responding to partner bid", hcp);
                        finalBid = Bid.pass();
                    } else {
                        logger.info("Opening longest suit with {} HCP", hcp);
                        finalBid = openLongestSuit(hand, suitLengths);
                    }
                }
            } else { // No partner bid, treat as opening
                if (hcp >= 13) {
                    finalBid = openLongestSuit(hand, suitLengths);
                }
            }
        }

        if (!isBidAllowed(finalBid)) {
            logger.warn("Proposed bid {} is not allowed. Passing instead.", finalBid);
            finalBid = Bid.pass();
        }

        return finalBid;
    }

    private Bid openLongestSuit(Hand hand, Map<Card.Suit, Integer> suitLengths) {
        if (hand.getHighCardPoints() < 13) {
            return Bid.pass();
        }

        Optional<Map.Entry<Card.Suit, Integer>> longestSuit = suitLengths.entrySet().stream()
                .filter(entry -> entry.getKey() != Card.Suit.NOTRUMP)
                .max(Comparator.comparing(Map.Entry::getValue));

        if (longestSuit.isPresent() && longestSuit.get().getValue() >= 5) {
            return new Bid(1, longestSuit.get().getKey());
        } else {
            // No 5-card suit, bid longest minor
            int diamondLength = suitLengths.getOrDefault(Card.Suit.DIAMONDS, 0);
            int clubLength = suitLengths.getOrDefault(Card.Suit.CLUBS, 0);
            if (diamondLength >= clubLength && diamondLength > 0) {
                return new Bid(1, Card.Suit.DIAMONDS);
            } else if (clubLength > 0) {
                return new Bid(1, Card.Suit.CLUBS);
            }
            return Bid.pass(); // Should not be reached with a valid hand
        }
    }

    private Bid getStaymanResponse(Hand hand, Bid partnerBid, List<Bid> biddingHistory, Player currentBidder) {
        if (partnerBid.getLevel() == 1 && partnerBid.getSuit() == Card.Suit.NOTRUMP) {
            int hcp = hand.getHighCardPoints();
            Map<Card.Suit, Integer> suitLengths = hand.getSuitLengths();
            boolean hasFourCardMajor = suitLengths.getOrDefault(Card.Suit.HEARTS, 0) >= 4 || suitLengths.getOrDefault(Card.Suit.SPADES, 0) >= 4;
            
            if (hasFourCardMajor) {
                // Use Stayman with 9+ HCP, or 8 HCP with unbalanced hand, or 8 HCP with strong major suit honors
                if (hcp >= 9 || (hcp == 8 && !hand.isBalanced()) || (hcp == 8 && hasStrongMajorSuit(hand))) {
                    return new Bid(2, Card.Suit.CLUBS); // Stayman bid
                }
            }
        }
        return null;
    }

    private boolean hasStrongMajorSuit(Hand hand) {
        Map<Card.Suit, Integer> suitLengths = hand.getSuitLengths();
        List<Card> allCards = hand.getCards();

        for (Card.Suit suit : new Card.Suit[]{Card.Suit.HEARTS, Card.Suit.SPADES}) {
            if (suitLengths.getOrDefault(suit, 0) >= 4) {
                int strongCards = 0;
                for (Card card : allCards) {
                    if (card.getSuit() == suit && (card.getRank() == Card.Rank.ACE || card.getRank() == Card.Rank.KING || card.getRank() == Card.Rank.QUEEN)) {
                        strongCards++;
                    }
                }
                if (strongCards >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOpeningBid(List<Bid> biddingHistory) {
        return biddingHistory.stream().allMatch(Bid::isPass);
    }

    private Bid getLastSignificantBidByPlayer(Player player, List<Bid> biddingHistory) {
        return biddingHistory.stream()
                .filter(b -> b.getPlayer() == player && !b.isPass())
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public int getCurrentBidderIndex() {
        return currentBidderIndex;
    }

    public void setCurrentBidderIndex(int currentBidderIndex) {
        this.currentBidderIndex = currentBidderIndex;
    }

    public int getCurrentDealerIndex() {
        return currentDealerIndex;
    }

    public List<Bid> getBiddingHistory() {
        return biddingHistory;
    }

    public String getBiddingSystem() {
        return biddingSystem;
    }

    public void setBiddingSystem(String biddingSystem) {
        this.biddingSystem = biddingSystem;
    }

    public Deal getCurrentDeal() {
        return currentDeal;
    }

    public Player getCurrentDealer() {
        if (currentDeal != null) {
            return currentDeal.getDealer();
        }
        return Player.values()[currentDealerIndex];
    }

    public Player getUserSeat() {
        // For now, we'll assume the user is always SOUTH.
        // This can be made configurable later.
        return Player.SOUTH;
    }

    public void saveDealIfFinished() {
        if (isBiddingFinished()) {
            saveDeal();
        }
    }

    public String getAdvice(Hand hand, List<Bid> biddingHistory) {
        // TODO: Implement actual bidding advice logic
        return "No advice available yet.";
    }
}

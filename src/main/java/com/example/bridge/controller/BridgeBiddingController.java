package com.example.bridge.controller;

import com.example.bridge.model.*;
import com.example.bridge.service.BridgeBiddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class BridgeBiddingController {
    @Autowired
    private BridgeBiddingService biddingService;

    @GetMapping("/")
    public String index(@RequestParam(value = "biddingSystem", required = false) String biddingSystem,
            @RequestParam(value = "trainingMode", required = false) String trainingMode,
            Model model) {
        if (biddingSystem != null) {
            biddingService.setBiddingSystem(biddingSystem);
        }
        if (trainingMode == null) {
            trainingMode = "single";
        }
        model.addAttribute("trainingMode", trainingMode);
        Deal deal = biddingService.getCurrentDeal();
        if (deal == null) {
            deal = biddingService.startNewDeal();
            // --- AUTO BIDDING LOGIC FOR SINGLE HAND MODE ON FIRST DEAL ---
            if ("single".equals(trainingMode)) {
                int userSeatIndex = biddingService.getUserSeat().ordinal();
                while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                    Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                    biddingService.makeBid(autoBid);
                }
            }
        }
        // Debug: print number of cards in each hand
        int handIdx = 0;
        for (Hand h : deal.getHands()) {
            System.out.println("Hand " + handIdx + " (" + h.getPlayer() + ") has "
                    + (h.getCards() == null ? 0 : h.getCards().size()) + " cards: "
                    + (h.getCards() == null ? "null" : h.getCards()));
            handIdx++;
        }
        // Set the dealer for the current deal (cycling N, E, S, W)
        Player dealer = biddingService.getCurrentDealer();
        model.addAttribute("dealer", dealer);
        model.addAttribute("dealerIndex", dealer.ordinal());
        // Get user seat
        Player userSeat = biddingService.getUserSeat();
        model.addAttribute("userSeat", userSeat);
        // Pick the current bidder's hand for the main screen
        int currentBidderIndex = biddingService.getCurrentBidderIndex();
        List<Hand> hands = deal.getHands();
        Hand hand = hands.get(currentBidderIndex);
        model.addAttribute("hand", hand);
        model.addAttribute("handIndex", currentBidderIndex);
        model.addAttribute("handBySuit", hand.getSortedCardsBySuitName());
        int totalPoints = 0;
        if (hand.getCards() != null) {
            for (Card card : hand.getCards()) {
                switch (card.getRank()) {
                    case ACE:
                        totalPoints += 4;
                        break;
                    case KING:
                        totalPoints += 3;
                        break;
                    case QUEEN:
                        totalPoints += 2;
                        break;
                    case JACK:
                        totalPoints += 1;
                        break;
                    default:
                        break;
                }
            }
        }
        model.addAttribute("totalPointsInHand", totalPoints);
        model.addAttribute("deal", deal);
        // Precompute biddingRounds for the template
        List<Bid> biddingHistory = biddingService.getBiddingHistory();
        List<String[]> biddingRounds = prepareBiddingTable(biddingHistory, dealer);
        model.addAttribute("biddingRounds", biddingRounds);
        // For popup: all hands by suit
        List<Map<String, List<Card>>> handsBySuit = new java.util.ArrayList<>();
        for (Hand h : deal.getHands()) {
            handsBySuit.add(h.getSortedCardsBySuitName());
        }
        model.addAttribute("handsBySuit", handsBySuit);
        model.addAttribute("biddingHistory", biddingService.getBiddingHistory());
        model.addAttribute("currentBidderIndex", biddingService.getCurrentBidderIndex());
        model.addAttribute("currentBidder",
                com.example.bridge.model.Player.values()[biddingService.getCurrentBidderIndex()]);
        model.addAttribute("biddingFinished", biddingService.isBiddingFinished());
        model.addAttribute("canDouble", biddingService.isBidAllowed(Bid.doubleBid()));
        model.addAttribute("canRedouble", biddingService.isBidAllowed(Bid.redoubleBid()));
        model.addAttribute("biddingSystem", biddingService.getBiddingSystem());
        model.addAttribute("allDeals", biddingService.getAllDeals());
        model.addAttribute("suitsOrdered", java.util.List.of(
            com.example.bridge.model.Card.Suit.SPADES,
            com.example.bridge.model.Card.Suit.HEARTS,
            com.example.bridge.model.Card.Suit.DIAMONDS,
            com.example.bridge.model.Card.Suit.CLUBS
        ));
        // Pick hands to display based on trainingMode
        List<Hand> displayHands = new ArrayList<>();
        if ("all".equals(trainingMode)) {
            displayHands.addAll(hands);
        } else if ("pair".equals(trainingMode)) {
            displayHands.add(hand);
            // Partner: (currentBidderIndex + 2) % 4
            displayHands.add(hands.get((currentBidderIndex + 2) % 4));
        } else { // single
            // Show the user's hand
            int userSeatIndex = userSeat.ordinal();
            displayHands.add(hands.get(userSeatIndex));
        }
        // Calculate total points for each display hand
        List<Integer> displayHandPoints = new ArrayList<>();
        for (Hand h : displayHands) {
            int points = 0;
            if (h.getCards() != null) {
                for (Card card : h.getCards()) {
                    switch (card.getRank()) {
                        case ACE:
                            points += 4;
                            break;
                        case KING:
                            points += 3;
                            break;
                        case QUEEN:
                            points += 2;
                            break;
                        case JACK:
                            points += 1;
                            break;
                        default:
                            break;
                    }
                }
            }
            displayHandPoints.add(points);
        }
        model.addAttribute("displayHandPoints", displayHandPoints);
        model.addAttribute("displayHands", displayHands);
        // Find highest non-pass bid for client-side validation
        Bid highestBid = null;
        for (Bid b : biddingService.getBiddingHistory()) {
            if (!b.isPass() && (highestBid == null || b.compareTo(highestBid) > 0)) {
                highestBid = b;
            }
        }
        if (highestBid != null) {
            model.addAttribute("highestBidLevel", highestBid.getLevel());
            model.addAttribute("highestBidSuit", highestBid.getSuit() != null ? highestBid.getSuit().name() : null);
        } else {
            model.addAttribute("highestBidLevel", null);
            model.addAttribute("highestBidSuit", null);
        }
        return "index";
    }

    @PostMapping("/new-deal")
    public String newDeal(@RequestParam(value = "trainingMode", required = false) String trainingMode) {
        if (trainingMode == null || trainingMode.isEmpty()) {
            trainingMode = "single";
        }
        biddingService.startNewDeal();
        // --- AUTO BIDDING LOGIC FOR SINGLE HAND MODE ON NEW DEAL ---
        if ("single".equals(trainingMode)) {
            int userSeatIndex = biddingService.getUserSeat().ordinal();
            while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                biddingService.makeBid(autoBid);
            }
        }
        return "redirect:/";
    }

    @PostMapping("/make-bid")
    public String makeBid(@RequestParam(required = false) Integer level,
            @RequestParam(required = false) Card.Suit suit,
            @RequestParam(required = false) String pass,
            @RequestParam(required = false) String doubleBid,
            @RequestParam(required = false) String redoubleBid,
            @RequestParam(value = "biddingSystem", required = false) String biddingSystem,
            @RequestParam(value = "trainingMode", required = false) String trainingMode,
                    Model model) {
        if (biddingSystem != null) {
            biddingService.setBiddingSystem(biddingSystem);
        }
        if (biddingService.isBiddingFinished()) {
            model.addAttribute("bidError", "Bidding is finished.");
            return index(null, null, model);
        }
        Bid bid;
        if (pass != null) {
            bid = Bid.pass();
        } else if (doubleBid != null) {
            bid = Bid.doubleBid();
        } else if (redoubleBid != null) {
            bid = Bid.redoubleBid();
        } else if (level != null && suit != null) {
            bid = new Bid(level, suit);
        } else {
            model.addAttribute("bidError", "Invalid bid action.");
            return index(null, null, model);
        }
        if (!biddingService.isBidAllowed(bid)) {
            model.addAttribute("bidError", "Bid must be higher than previous bids.");
            return index(null, null, model);
        }
        biddingService.makeBid(bid);
        // --- AUTO BIDDING LOGIC FOR SINGLE HAND MODE ---
        if ("single".equals(trainingMode)) {
            int userSeatIndex = biddingService.getUserSeat().ordinal();
            // After user's bid, let AI bid until it's user's turn again
            while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                biddingService.makeBid(autoBid);
            }
        }
        biddingService.saveDealIfFinished();
        return "redirect:/";
    }

// ...
    @ResponseBody
    public String getAdvice(@PathVariable int handIndex) {
        Deal deal = biddingService.getCurrentDeal();
        if (deal == null)
            return "No deal";
        Hand hand = deal.getHands().get(handIndex);
        return biddingService.getAdvice(hand, biddingService.getBiddingHistory());
    }

    // Helper to trim last 3 passes from bidding history if present
    public List<Bid> getBiddingWithoutTrailingPasses(List<Bid> bids) {
        if (bids == null || bids.size() < 4)
            return bids;
        int n = bids.size();
        if (bids.get(n - 1).isPass() && bids.get(n - 2).isPass() && bids.get(n - 3).isPass()) {
            return bids.subList(0, n - 3);
        }
        return bids;
    }

    @GetMapping("/past-deals")
    public String pastDeals(Model model) {
        List<Deal> allDeals = biddingService.getAllDeals();
        // For each deal, trim trailing passes for display
        List<List<Bid>> allBidsTrimmed = new ArrayList<>();
        List<String> finalBids = new ArrayList<>();
        List<String> dealers = new ArrayList<>();
        List<String> playerHands = new ArrayList<>();
        for (int dealIdx = 0; dealIdx < allDeals.size(); dealIdx++) {
            Deal deal = allDeals.get(dealIdx);
            List<Bid> trimmed = getBiddingWithoutTrailingPasses(deal.getBids());
            allBidsTrimmed.add(trimmed);
            // Final bid: last non-pass bid or "-"
            String lastBid = "-";
            for (int i = trimmed.size() - 1; i >= 0; i--) {
                if (!trimmed.get(i).isPass()) {
                    lastBid = renderBidHtml(trimmed.get(i));
                    break;
                }
            }
            finalBids.add(lastBid);
            // Dealer: cycle N, E, S, W by deal index
            String dealer = com.example.bridge.model.Player.values()[dealIdx % 4].toString();
            dealers.add(dealer.substring(0, 1));
            // Player hand: show South (deal.getHands().get(2)) as example, or could be
            // based on user
            String playerHand = deal.getHands() != null && deal.getHands().size() > 2
                    ? deal.getHands().get(2).getPlayer().toString()
                    : "-";
            playerHands.add(playerHand.equals("-") ? "-" : playerHand.substring(0, 1));
        }
        model.addAttribute("allDeals", allDeals);
        model.addAttribute("allBidsTrimmed", allBidsTrimmed);
        model.addAttribute("finalBids", finalBids);
        model.addAttribute("dealers", dealers);
        model.addAttribute("playerHands", playerHands);
        return "past-deals";
    }

    @GetMapping("/current-deal-popup")
    public String currentDealPopup(Model model) {
        Deal deal = biddingService.getCurrentDeal();
        if (deal == null) {
            deal = biddingService.startNewDeal();
        }
        model.addAttribute("deal", deal);
        List<Map<String, List<Card>>> handsBySuit = new java.util.ArrayList<>();
        for (Hand h : deal.getHands()) {
            handsBySuit.add(h.getSortedCardsBySuitName());
        }
        model.addAttribute("handsBySuit", handsBySuit);
        model.addAttribute("suitsOrdered", java.util.List.of(
            com.example.bridge.model.Card.Suit.SPADES,
            com.example.bridge.model.Card.Suit.HEARTS,
            com.example.bridge.model.Card.Suit.DIAMONDS,
            com.example.bridge.model.Card.Suit.CLUBS
        ));
        return "current-deal-popup";
    }

    // Helper for Thymeleaf to render bid with suit icon
    private List<String[]> prepareBiddingTable(List<Bid> bids, Player dealer) {
        List<String[]> biddingRounds = new ArrayList<>();
        if (bids.isEmpty()) {
            return biddingRounds;
        }

        int dealerIndex = dealer.ordinal();
        int totalBids = bids.size();

        // Create a flat list representing the grid cells, initialized to ""
        List<String> grid = new ArrayList<>(java.util.Collections.nCopies((dealerIndex + totalBids + 3) / 4 * 4, ""));

        for (int i = 0; i < totalBids; i++) {
            Bid bid = bids.get(i);
            int playerIndex = (dealerIndex + i) % 4;
            int roundIndex = (dealerIndex + i) / 4;
            int gridIndex = roundIndex * 4 + playerIndex;
            grid.set(gridIndex, renderBidHtml(bid));
        }

        // Group the flat list into rounds of 4
        for (int i = 0; i < grid.size(); i += 4) {
            biddingRounds.add(grid.subList(i, i + 4).toArray(new String[0]));
        }

        return biddingRounds;
    }

    // Helper for Thymeleaf to render bid with suit icon
    public String renderBidHtml(Bid bid) {
        if (bid == null)
            return "-";
        if (bid.isPass())
            return "Pass";
        String suitIcon;
        switch (bid.getSuit()) {
            case SPADES:
                suitIcon = "<span style='color:black'>&#9824;</span>";
                break;
            case HEARTS:
                suitIcon = "<span style='color:red'>&#9829;</span>";
                break;
            case DIAMONDS:
                suitIcon = "<span style='color:red'>&#9830;</span>";
                break;
            case CLUBS:
                suitIcon = "<span style='color:black'>&#9827;</span>";
                break;
            case NOTRUMP:
                suitIcon = "NT";
                break;
            default:
                suitIcon = "";
        }
        return bid.getLevel() + " " + suitIcon;
    }
}

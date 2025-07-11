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
    public String index(@RequestParam(value = "biddingSystem", required = false) String biddingSystem, Model model) {
        if (biddingSystem != null) {
            biddingService.setBiddingSystem(biddingSystem);
        }
        Deal deal = biddingService.getCurrentDeal();
        if (deal == null) {
            deal = biddingService.startNewDeal();
        }
        // Debug: print number of cards in each hand
        int handIdx = 0;
        for (Hand h : deal.getHands()) {
            System.out.println("Hand " + handIdx + " (" + h.getPlayer() + ") has "
                    + (h.getCards() == null ? 0 : h.getCards().size()) + " cards: "
                    + (h.getCards() == null ? "null" : h.getCards()));
            handIdx++;
        }
        // Pick the current bidder's hand for the main screen
        int currentBidderIndex = biddingService.getCurrentBidderIndex();
        List<Hand> hands = deal.getHands();
        Hand hand = hands.get(currentBidderIndex);
        model.addAttribute("hand", hand);
        model.addAttribute("handIndex", currentBidderIndex);
        model.addAttribute("handBySuit", hand.getSortedCardsBySuitName());
        int totalCards = hand.getCards() == null ? 0 : hand.getCards().size();
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
        List<String[]> biddingRounds = new ArrayList<>();
        for (int i = 0; i < biddingHistory.size(); i += 4) {
            String[] round = new String[4];
            for (int j = 0; j < 4; j++) {
                int idx = i + j;
                if (idx < biddingHistory.size()) {
                    Bid bid = biddingHistory.get(idx);
                    if (bid.isPass()) {
                        round[j] = "Pass";
                    } else {
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
                        round[j] = bid.getLevel() + " " + suitIcon;
                    }
                } else {
                    round[j] = "-";
                }
            }
            biddingRounds.add(round);
        }
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
        model.addAttribute("biddingSystem", biddingService.getBiddingSystem());
        model.addAttribute("allDeals", biddingService.getAllDeals());
        model.addAttribute("suitsOrdered", java.util.List.of(
            com.example.bridge.model.Card.Suit.SPADES,
            com.example.bridge.model.Card.Suit.HEARTS,
            com.example.bridge.model.Card.Suit.DIAMONDS,
            com.example.bridge.model.Card.Suit.CLUBS
        ));
        return "index";
    }

    @PostMapping("/new-deal")
    public String newDeal() {
        biddingService.startNewDeal();
        return "redirect:/";
    }

    @PostMapping("/make-bid")
    public String makeBid(@RequestParam(required = false) Integer level,
            @RequestParam(required = false) Card.Suit suit,
            @RequestParam(required = false) String pass,
            @RequestParam(value = "biddingSystem", required = false) String biddingSystem,
                    Model model) {
        if (biddingSystem != null) {
            biddingService.setBiddingSystem(biddingSystem);
        }
        if (biddingService.isBiddingFinished()) {
            model.addAttribute("bidError", "Bidding is finished.");
            return index(null, model);
        }
        Bid bid;
        if (pass != null) {
            bid = Bid.pass();
        } else if (level != null && suit != null) {
            bid = new Bid(level, suit);
        } else {
            return "redirect:/";
        }
        if (!biddingService.isBidAllowed(bid)) {
            model.addAttribute("bidError", "Bid must be higher than previous bids.");
            return index(null, model);
        }
        biddingService.makeBid(bid);
        biddingService.saveDealIfFinished();
        return "redirect:/";
    }

    @GetMapping("/advice/{handIndex}")
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
        for (Deal deal : allDeals) {
            allBidsTrimmed.add(getBiddingWithoutTrailingPasses(deal.getBids()));
        }
        model.addAttribute("allDeals", allDeals);
        model.addAttribute("allBidsTrimmed", allBidsTrimmed);
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

package com.example.bridge.controller;

import com.example.bridge.model.*;
import com.example.bridge.service.BridgeBiddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow React dev server
public class BridgeBiddingRestController {

    @Autowired
    private BridgeBiddingService biddingService;

    @GetMapping("/game-state")
    public ResponseEntity<Map<String, Object>> getGameState(
            @RequestParam(value = "biddingSystem", required = false) String biddingSystem,
            @RequestParam(value = "trainingMode", required = false, defaultValue = "single") String trainingMode) {
        
        if (biddingSystem != null) {
            biddingService.setBiddingSystem(biddingSystem);
        }

        Deal deal = biddingService.getCurrentDeal();
        if (deal == null) {
            deal = biddingService.startNewDeal();
            // Auto bidding logic for single hand mode on first deal
            if ("single".equals(trainingMode)) {
                int userSeatIndex = biddingService.getUserSeat().ordinal();
                while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                    Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                    biddingService.makeBid(autoBid);
                }
            }
        }

        Map<String, Object> gameState = new HashMap<>();
        
        // Basic game info
        gameState.put("trainingMode", trainingMode);
        gameState.put("dealer", biddingService.getCurrentDealer());
        gameState.put("dealerIndex", biddingService.getCurrentDealer().ordinal());
        gameState.put("userSeat", biddingService.getUserSeat());
        gameState.put("userSeatIndex", biddingService.getUserSeat().ordinal());
        gameState.put("currentBidderIndex", biddingService.getCurrentBidderIndex());
        gameState.put("biddingFinished", biddingService.isBiddingFinished());
        gameState.put("biddingSystem", biddingService.getBiddingSystem());

        // Current hand info
        int currentBidderIndex = biddingService.getCurrentBidderIndex();
        List<Hand> hands = deal.getHands();
        Hand currentHand = hands.get(currentBidderIndex);
        
        Map<String, Object> handInfo = new HashMap<>();
        handInfo.put("player", currentHand.getPlayer());
        handInfo.put("cards", currentHand.getSortedCardsBySuitName());
        handInfo.put("ranks", currentHand.getSortedRankNamesBySuit());
        handInfo.put("highCardPoints", currentHand.getHighCardPoints());
        gameState.put("currentHand", handInfo);

        // All hands for popup (when needed)
        List<Map<String, Object>> allHands = new ArrayList<>();
        for (Hand hand : hands) {
            Map<String, Object> handData = new HashMap<>();
            handData.put("player", hand.getPlayer());
            handData.put("cards", hand.getSortedCardsBySuitName());
            handData.put("ranks", hand.getSortedRankNamesBySuit());
            handData.put("highCardPoints", hand.getHighCardPoints());
            allHands.add(handData);
        }
        gameState.put("allHands", allHands);

        // Bidding history
        List<Bid> biddingHistory = biddingService.getBiddingHistory();
        List<Map<String, Object>> bids = new ArrayList<>();
        for (Bid bid : biddingHistory) {
            Map<String, Object> bidData = new HashMap<>();
            bidData.put("player", bid.getPlayer());
            bidData.put("level", bid.getLevel());
            bidData.put("suit", bid.getSuit());
            bidData.put("bidType", bid.getBidType());
            bidData.put("isPass", bid.isPass());
            bidData.put("isDouble", bid.isDouble());
            bidData.put("isRedouble", bid.isRedouble());
            bidData.put("displayText", renderBidText(bid));
            bids.add(bidData);
        }
        gameState.put("biddingHistory", bids);

        // Bidding table structure
        gameState.put("biddingTable", prepareBiddingTable(biddingHistory, biddingService.getCurrentDealer()));

        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/new-deal")
    public ResponseEntity<Map<String, Object>> startNewDeal(
            @RequestParam(value = "trainingMode", required = false, defaultValue = "single") String trainingMode) {
        
        biddingService.startNewDeal();
        
        // Auto bidding for single hand mode
        if ("single".equals(trainingMode)) {
            int userSeatIndex = biddingService.getUserSeat().ordinal();
            while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                biddingService.makeBid(autoBid);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "New deal started");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/make-bid")
    public ResponseEntity<Map<String, Object>> makeBid(@RequestBody Map<String, Object> bidRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Bid bid;
            
            if (bidRequest.containsKey("pass") && (Boolean) bidRequest.get("pass")) {
                bid = Bid.pass();
            } else if (bidRequest.containsKey("double") && (Boolean) bidRequest.get("double")) {
                bid = Bid.doubleBid();
            } else if (bidRequest.containsKey("redouble") && (Boolean) bidRequest.get("redouble")) {
                bid = Bid.redoubleBid();
            } else {
                Integer level = (Integer) bidRequest.get("level");
                String suitStr = (String) bidRequest.get("suit");
                Card.Suit suit = Card.Suit.valueOf(suitStr);
                bid = new Bid(level, suit);
            }

            if (biddingService.isBidAllowed(bid)) {
                biddingService.makeBid(bid);
                
                // Auto bidding for other players in single hand mode
                String trainingMode = (String) bidRequest.getOrDefault("trainingMode", "single");
                if ("single".equals(trainingMode)) {
                    int userSeatIndex = biddingService.getUserSeat().ordinal();
                    while (!biddingService.isBiddingFinished() && biddingService.getCurrentBidderIndex() != userSeatIndex) {
                        Bid autoBid = biddingService.getSimpleNaturalBid(biddingService.getBiddingHistory());
                        biddingService.makeBid(autoBid);
                    }
                }

                response.put("success", true);
                response.put("message", "Bid made successfully");
                
                if (biddingService.isBiddingFinished()) {
                    biddingService.saveDealIfFinished();
                    response.put("biddingFinished", true);
                }
            } else {
                response.put("success", false);
                response.put("message", "Bid not allowed");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error making bid: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/past-deals")
    public ResponseEntity<Map<String, Object>> getPastDeals() {
        List<Deal> allDeals = biddingService.getAllDeals();
        
        List<Map<String, Object>> dealData = new ArrayList<>();
        for (Deal deal : allDeals) {
            Map<String, Object> dealInfo = new HashMap<>();
            dealInfo.put("dealer", deal.getDealer());
            dealInfo.put("contract", deal.getContract());
            dealInfo.put("biddingSystem", deal.getBiddingSystem());
            
            // Get bidding history without trailing passes
            List<Bid> bids = getBiddingWithoutTrailingPasses(deal.getBids());
            List<Map<String, Object>> bidData = new ArrayList<>();
            for (Bid bid : bids) {
                Map<String, Object> bidInfo = new HashMap<>();
                bidInfo.put("player", bid.getPlayer());
                bidInfo.put("displayText", renderBidText(bid));
                bidData.add(bidInfo);
            }
            dealInfo.put("bids", bidData);
            
            // Final bid
            if (!bids.isEmpty()) {
                dealInfo.put("finalBid", renderBidText(bids.get(bids.size() - 1)));
            } else {
                dealInfo.put("finalBid", "All Pass");
            }
            
            dealData.add(dealInfo);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("deals", dealData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/advice/{handIndex}")
    public ResponseEntity<Map<String, Object>> getAdvice(@PathVariable int handIndex) {
        Deal deal = biddingService.getCurrentDeal();
        if (deal == null || handIndex < 0 || handIndex >= deal.getHands().size()) {
            Map<String, Object> response = new HashMap<>();
            response.put("advice", "No advice available");
            return ResponseEntity.ok(response);
        }
        
        Hand hand = deal.getHands().get(handIndex);
        String advice = biddingService.getAdvice(hand, biddingService.getBiddingHistory());
        
        Map<String, Object> response = new HashMap<>();
        response.put("advice", advice);
        return ResponseEntity.ok(response);
    }

    // Helper methods
    private String renderBidText(Bid bid) {
        if (bid == null) return "-";
        if (bid.isPass()) return "Pass";
        if (bid.isDouble()) return "Double";
        if (bid.isRedouble()) return "Redouble";
        
        if (bid.getSuit() == null) return "Unknown";
        
        String suitSymbol;
        switch (bid.getSuit()) {
            case SPADES: suitSymbol = "♠"; break;
            case HEARTS: suitSymbol = "♥"; break;
            case DIAMONDS: suitSymbol = "♦"; break;
            case CLUBS: suitSymbol = "♣"; break;
            case NOTRUMP: suitSymbol = "NT"; break;
            default: suitSymbol = "";
        }
        return bid.getLevel() + suitSymbol;
    }

    private List<String[]> prepareBiddingTable(List<Bid> bids, Player dealer) {
        List<String[]> biddingRounds = new ArrayList<>();
        if (bids.isEmpty()) {
            return biddingRounds;
        }

        int dealerIndex = dealer.ordinal();
        int totalBids = bids.size();

        // Create a flat list representing the grid cells
        List<String> grid = new ArrayList<>();
        for (int i = 0; i < (dealerIndex + totalBids + 3) / 4 * 4; i++) {
            grid.add("");
        }

        for (int i = 0; i < totalBids; i++) {
            Bid bid = bids.get(i);
            int playerIndex = (dealerIndex + i) % 4;
            int roundIndex = (dealerIndex + i) / 4;
            int gridIndex = roundIndex * 4 + playerIndex;
            if (gridIndex < grid.size()) {
                grid.set(gridIndex, renderBidText(bid));
            }
        }

        // Group the flat list into rounds of 4
        for (int i = 0; i < grid.size(); i += 4) {
            int endIndex = Math.min(i + 4, grid.size());
            biddingRounds.add(grid.subList(i, endIndex).toArray(new String[0]));
        }

        return biddingRounds;
    }

    private List<Bid> getBiddingWithoutTrailingPasses(List<Bid> bids) {
        if (bids == null || bids.size() < 3) {
            return bids != null ? new ArrayList<>(bids) : new ArrayList<>();
        }
        
        List<Bid> result = new ArrayList<>(bids);
        while (result.size() >= 3 && 
               result.get(result.size() - 1).isPass() &&
               result.get(result.size() - 2).isPass() &&
               result.get(result.size() - 3).isPass()) {
            result.remove(result.size() - 1);
            result.remove(result.size() - 1);
            result.remove(result.size() - 1);
        }
        return result;
    }
}

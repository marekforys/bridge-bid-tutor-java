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
        model.addAttribute("deal", deal);
        model.addAttribute("biddingHistory", biddingService.getBiddingHistory());
        // Add hands grouped by suit for template
        List<Map<String, List<Card>>> handsBySuit = new ArrayList<>();
        for (Hand hand : deal.getHands()) {
            handsBySuit.add(hand.getSortedCardsBySuitName());
        }
        model.addAttribute("handsBySuit", handsBySuit);
        model.addAttribute("currentBidderIndex", biddingService.getCurrentBidderIndex());
        model.addAttribute("currentBidder",
                com.example.bridge.model.Player.values()[biddingService.getCurrentBidderIndex()]);
        model.addAttribute("biddingFinished", biddingService.isBiddingFinished());
        model.addAttribute("biddingSystem", biddingService.getBiddingSystem());
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
            bid = new Bid();
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
}

package com.example.bridge.controller;

import com.example.bridge.model.*;
import com.example.bridge.service.BridgeBiddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BridgeBiddingController.class)
public class BridgeBiddingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BridgeBiddingService bridgeBiddingService;

    private String renderBidHtml(Bid bid) {
        if (bid.isPass()) return "Pass";
        String suitIcon;
        switch (bid.getSuit()) {
            case SPADES: suitIcon = "<span style='color:black'>&#9824;</span>"; break;
            case HEARTS: suitIcon = "<span style='color:red'>&#9829;</span>"; break;
            case DIAMONDS: suitIcon = "<span style='color:red'>&#9830;</span>"; break;
            case CLUBS: suitIcon = "<span style='color:black'>&#9827;</span>"; break;
            case NOTRUMP: suitIcon = "NT"; break;
            default: suitIcon = "";
        }
        return bid.getLevel() + " " + suitIcon;
    }

    @Test
    void testBiddingTable_WhenDealerIsEast() throws Exception {
        // Given
        Player dealer = Player.EAST;
        Bid bid1 = new Bid(1, Card.Suit.CLUBS);
        Bid bid2 = Bid.pass();
        Bid bid3 = new Bid(1, Card.Suit.HEARTS);
        Bid bid4 = Bid.pass();
        Bid bid5 = Bid.pass();
        List<Bid> bids = List.of(bid1, bid2, bid3, bid4, bid5);

        Deal mockDeal = new Deal();
        mockDeal.setDealer(dealer);
        Hand northHand = new Hand(new java.util.ArrayList<>());
        northHand.setPlayer(Player.NORTH);
        Hand eastHand = new Hand(new java.util.ArrayList<>());
        eastHand.setPlayer(Player.EAST);
        Hand southHand = new Hand(new java.util.ArrayList<>());
        southHand.setPlayer(Player.SOUTH);
        Hand westHand = new Hand(new java.util.ArrayList<>());
        westHand.setPlayer(Player.WEST);
        mockDeal.setHands(List.of(northHand, eastHand, southHand, westHand));

        when(bridgeBiddingService.getCurrentDeal()).thenReturn(mockDeal);
        when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(1);

        // Expected biddingRounds structure
        List<String[]> expectedRounds = List.of(
            new String[]{"", renderBidHtml(bid1), renderBidHtml(bid2), renderBidHtml(bid3)},
            new String[]{renderBidHtml(bid4), renderBidHtml(bid5), "", ""}
        );

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.contains(
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(0)),
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(1))
            )));
    }

    @Test
    void testBiddingTable_WhenDealerIsNorth() throws Exception {
        // Given
        Player dealer = Player.NORTH;
        Bid bid1 = new Bid(1, Card.Suit.CLUBS);
        Bid bid2 = Bid.pass();
        Bid bid3 = new Bid(1, Card.Suit.HEARTS);
        Bid bid4 = Bid.pass();
        Bid bid5 = Bid.pass();
        List<Bid> bids = List.of(bid1, bid2, bid3, bid4, bid5);

        Deal mockDeal = new Deal();
        mockDeal.setDealer(dealer);
        Hand northHand = new Hand(new java.util.ArrayList<>());
        northHand.setPlayer(Player.NORTH);
        Hand eastHand = new Hand(new java.util.ArrayList<>());
        eastHand.setPlayer(Player.EAST);
        Hand southHand = new Hand(new java.util.ArrayList<>());
        southHand.setPlayer(Player.SOUTH);
        Hand westHand = new Hand(new java.util.ArrayList<>());
        westHand.setPlayer(Player.WEST);
        mockDeal.setHands(List.of(northHand, eastHand, southHand, westHand));

        when(bridgeBiddingService.getCurrentDeal()).thenReturn(mockDeal);
        when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(0);

        // Expected biddingRounds structure
        List<String[]> expectedRounds = List.of(
            new String[]{renderBidHtml(bid1), renderBidHtml(bid2), renderBidHtml(bid3), renderBidHtml(bid4)},
            new String[]{renderBidHtml(bid5), "", "", ""}
        );

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.contains(
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(0)),
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(1))
            )));
    }

    @Test
    void testBiddingTable_WhenDealerIsSouth() throws Exception {
        // Given
        Player dealer = Player.SOUTH;
        Bid bid1 = new Bid(1, Card.Suit.CLUBS);
        Bid bid2 = Bid.pass();
        Bid bid3 = new Bid(1, Card.Suit.HEARTS);
        Bid bid4 = Bid.pass();
        Bid bid5 = Bid.pass();
        List<Bid> bids = List.of(bid1, bid2, bid3, bid4, bid5);

        Deal mockDeal = new Deal();
        mockDeal.setDealer(dealer);
        Hand northHand = new Hand(new java.util.ArrayList<>());
        northHand.setPlayer(Player.NORTH);
        Hand eastHand = new Hand(new java.util.ArrayList<>());
        eastHand.setPlayer(Player.EAST);
        Hand southHand = new Hand(new java.util.ArrayList<>());
        southHand.setPlayer(Player.SOUTH);
        Hand westHand = new Hand(new java.util.ArrayList<>());
        westHand.setPlayer(Player.WEST);
        mockDeal.setHands(List.of(northHand, eastHand, southHand, westHand));

        when(bridgeBiddingService.getCurrentDeal()).thenReturn(mockDeal);
        when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(2);

        // Expected biddingRounds structure
        List<String[]> expectedRounds = List.of(
            new String[]{"", "", renderBidHtml(bid1), renderBidHtml(bid2)},
            new String[]{renderBidHtml(bid3), renderBidHtml(bid4), renderBidHtml(bid5), ""}
        );

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.contains(
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(0)),
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(1))
            )));
    }

    @Test
    void testBiddingTable_WhenDealerIsWest() throws Exception {
        // Given
        Player dealer = Player.WEST;
        Bid bid1 = new Bid(1, Card.Suit.CLUBS);
        Bid bid2 = Bid.pass();
        Bid bid3 = new Bid(1, Card.Suit.HEARTS);
        Bid bid4 = Bid.pass();
        Bid bid5 = Bid.pass();
        List<Bid> bids = List.of(bid1, bid2, bid3, bid4, bid5);

        Deal mockDeal = new Deal();
        mockDeal.setDealer(dealer);
        Hand northHand = new Hand(new java.util.ArrayList<>());
        northHand.setPlayer(Player.NORTH);
        Hand eastHand = new Hand(new java.util.ArrayList<>());
        eastHand.setPlayer(Player.EAST);
        Hand southHand = new Hand(new java.util.ArrayList<>());
        southHand.setPlayer(Player.SOUTH);
        Hand westHand = new Hand(new java.util.ArrayList<>());
        westHand.setPlayer(Player.WEST);
        mockDeal.setHands(List.of(northHand, eastHand, southHand, westHand));

        when(bridgeBiddingService.getCurrentDeal()).thenReturn(mockDeal);
        when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(3);

        // Expected biddingRounds structure
        List<String[]> expectedRounds = List.of(
            new String[]{"", "", "", renderBidHtml(bid1)},
            new String[]{renderBidHtml(bid2), renderBidHtml(bid3), renderBidHtml(bid4), renderBidHtml(bid5)}
        );

        // When & Then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(model().attribute("biddingRounds", org.hamcrest.Matchers.contains(
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(0)),
                org.hamcrest.Matchers.arrayContaining(expectedRounds.get(1))
            )));
    }
}

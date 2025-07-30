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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void testNewDeal() throws Exception {
        // Given
        Deal mockDeal = new Deal();
        when(biddingService.startNewDeal()).thenReturn(mockDeal);

        // When & Then
        mockMvc.perform(post("/new-deal"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(biddingService, times(1)).startNewDeal();
    }

    @Test
    void testMakeBid() throws Exception {
        // Given
        Bid userBid = new Bid(1, Card.Suit.SPADES);

        // When & Then
        mockMvc.perform(post("/make-bid")
                .param("level", "1")
                .param("suit", "SPADES"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(biddingService, times(1)).makeBid(userBid);
    }

    @Test
    void testMakeBid_Pass() throws Exception {
        // Given
        Bid passBid = Bid.pass();

        // When & Then
        mockMvc.perform(post("/make-bid")
                .param("pass", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(biddingService, times(1)).makeBid(passBid);
    }

    @Test
    void testPastDeals() throws Exception {
        // Given
        Deal deal1 = new Deal();
        deal1.setDealer(Player.NORTH);
        deal1.setBids(List.of(new Bid(1, Card.Suit.CLUBS), Bid.pass(), Bid.pass(), Bid.pass()));
        Hand southHand = new Hand(Player.SOUTH, new ArrayList<>());
        deal1.setHands(List.of(new Hand(Player.NORTH, new ArrayList<>()), new Hand(Player.EAST, new ArrayList<>()), southHand, new Hand(Player.WEST, new ArrayList<>())));

        List<Deal> mockDeals = List.of(deal1);
        when(biddingService.getAllDeals()).thenReturn(mockDeals);

        // When & Then
        mockMvc.perform(get("/past-deals"))
            .andExpect(status().isOk())
            .andExpect(view().name("past-deals"))
            .andExpect(model().attributeExists("allDeals", "finalBids", "dealers"));
    }

    @Test
    void testCurrentDealPopup() throws Exception {
        // Given
        Deal mockDeal = new Deal();
        Hand northHand = new Hand(Player.NORTH, new ArrayList<>());
        Hand eastHand = new Hand(Player.EAST, new ArrayList<>());
        Hand southHand = new Hand(Player.SOUTH, new ArrayList<>());
        Hand westHand = new Hand(Player.WEST, new ArrayList<>());
        mockDeal.setHands(List.of(northHand, eastHand, southHand, westHand));

        when(biddingService.getCurrentDeal()).thenReturn(mockDeal);

        // When & Then
        mockMvc.perform(get("/current-deal-popup"))
            .andExpect(status().isOk())
            .andExpect(view().name("current-deal-popup"))
            .andExpect(model().attributeExists("deal", "handsBySuit"));
    }
}

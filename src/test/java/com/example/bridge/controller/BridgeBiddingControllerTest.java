package com.example.bridge.controller;

import com.example.bridge.model.*;
import com.example.bridge.service.BridgeBiddingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BridgeBiddingController.class)
public class BridgeBiddingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BridgeBiddingService bridgeBiddingService;

    private Deal mockDeal;
    private List<Hand> hands;

    @BeforeEach
    void setUp() {
        setupMockDealWithShuffledHands();
    }

    private void setupMockDealWithShuffledHands() {
        mockDeal = new Deal();
        hands = new ArrayList<>();
        List<Card> deck = Card.getShuffledDeck();
        for (int i = 0; i < 4; i++) {
            Hand hand = new Hand(new ArrayList<>(deck.subList(i * 13, (i + 1) * 13)), Player.values()[i]);
            hands.add(hand);
        }
        mockDeal.setHands(hands);
        when(bridgeBiddingService.getCurrentDeal()).thenReturn(mockDeal);
        when(bridgeBiddingService.getHandForPlayer(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            return hands.stream().filter(h -> h.getPlayer() == player).findFirst().orElse(null);
        });
    }

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

        // Use the properly initialized mock deal from @BeforeEach
        mockDeal.setDealer(dealer);
        Mockito.when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        Mockito.when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(1);

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

        // Use the properly initialized mock deal from @BeforeEach
        mockDeal.setDealer(dealer);
        Mockito.when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        Mockito.when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(0);

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

        // Use the properly initialized mock deal from @BeforeEach
        mockDeal.setDealer(dealer);
        Mockito.when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        Mockito.when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(2);

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

        // Use the properly initialized mock deal from @BeforeEach
        mockDeal.setDealer(dealer);
        Mockito.when(bridgeBiddingService.getBiddingHistory()).thenReturn(bids);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(dealer);
        Mockito.when(bridgeBiddingService.isBiddingFinished()).thenReturn(false);
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(3);

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
        Deal newMockDeal = new Deal();
        Mockito.when(bridgeBiddingService.startNewDeal()).thenReturn(newMockDeal);
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);

        // When & Then
        mockMvc.perform(post("/new-deal"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(bridgeBiddingService, times(1)).startNewDeal();
    }

    @Test
    void testMakeBid() throws Exception {
        // Given
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(0);
        Bid userBid = new Bid(1, Card.Suit.SPADES);
        Mockito.when(bridgeBiddingService.isBidAllowed(userBid)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/make-bid")
                .param("level", "1")
                .param("suit", "SPADES"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(bridgeBiddingService, times(1)).makeBid(userBid);
    }

    @Test
    void testMakeBid_Pass() throws Exception {
        // Given
        Mockito.when(bridgeBiddingService.getUserSeat()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentDealer()).thenReturn(Player.NORTH);
        Mockito.when(bridgeBiddingService.getCurrentBidderIndex()).thenReturn(0);
        Bid passBid = Bid.pass();
        Mockito.when(bridgeBiddingService.isBidAllowed(passBid)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/make-bid")
                .param("pass", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(bridgeBiddingService, times(1)).makeBid(passBid);
    }

    @Test
    void testPastDeals() throws Exception {
        // Given
        Deal deal1 = new Deal();
        deal1.setDealer(Player.NORTH);
        deal1.setBids(List.of(new Bid(1, Card.Suit.CLUBS), Bid.pass(), Bid.pass(), Bid.pass()));
        // Use properly initialized hands instead of empty ones
        deal1.setHands(hands);

        List<Deal> mockDeals = List.of(deal1);
        Mockito.when(bridgeBiddingService.getAllDeals()).thenReturn(mockDeals);

        // When & Then
        mockMvc.perform(get("/past-deals"))
            .andExpect(status().isOk())
            .andExpect(view().name("past-deals"))
            .andExpect(model().attributeExists("allDeals", "finalBids", "dealers"));
    }

    @Test
    void testCurrentDealPopup() throws Exception {
        // When & Then
        mockMvc.perform(get("/current-deal-popup"))
            .andExpect(status().isOk())
            .andExpect(view().name("current-deal-popup"))
            .andExpect(model().attributeExists("deal", "handsBySuit"));
    }
}

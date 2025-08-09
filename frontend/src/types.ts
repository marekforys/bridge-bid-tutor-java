export interface Player {
  name: string;
  ordinal: number;
}

export interface Card {
  suit: string;
  rank: string;
}

export interface Bid {
  player: string;
  level: number;
  suit: string;
  bidType: string;
  isPass: boolean;
  isDouble: boolean;
  isRedouble: boolean;
  displayText: string;
}

export interface Hand {
  player: string;
  cards: { [suit: string]: Card[] };
  ranks: { [suit: string]: string[] };
  highCardPoints: number;
}

export interface GameState {
  trainingMode: string;
  dealer: string;
  dealerIndex: number;
  userSeat: string;
  userSeatIndex: number;
  currentBidderIndex: number;
  biddingFinished: boolean;
  biddingSystem: string;
  currentHand: Hand;
  allHands: Hand[];
  biddingHistory: Bid[];
  biddingTable: string[][];
}

export interface Deal {
  dealer: string;
  contract: string;
  biddingSystem: string;
  bids: Bid[];
  finalBid: string;
}

export interface BidRequest {
  level?: number;
  suit?: string;
  pass?: boolean;
  double?: boolean;
  redouble?: boolean;
  trainingMode?: string;
}

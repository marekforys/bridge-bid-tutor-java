import { GameState, Deal, BidRequest } from './types';

const API_BASE_URL = '/api';

export class BridgeAPI {
  static async getGameState(biddingSystem?: string, trainingMode?: string): Promise<GameState> {
    const params = new URLSearchParams();
    if (biddingSystem) params.append('biddingSystem', biddingSystem);
    if (trainingMode) params.append('trainingMode', trainingMode);
    
    const response = await fetch(`${API_BASE_URL}/game-state?${params}`);
    if (!response.ok) {
      throw new Error('Failed to fetch game state');
    }
    return response.json();
  }

  static async startNewDeal(trainingMode?: string): Promise<{ success: boolean; message: string }> {
    const params = new URLSearchParams();
    if (trainingMode) params.append('trainingMode', trainingMode);
    
    const response = await fetch(`${API_BASE_URL}/new-deal?${params}`, {
      method: 'POST',
    });
    if (!response.ok) {
      throw new Error('Failed to start new deal');
    }
    return response.json();
  }

  static async makeBid(bidRequest: BidRequest): Promise<{ success: boolean; message: string; biddingFinished?: boolean }> {
    const response = await fetch(`${API_BASE_URL}/make-bid`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(bidRequest),
    });
    if (!response.ok) {
      throw new Error('Failed to make bid');
    }
    return response.json();
  }

  static async getPastDeals(): Promise<{ deals: Deal[] }> {
    const response = await fetch(`${API_BASE_URL}/past-deals`);
    if (!response.ok) {
      throw new Error('Failed to fetch past deals');
    }
    return response.json();
  }

  static async getAdvice(handIndex: number): Promise<{ advice: string }> {
    const response = await fetch(`${API_BASE_URL}/advice/${handIndex}`);
    if (!response.ok) {
      throw new Error('Failed to fetch advice');
    }
    return response.json();
  }
}

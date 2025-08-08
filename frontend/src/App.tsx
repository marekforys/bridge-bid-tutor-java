import React, { useState, useEffect } from 'react';
import { GameState, BidRequest } from './types';
import { BridgeAPI } from './api';
import BiddingTable from './components/BiddingTable';
import HandDisplay from './components/HandDisplay';
import BiddingControls from './components/BiddingControls';
import GameControls from './components/GameControls';
import PastDeals from './components/PastDeals';
import './App.css';

const App: React.FC = () => {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<'game' | 'past-deals'>('game');
  const [trainingMode, setTrainingMode] = useState<string>('single');
  const [biddingSystem, setBiddingSystem] = useState<string>('2/1 Game Forcing');

  useEffect(() => {
    loadGameState();
  }, []);

  const loadGameState = async () => {
    try {
      setLoading(true);
      const state = await BridgeAPI.getGameState(biddingSystem, trainingMode);
      setGameState(state);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load game state');
    } finally {
      setLoading(false);
    }
  };

  const handleNewDeal = async () => {
    try {
      await BridgeAPI.startNewDeal(trainingMode);
      await loadGameState();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start new deal');
    }
  };

  const handleMakeBid = async (bidRequest: BidRequest) => {
    try {
      const response = await BridgeAPI.makeBid({ ...bidRequest, trainingMode });
      if (response.success) {
        await loadGameState();
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to make bid');
    }
  };

  const handleBiddingSystemChange = async (newSystem: string) => {
    setBiddingSystem(newSystem);
    try {
      const state = await BridgeAPI.getGameState(newSystem, trainingMode);
      setGameState(state);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update bidding system');
    }
  };

  const handleTrainingModeChange = async (newMode: string) => {
    setTrainingMode(newMode);
    try {
      const state = await BridgeAPI.getGameState(biddingSystem, newMode);
      setGameState(state);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update training mode');
    }
  };

  if (loading) {
    return (
      <div className="app">
        <div className="loading">Loading Bridge Bidding Tutor...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="app">
        <div className="error">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={loadGameState} className="btn btn-primary">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="app">
      <header className="header">
        <div className="container">
          <h1>Bridge Bidding Tutor</h1>
          <nav className="nav">
            <button
              className={`nav-btn ${currentView === 'game' ? 'active' : ''}`}
              onClick={() => setCurrentView('game')}
            >
              Current Game
            </button>
            <button
              className={`nav-btn ${currentView === 'past-deals' ? 'active' : ''}`}
              onClick={() => setCurrentView('past-deals')}
            >
              Past Deals
            </button>
          </nav>
        </div>
      </header>

      <main className="main">
        <div className="container">
          {currentView === 'game' && gameState && (
            <div className="game-view">
              <div className="main-grid">
                <div className="left-panel">
                  <GameControls
                    trainingMode={trainingMode}
                    biddingSystem={biddingSystem}
                    onTrainingModeChange={handleTrainingModeChange}
                    onBiddingSystemChange={handleBiddingSystemChange}
                    onNewDeal={handleNewDeal}
                  />
                  
                  <HandDisplay
                    hand={gameState.currentHand}
                    isCurrentPlayer={gameState.currentBidderIndex === gameState.userSeat}
                  />

                  {!gameState.biddingFinished && (
                    <BiddingControls
                      gameState={gameState}
                      onMakeBid={handleMakeBid}
                    />
                  )}
                </div>

                <div className="right-panel">
                  <BiddingTable
                    biddingHistory={gameState.biddingHistory}
                    biddingTable={gameState.biddingTable}
                    dealer={gameState.dealer}
                    currentBidder={gameState.currentBidderIndex}
                    biddingFinished={gameState.biddingFinished}
                  />
                </div>
              </div>
            </div>
          )}

          {currentView === 'past-deals' && (
            <PastDeals />
          )}
        </div>
      </main>
    </div>
  );
};

export default App;

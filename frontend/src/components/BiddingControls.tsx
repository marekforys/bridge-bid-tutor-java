import React, { useState } from 'react';
import { GameState, BidRequest } from '../types';

interface BiddingControlsProps {
  gameState: GameState;
  onMakeBid: (bidRequest: BidRequest) => void;
}

const BiddingControls: React.FC<BiddingControlsProps> = ({ gameState, onMakeBid }) => {
  const [selectedLevel, setSelectedLevel] = useState<number>(1);
  const [selectedSuit, setSelectedSuit] = useState<string>('CLUBS');

  const suits = [
    { value: 'CLUBS', symbol: '♣', color: '#000000' },
    { value: 'DIAMONDS', symbol: '♦', color: '#dc2626' },
    { value: 'HEARTS', symbol: '♥', color: '#dc2626' },
    { value: 'SPADES', symbol: '♠', color: '#000000' },
    { value: 'NOTRUMP', symbol: 'NT', color: '#000000' }
  ];

  const levels = [1, 2, 3, 4, 5, 6, 7];

  const handleBid = () => {
    onMakeBid({
      level: selectedLevel,
      suit: selectedSuit
    });
  };

  const handlePass = () => {
    onMakeBid({ pass: true });
  };

  const handleDouble = () => {
    onMakeBid({ double: true });
  };

  const handleRedouble = () => {
    onMakeBid({ redouble: true });
  };

  const isUserTurn = gameState.currentBidderIndex === gameState.userSeatIndex;
  const canDouble = gameState.biddingHistory.length > 0 && 
    !gameState.biddingHistory[gameState.biddingHistory.length - 1].isPass;
  const canRedouble = gameState.biddingHistory.length > 0 && 
    gameState.biddingHistory[gameState.biddingHistory.length - 1].isDouble;

  if (!isUserTurn) {
    return (
      <div className="bidding-controls disabled">
        <div className="waiting-message">
          <p>Waiting for other players...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bidding-controls">
      <h3>Make Your Bid</h3>
      
      <div className="bid-selection">
        <div className="level-selection">
          <label className="form-label">Level:</label>
          <div className="level-buttons">
            {levels.map(level => (
              <button
                key={level}
                className={`level-btn ${selectedLevel === level ? 'selected' : ''}`}
                onClick={() => setSelectedLevel(level)}
              >
                {level}
              </button>
            ))}
          </div>
        </div>

        <div className="suit-selection">
          <label className="form-label">Suit:</label>
          <div className="suit-buttons">
            {suits.map(suit => (
              <button
                key={suit.value}
                className={`suit-btn ${selectedSuit === suit.value ? 'selected' : ''}`}
                onClick={() => setSelectedSuit(suit.value)}
                style={{ color: suit.color }}
              >
                <span className="suit-symbol">{suit.symbol}</span>
                <span className="suit-name">{suit.value}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="bid-actions">
        <button 
          className="btn btn-primary bid-btn"
          onClick={handleBid}
        >
          Bid {selectedLevel}
          <span style={{ color: suits.find(s => s.value === selectedSuit)?.color }}>
            {suits.find(s => s.value === selectedSuit)?.symbol}
          </span>
        </button>

        <button 
          className="btn btn-secondary"
          onClick={handlePass}
        >
          Pass
        </button>

        {canDouble && (
          <button 
            className="btn btn-warning"
            onClick={handleDouble}
          >
            Double
          </button>
        )}

        {canRedouble && (
          <button 
            className="btn btn-danger"
            onClick={handleRedouble}
          >
            Redouble
          </button>
        )}
      </div>

      <div className="bidding-help">
        <p className="help-text">
          Current bidding system: <strong>{gameState.biddingSystem}</strong>
        </p>
      </div>
    </div>
  );
};

export default BiddingControls;

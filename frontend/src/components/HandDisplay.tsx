import React from 'react';
import { Hand } from '../types';

interface HandDisplayProps {
  hand: Hand;
  isCurrentPlayer: boolean;
}

const HandDisplay: React.FC<HandDisplayProps> = ({ hand, isCurrentPlayer }) => {
  const suitOrder = ['SPADES', 'HEARTS', 'DIAMONDS', 'CLUBS'];
  const suitSymbols: { [key: string]: string } = {
    'SPADES': '♠',
    'HEARTS': '♥',
    'DIAMONDS': '♦',
    'CLUBS': '♣'
  };

  const suitColors: { [key: string]: string } = {
    'SPADES': '#000000',
    'HEARTS': '#dc2626',
    'DIAMONDS': '#dc2626',
    'CLUBS': '#000000'
  };

  const formatCards = (cards: string[]): string => {
    if (!cards || cards.length === 0) return '-';
    return cards.join(' ');
  };

  return (
    <div className={`hand-display ${isCurrentPlayer ? 'current-player' : ''}`}>
      <div className="hand-header">
        <h3>{hand.player}'s Hand</h3>
        <div className="hcp-display">
          <span className="hcp-label">HCP:</span>
          <span className="hcp-value">{hand.highCardPoints}</span>
        </div>
      </div>

      <div className="suits-container">
        {suitOrder.map(suit => (
          <div key={suit} className="suit-line">
            <span 
              className="suit-symbol"
              style={{ color: suitColors[suit] }}
            >
              {suitSymbols[suit]}
            </span>
            <span className="suit-cards">
              {formatCards(hand.ranks[suit] || [])}
            </span>
            <span className="suit-count">
              ({(hand.ranks[suit] || []).length})
            </span>
          </div>
        ))}
      </div>

      {isCurrentPlayer && (
        <div className="current-player-indicator">
          <span className="indicator-text">Your Turn</span>
        </div>
      )}
    </div>
  );
};

export default HandDisplay;

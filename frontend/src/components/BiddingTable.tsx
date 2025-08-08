import React from 'react';
import { Bid } from '../types';

interface BiddingTableProps {
  biddingHistory: Bid[];
  biddingTable: string[][];
  dealer: string;
  currentBidder: number;
  biddingFinished: boolean;
}

const BiddingTable: React.FC<BiddingTableProps> = ({
  biddingHistory,
  biddingTable,
  dealer,
  currentBidder,
  biddingFinished
}) => {
  const playerNames = ['North', 'East', 'South', 'West'];
  
  const getSuitSymbol = (text: string): string => {
    return text
      .replace(/♠/g, '<span style="color: black;">♠</span>')
      .replace(/♥/g, '<span style="color: red;">♥</span>')
      .replace(/♦/g, '<span style="color: red;">♦</span>')
      .replace(/♣/g, '<span style="color: black;">♣</span>');
  };

  return (
    <div className="bidding-table-container">
      <div className="bidding-info">
        <div className="dealer-info">
          <strong>Dealer:</strong> {dealer}
        </div>
        <div className="bidding-status">
          {biddingFinished ? (
            <span className="status-finished">Bidding Complete</span>
          ) : (
            <span className="status-active">Current Bidder: {playerNames[currentBidder]}</span>
          )}
        </div>
      </div>

      <div className="bidding-history">
        <h3>Bidding History</h3>
        <table className="bidding-table">
          <thead>
            <tr>
              <th>North</th>
              <th>East</th>
              <th>South</th>
              <th>West</th>
            </tr>
          </thead>
          <tbody>
            {biddingTable.map((round, roundIndex) => (
              <tr key={roundIndex}>
                {round.map((bid, bidIndex) => (
                  <td 
                    key={bidIndex}
                    className={bid ? 'has-bid' : 'empty-bid'}
                    dangerouslySetInnerHTML={{ __html: getSuitSymbol(bid || '-') }}
                  />
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {biddingHistory.length > 0 && (
        <div className="recent-bids">
          <h4>Recent Bids</h4>
          <div className="recent-bids-list">
            {biddingHistory.slice(-6).map((bid, index) => (
              <div key={index} className="recent-bid">
                <span className="bid-player">{bid.player}:</span>
                <span 
                  className="bid-text"
                  dangerouslySetInnerHTML={{ __html: getSuitSymbol(bid.displayText) }}
                />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default BiddingTable;

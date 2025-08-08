import React, { useState, useEffect } from 'react';
import { Deal } from '../types';
import { BridgeAPI } from '../api';

const PastDeals: React.FC = () => {
  const [deals, setDeals] = useState<Deal[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPastDeals();
  }, []);

  const loadPastDeals = async () => {
    try {
      setLoading(true);
      const response = await BridgeAPI.getPastDeals();
      setDeals(response.deals);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load past deals');
    } finally {
      setLoading(false);
    }
  };

  const getSuitSymbol = (text: string): string => {
    return text
      .replace(/♠/g, '<span style="color: black;">♠</span>')
      .replace(/♥/g, '<span style="color: red;">♥</span>')
      .replace(/♦/g, '<span style="color: red;">♦</span>')
      .replace(/♣/g, '<span style="color: black;">♣</span>');
  };

  if (loading) {
    return (
      <div className="past-deals">
        <div className="loading">Loading past deals...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="past-deals">
        <div className="error">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={loadPastDeals} className="btn btn-primary">
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (deals.length === 0) {
    return (
      <div className="past-deals">
        <div className="empty-state">
          <h2>No Past Deals</h2>
          <p>Complete some bidding sessions to see your deal history here.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="past-deals">
      <div className="past-deals-header">
        <h2>Past Deals</h2>
        <p>Review your completed bidding sessions</p>
      </div>

      <div className="deals-table-container">
        <table className="deals-table">
          <thead>
            <tr>
              <th>Deal #</th>
              <th>Dealer</th>
              <th>Final Contract</th>
              <th>Bidding System</th>
              <th>Bidding Sequence</th>
            </tr>
          </thead>
          <tbody>
            {deals.map((deal, index) => (
              <tr key={index}>
                <td>{index + 1}</td>
                <td>{deal.dealer}</td>
                <td 
                  className="final-contract"
                  dangerouslySetInnerHTML={{ __html: getSuitSymbol(deal.finalBid) }}
                />
                <td>{deal.biddingSystem}</td>
                <td className="bidding-sequence">
                  <div className="bid-sequence">
                    {deal.bids.map((bid, bidIndex) => (
                      <span 
                        key={bidIndex}
                        className="bid-item"
                        dangerouslySetInnerHTML={{ 
                          __html: `${bid.player}: ${getSuitSymbol(bid.displayText)}` 
                        }}
                      />
                    ))}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="deals-summary">
        <div className="summary-stats">
          <div className="stat">
            <span className="stat-label">Total Deals:</span>
            <span className="stat-value">{deals.length}</span>
          </div>
          <div className="stat">
            <span className="stat-label">Most Recent System:</span>
            <span className="stat-value">
              {deals.length > 0 ? deals[deals.length - 1].biddingSystem : 'N/A'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PastDeals;

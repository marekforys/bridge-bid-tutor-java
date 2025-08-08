import React from 'react';

interface GameControlsProps {
  trainingMode: string;
  biddingSystem: string;
  onTrainingModeChange: (mode: string) => void;
  onBiddingSystemChange: (system: string) => void;
  onNewDeal: () => void;
}

const GameControls: React.FC<GameControlsProps> = ({
  trainingMode,
  biddingSystem,
  onTrainingModeChange,
  onBiddingSystemChange,
  onNewDeal
}) => {
  const trainingModes = [
    { value: 'single', label: 'Single Hand' },
    { value: 'multi', label: 'Multi Hand' }
  ];

  const biddingSystems = [
    '2/1 Game Forcing',
    'Standard American',
    'Precision',
    'Acol'
  ];

  return (
    <div className="game-controls">
      <h3>Game Settings</h3>
      
      <div className="form-group">
        <label className="form-label">Training Mode:</label>
        <select 
          className="form-control"
          value={trainingMode}
          onChange={(e) => onTrainingModeChange(e.target.value)}
        >
          {trainingModes.map(mode => (
            <option key={mode.value} value={mode.value}>
              {mode.label}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Bidding System:</label>
        <select 
          className="form-control"
          value={biddingSystem}
          onChange={(e) => onBiddingSystemChange(e.target.value)}
        >
          {biddingSystems.map(system => (
            <option key={system} value={system}>
              {system}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <button 
          className="btn btn-primary btn-full-width"
          onClick={onNewDeal}
        >
          New Deal
        </button>
      </div>

      <div className="training-info">
        <h4>Training Mode Info</h4>
        {trainingMode === 'single' ? (
          <p>In Single Hand mode, you play as South while the computer handles other players' bids automatically.</p>
        ) : (
          <p>In Multi Hand mode, you control all players' bids manually for complete practice.</p>
        )}
      </div>
    </div>
  );
};

export default GameControls;

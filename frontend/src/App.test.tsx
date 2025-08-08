import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders bridge bidding tutor', () => {
  render(<App />);
  const linkElement = screen.getByText(/bridge bidding tutor/i);
  expect(linkElement).toBeInTheDocument();
});

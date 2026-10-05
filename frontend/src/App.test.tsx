import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';

// Mock the REST client so the component renders without a backend.
// The factory is hoisted, so the sample data lives inside it.
vi.mock('./api/client', () => {
  const sampleNotes = [
    {
      id: 1,
      title: 'Buy groceries',
      content: 'milk and eggs',
      archived: false,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
      categories: [],
    },
    {
      id: 2,
      title: 'Monday meeting',
      content: 'prepare the deck',
      archived: false,
      createdAt: '2026-01-02T00:00:00Z',
      updatedAt: '2026-01-02T00:00:00Z',
      categories: [],
    },
  ];
  return {
    notesApi: {
      list: () => Promise.resolve(sampleNotes),
      create: () => Promise.resolve(sampleNotes[0]),
      update: () => Promise.resolve(sampleNotes[0]),
      remove: () => Promise.resolve(),
      setArchived: () => Promise.resolve(sampleNotes[0]),
      addCategory: () => Promise.resolve(sampleNotes[0]),
      removeCategory: () => Promise.resolve(sampleNotes[0]),
    },
    categoriesApi: {
      list: () => Promise.resolve([]),
      create: () => Promise.resolve({ id: 1, name: 'x' }),
      remove: () => Promise.resolve(),
    },
  };
});

describe('App search', () => {
  it('loads notes from the API on mount', async () => {
    render(<App />);
    expect(await screen.findByText('Buy groceries')).toBeInTheDocument();
    expect(screen.getByText('Monday meeting')).toBeInTheDocument();
  });

  it('filters the visible notes as the user types', async () => {
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Buy groceries');

    await user.type(screen.getByLabelText('Search notes'), 'monday');

    expect(screen.getByText('Monday meeting')).toBeInTheDocument();
    expect(screen.queryByText('Buy groceries')).not.toBeInTheDocument();
  });

  it('shows an empty-state message when nothing matches', async () => {
    const user = userEvent.setup();
    render(<App />);
    await screen.findByText('Buy groceries');

    await user.type(screen.getByLabelText('Search notes'), 'zzz-nomatch');

    expect(screen.getByText(/No active notes match/i)).toBeInTheDocument();
  });
});

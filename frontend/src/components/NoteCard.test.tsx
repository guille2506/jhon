import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { NoteCard } from './NoteCard';
import type { Note } from '../types';

const baseNote: Note = {
  id: 1,
  title: 'My note',
  content: 'Some content',
  archived: false,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
  categories: [{ id: 9, name: 'Work' }],
};

function renderCard(overrides: Partial<Note> = {}) {
  const onEdit = vi.fn();
  const onDelete = vi.fn();
  const onToggleArchive = vi.fn();
  render(
    <NoteCard
      note={{ ...baseNote, ...overrides }}
      onEdit={onEdit}
      onDelete={onDelete}
      onToggleArchive={onToggleArchive}
    />,
  );
  return { onEdit, onDelete, onToggleArchive };
}

describe('NoteCard', () => {
  it('renders title, content and category', () => {
    renderCard();
    expect(screen.getByText('My note')).toBeInTheDocument();
    expect(screen.getByText('Some content')).toBeInTheDocument();
    expect(screen.getByText('Work')).toBeInTheDocument();
  });

  it('calls onEdit and onDelete when their buttons are clicked', async () => {
    const user = userEvent.setup();
    const { onEdit, onDelete } = renderCard();

    await user.click(screen.getByLabelText('Edit note'));
    await user.click(screen.getByLabelText('Delete note'));

    expect(onEdit).toHaveBeenCalledTimes(1);
    expect(onDelete).toHaveBeenCalledTimes(1);
  });

  it('shows the "Archive" action for an active note', () => {
    renderCard({ archived: false });
    expect(screen.getByLabelText('Archive note')).toBeInTheDocument();
  });

  it('shows the "Unarchive" action for an archived note', () => {
    renderCard({ archived: true });
    expect(screen.getByLabelText('Unarchive note')).toBeInTheDocument();
  });
});

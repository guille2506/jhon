import { describe, it, expect } from 'vitest';
import { filterNotes } from './filterNotes';
import type { Note } from '../types';

function note(partial: Partial<Note> & { id: number; title: string }): Note {
  return {
    content: null,
    archived: false,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    categories: [],
    ...partial,
  };
}

const notes: Note[] = [
  note({ id: 1, title: 'Buy groceries', content: 'milk and eggs' }),
  note({ id: 2, title: 'Monday meeting', content: 'prepare the deck' }),
  note({
    id: 3,
    title: 'Project idea',
    content: 'a notes app',
    categories: [{ id: 9, name: 'Work' }],
  }),
];

describe('filterNotes', () => {
  it('returns all notes for an empty or blank query', () => {
    expect(filterNotes(notes, '')).toHaveLength(3);
    expect(filterNotes(notes, '   ')).toHaveLength(3);
  });

  it('matches by title (case-insensitive)', () => {
    const result = filterNotes(notes, 'MONDAY');
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(2);
  });

  it('matches by content', () => {
    const result = filterNotes(notes, 'eggs');
    expect(result.map((n) => n.id)).toEqual([1]);
  });

  it('matches by category name', () => {
    const result = filterNotes(notes, 'work');
    expect(result.map((n) => n.id)).toEqual([3]);
  });

  it('returns an empty array when nothing matches', () => {
    expect(filterNotes(notes, 'xyz-nope')).toEqual([]);
  });

  it('handles notes with null content without throwing', () => {
    const withNull = [note({ id: 4, title: 'Solo title' })];
    expect(filterNotes(withNull, 'solo')).toHaveLength(1);
    expect(filterNotes(withNull, 'missing')).toHaveLength(0);
  });
});

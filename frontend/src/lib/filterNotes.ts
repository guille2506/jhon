import type { Note } from '../types';

/**
 * Filters notes by a free-text query, matching (case-insensitive) against the
 * title, content and category names. An empty/blank query returns all notes.
 */
export function filterNotes(notes: Note[], query: string): Note[] {
  const term = query.trim().toLowerCase();
  if (!term) {
    return notes;
  }
  return notes.filter((note) => {
    const haystack = [
      note.title,
      note.content ?? '',
      ...note.categories.map((c) => c.name),
    ]
      .join(' ')
      .toLowerCase();
    return haystack.includes(term);
  });
}

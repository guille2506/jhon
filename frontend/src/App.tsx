import { useCallback, useEffect, useState } from 'react';
import { categoriesApi, notesApi } from './api/client';
import { NoteForm } from './components/NoteForm';
import { NoteCard } from './components/NoteCard';
import { CategorySidebar } from './components/CategorySidebar';
import type { Category, Note, NotePayload } from './types';
import { confirmDelete, toastError, toastSuccess } from './lib/alerts';
import { filterNotes } from './lib/filterNotes';

type Tab = 'active' | 'archived';

export default function App() {
  const [tab, setTab] = useState<Tab>('active');
  const [notes, setNotes] = useState<Note[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [filters, setFilters] = useState<number[]>([]);
  const [search, setSearch] = useState('');
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const loadNotes = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await notesApi.list(tab === 'archived', filters);
      setNotes(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load notes');
    } finally {
      setLoading(false);
    }
  }, [tab, filters]);

  const loadCategories = useCallback(async () => {
    try {
      setCategories(await categoriesApi.list());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load categories');
    }
  }, []);

  useEffect(() => {
    void loadNotes();
  }, [loadNotes]);

  useEffect(() => {
    void loadCategories();
  }, [loadCategories]);

  const handleSubmit = async (payload: NotePayload) => {
    if (editingNote) {
      await notesApi.update(editingNote.id, payload);
      setEditingNote(null);
      toastSuccess('Note updated');
    } else {
      await notesApi.create(payload);
      toastSuccess('Note created');
    }
    await loadNotes();
  };

  const handleDelete = async (note: Note) => {
    if (!(await confirmDelete(note.title))) return;
    try {
      await notesApi.remove(note.id);
      if (editingNote?.id === note.id) setEditingNote(null);
      await loadNotes();
      toastSuccess('Note deleted');
    } catch (err) {
      toastError(err instanceof Error ? err.message : 'Could not delete note');
    }
  };

  const handleToggleArchive = async (note: Note) => {
    await notesApi.setArchived(note.id, !note.archived);
    await loadNotes();
    toastSuccess(note.archived ? 'Note unarchived' : 'Note archived');
  };

  const handleCreateCategory = async (name: string) => {
    await categoriesApi.create(name);
    await loadCategories();
  };

  const handleDeleteCategory = async (category: Category) => {
    if (!(await confirmDelete(category.name))) return;
    try {
      await categoriesApi.remove(category.id);
      setFilters((prev) => prev.filter((id) => id !== category.id));
      await loadCategories();
      await loadNotes();
      toastSuccess('Category deleted');
    } catch (err) {
      toastError(err instanceof Error ? err.message : 'Could not delete category');
    }
  };

  const toggleFilter = (id: number) => {
    setFilters((prev) => (prev.includes(id) ? prev.filter((f) => f !== id) : [...prev, id]));
  };

  const switchTab = (next: Tab) => {
    setTab(next);
    setEditingNote(null);
  };

  const visibleNotes = filterNotes(notes, search);

  return (
    <div className="layout">
      <h2 className="sr-only">
        Notes application. Create and edit notes, organize them with categories,
        switch between active and archived notes, and filter the list by category.
      </h2>
      <header className="app-header">
        <h1 className="app-title">
          <i className="ti ti-notebook"></i>
          Notes
        </h1>
        <div className="header-controls">
          <label className="search">
            <i className="ti ti-search"></i>
            <input
              type="text"
              placeholder="Search notes..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Search notes"
            />
            {search && (
              <button
                type="button"
                className="search__clear"
                aria-label="Clear search"
                onClick={() => setSearch('')}
              >
                <i className="ti ti-x"></i>
              </button>
            )}
          </label>
          <nav className="tabs">
            <button
              className={`tab ${tab === 'active' ? 'tab--active' : ''}`}
              onClick={() => switchTab('active')}
            >
              <i className="ti ti-note"></i>
              Active
            </button>
            <button
              className={`tab ${tab === 'archived' ? 'tab--active' : ''}`}
              onClick={() => switchTab('archived')}
            >
              <i className="ti ti-archive"></i>
              Archived
            </button>
          </nav>
        </div>
      </header>

      <div className="main-grid">
        <div className="left-column">
          <NoteForm
            categories={categories}
            editingNote={editingNote}
            onSubmit={handleSubmit}
            onCancelEdit={() => setEditingNote(null)}
          />
          <CategorySidebar
            categories={categories}
            activeFilters={filters}
            onToggleFilter={toggleFilter}
            onClearFilters={() => setFilters([])}
            onCreate={handleCreateCategory}
            onDelete={handleDeleteCategory}
          />
        </div>

        <main className="notes-area">
          {error && <p className="error">{error}</p>}
          {loading ? (
            <p className="muted">Loading...</p>
          ) : visibleNotes.length === 0 ? (
            <p className="muted">
              {search.trim()
                ? `No ${tab} notes match "${search.trim()}".`
                : `No ${tab} notes${filters.length > 0 ? ' for the selected categories' : ''}.`}
            </p>
          ) : (
            <div className="notes-grid">
              {visibleNotes.map((note) => (
                <NoteCard
                  key={note.id}
                  note={note}
                  onEdit={setEditingNote}
                  onDelete={handleDelete}
                  onToggleArchive={handleToggleArchive}
                />
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

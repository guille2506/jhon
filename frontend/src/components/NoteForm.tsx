import { useEffect, useState } from 'react';
import type { Category, Note, NotePayload } from '../types';

interface Props {
  categories: Category[];
  editingNote: Note | null;
  onSubmit: (payload: NotePayload) => Promise<void>;
  onCancelEdit: () => void;
}

export function NoteForm({ categories, editingNote, onSubmit, onCancelEdit }: Props) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedCategories, setSelectedCategories] = useState<number[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (editingNote) {
      setTitle(editingNote.title);
      setContent(editingNote.content ?? '');
      setSelectedCategories(editingNote.categories.map((c) => c.id));
    } else {
      setTitle('');
      setContent('');
      setSelectedCategories([]);
    }
    setError(null);
  }, [editingNote]);

  const toggleCategory = (id: number) => {
    setSelectedCategories((prev) =>
      prev.includes(id) ? prev.filter((c) => c !== id) : [...prev, id],
    );
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim()) {
      setError('Title is required');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await onSubmit({ title: title.trim(), content, categoryIds: selectedCategories });
      if (!editingNote) {
        setTitle('');
        setContent('');
        setSelectedCategories([]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save note');
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="note-form card" onSubmit={handleSubmit}>
      <h2>{editingNote ? 'Edit note' : 'New note'}</h2>
      {error && <p className="error">{error}</p>}
      <input
        type="text"
        placeholder="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={255}
      />
      <textarea
        placeholder="Write your note..."
        value={content}
        rows={4}
        onChange={(e) => setContent(e.target.value)}
      />
      {categories.length > 0 && (
        <div className="chip-row">
          {categories.map((category) => (
            <button
              type="button"
              key={category.id}
              className={`chip ${selectedCategories.includes(category.id) ? 'chip--active' : ''}`}
              onClick={() => toggleCategory(category.id)}
            >
              <span className="chip__dot" />
              {category.name}
            </button>
          ))}
        </div>
      )}
      <div className="form-actions">
        <button type="submit" className="btn btn--primary" disabled={saving}>
          <i className={editingNote ? 'ti ti-check' : 'ti ti-plus'}></i>
          {editingNote ? 'Save changes' : 'Add note'}
        </button>
        {editingNote && (
          <button type="button" className="btn" onClick={onCancelEdit} disabled={saving}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

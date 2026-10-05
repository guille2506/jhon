import type { Note } from '../types';

interface Props {
  note: Note;
  onEdit: (note: Note) => void;
  onDelete: (note: Note) => void;
  onToggleArchive: (note: Note) => void;
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

export function NoteCard({ note, onEdit, onDelete, onToggleArchive }: Props) {
  return (
    <article className="card note-card">
      <div className="note-card__header">
        <h3>{note.title}</h3>
      </div>
      {note.content && <p className="note-card__content">{note.content}</p>}
      {note.categories.length > 0 && (
        <div className="chip-row">
          {note.categories.map((category) => (
            <span key={category.id} className="chip chip--static">
              <span className="chip__dot" />
              {category.name}
            </span>
          ))}
        </div>
      )}
      <div className="note-card__footer">
        <span className="note-card__date">Updated {formatDate(note.updatedAt)}</span>
        <div className="note-card__actions">
          <button className="icon-btn" aria-label="Edit note" onClick={() => onEdit(note)}>
            <i className="ti ti-pencil"></i>
          </button>
          <button
            className="icon-btn"
            aria-label={note.archived ? 'Unarchive note' : 'Archive note'}
            onClick={() => onToggleArchive(note)}
          >
            <i className={note.archived ? 'ti ti-archive-off' : 'ti ti-archive'}></i>
          </button>
          <button
            className="icon-btn icon-btn--danger"
            aria-label="Delete note"
            onClick={() => onDelete(note)}
          >
            <i className="ti ti-trash"></i>
          </button>
        </div>
      </div>
    </article>
  );
}

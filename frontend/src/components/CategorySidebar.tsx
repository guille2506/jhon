import { useState } from 'react';
import type { Category } from '../types';

interface Props {
  categories: Category[];
  activeFilters: number[];
  onToggleFilter: (id: number) => void;
  onClearFilters: () => void;
  onCreate: (name: string) => Promise<void>;
  onDelete: (category: Category) => Promise<void>;
}

export function CategorySidebar({
  categories,
  activeFilters,
  onToggleFilter,
  onClearFilters,
  onCreate,
  onDelete,
}: Props) {
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleCreate = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!name.trim()) return;
    setError(null);
    try {
      await onCreate(name.trim());
      setName('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not create category');
    }
  };

  return (
    <aside className="card sidebar">
      <h2>Categories</h2>

      <form className="sidebar__create" onSubmit={handleCreate}>
        <input
          type="text"
          placeholder="New category"
          value={name}
          onChange={(e) => setName(e.target.value)}
          maxLength={60}
        />
        <button type="submit" className="btn btn--primary">
          <i className="ti ti-plus"></i>
          Add
        </button>
      </form>
      {error && <p className="error">{error}</p>}

      {categories.length === 0 ? (
        <p className="muted">No categories yet.</p>
      ) : (
        <>
          <div className="sidebar__filter-header">
            <span>Filter by category</span>
            {activeFilters.length > 0 && (
              <button className="link-btn" onClick={onClearFilters}>
                Clear
              </button>
            )}
          </div>
          <ul className="category-list">
            {categories.map((category) => (
              <li key={category.id} className="category-list__item">
                <button
                  className={`chip ${activeFilters.includes(category.id) ? 'chip--active' : ''}`}
                  onClick={() => onToggleFilter(category.id)}
                >
                  <span className="chip__dot" />
                  {category.name}
                </button>
                <button
                  className="link-btn link-btn--danger"
                  aria-label={`Delete category ${category.name}`}
                  title="Delete category"
                  onClick={() => onDelete(category)}
                >
                  <i className="ti ti-x"></i>
                </button>
              </li>
            ))}
          </ul>
        </>
      )}
    </aside>
  );
}

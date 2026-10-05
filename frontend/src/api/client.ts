import type { Category, Note, NotePayload } from '../types';

// Base URL of the REST API. Defaults to the relative /api path which the Vite
// dev server proxies to the backend. Override with VITE_API_URL in production.
const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // ignore non-JSON error bodies
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const notesApi = {
  list(archived: boolean, categoryIds: number[] = []): Promise<Note[]> {
    const params = new URLSearchParams({ archived: String(archived) });
    categoryIds.forEach((id) => params.append('categoryIds', String(id)));
    return request<Note[]>(`/notes?${params.toString()}`);
  },

  create(payload: NotePayload): Promise<Note> {
    return request<Note>('/notes', { method: 'POST', body: JSON.stringify(payload) });
  },

  update(id: number, payload: NotePayload): Promise<Note> {
    return request<Note>(`/notes/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  remove(id: number): Promise<void> {
    return request<void>(`/notes/${id}`, { method: 'DELETE' });
  },

  setArchived(id: number, archived: boolean): Promise<Note> {
    const action = archived ? 'archive' : 'unarchive';
    return request<Note>(`/notes/${id}/${action}`, { method: 'PATCH' });
  },

  addCategory(noteId: number, categoryId: number): Promise<Note> {
    return request<Note>(`/notes/${noteId}/categories/${categoryId}`, { method: 'POST' });
  },

  removeCategory(noteId: number, categoryId: number): Promise<Note> {
    return request<Note>(`/notes/${noteId}/categories/${categoryId}`, { method: 'DELETE' });
  },
};

export const categoriesApi = {
  list(): Promise<Category[]> {
    return request<Category[]>('/categories');
  },

  create(name: string): Promise<Category> {
    return request<Category>('/categories', { method: 'POST', body: JSON.stringify({ name }) });
  },

  remove(id: number): Promise<void> {
    return request<void>(`/categories/${id}`, { method: 'DELETE' });
  },
};

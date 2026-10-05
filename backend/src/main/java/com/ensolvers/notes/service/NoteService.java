package com.ensolvers.notes.service;

import com.ensolvers.notes.dto.NoteRequest;
import com.ensolvers.notes.exception.ResourceNotFoundException;
import com.ensolvers.notes.model.Category;
import com.ensolvers.notes.model.Note;
import com.ensolvers.notes.repository.CategoryRepository;
import com.ensolvers.notes.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;

    public NoteService(NoteRepository noteRepository, CategoryRepository categoryRepository) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Note> list(boolean archived, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return noteRepository.findByArchivedOrderByUpdatedAtDesc(archived);
        }
        return noteRepository.findByArchivedAndCategoryIds(archived, categoryIds);
    }

    @Transactional(readOnly = true)
    public Note getById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));
    }

    @Transactional
    public Note create(NoteRequest request) {
        Note note = new Note();
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent());
        note.setCategories(resolveCategories(request.getCategoryIds()));
        return noteRepository.save(note);
    }

    @Transactional
    public Note update(Long id, NoteRequest request) {
        Note note = getById(id);
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent());
        if (request.getCategoryIds() != null) {
            note.setCategories(resolveCategories(request.getCategoryIds()));
        }
        return noteRepository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        Note note = getById(id);
        noteRepository.delete(note);
    }

    @Transactional
    public Note setArchived(Long id, boolean archived) {
        Note note = getById(id);
        note.setArchived(archived);
        return noteRepository.save(note);
    }

    @Transactional
    public Note addCategory(Long noteId, Long categoryId) {
        Note note = getById(noteId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        note.getCategories().add(category);
        return noteRepository.save(note);
    }

    @Transactional
    public Note removeCategory(Long noteId, Long categoryId) {
        Note note = getById(noteId);
        note.getCategories().removeIf(c -> c.getId().equals(categoryId));
        return noteRepository.save(note);
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>();
        if (categoryIds == null) {
            return categories;
        }
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            categories.add(category);
        }
        return categories;
    }
}

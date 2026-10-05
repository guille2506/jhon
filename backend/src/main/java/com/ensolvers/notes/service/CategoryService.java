package com.ensolvers.notes.service;

import com.ensolvers.notes.dto.CategoryRequest;
import com.ensolvers.notes.exception.ConflictException;
import com.ensolvers.notes.exception.ResourceNotFoundException;
import com.ensolvers.notes.model.Category;
import com.ensolvers.notes.model.Note;
import com.ensolvers.notes.repository.CategoryRepository;
import com.ensolvers.notes.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NoteRepository noteRepository;

    public CategoryService(CategoryRepository categoryRepository, NoteRepository noteRepository) {
        this.categoryRepository = categoryRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        return categoryRepository.save(new Category(name));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        // Detach the category from any note first to keep the join table consistent
        List<Note> notes = noteRepository.findByCategories_Id(id);
        for (Note note : notes) {
            note.getCategories().remove(category);
        }
        noteRepository.saveAll(notes);
        categoryRepository.delete(category);
    }
}

package com.ensolvers.notes.service;

import com.ensolvers.notes.dto.CategoryRequest;
import com.ensolvers.notes.exception.ConflictException;
import com.ensolvers.notes.exception.ResourceNotFoundException;
import com.ensolvers.notes.model.Category;
import com.ensolvers.notes.model.Note;
import com.ensolvers.notes.repository.CategoryRepository;
import com.ensolvers.notes.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    @Test
    void create_trimsName_andSaves() {
        CategoryRequest request = new CategoryRequest();
        request.setName("  Work  ");
        when(categoryRepository.existsByNameIgnoreCase("Work")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category created = categoryService.create(request);

        assertThat(created.getName()).isEqualTo("Work");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Work");
    }

    @Test
    void create_duplicateName_throwsConflict() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Work");
        when(categoryRepository.existsByNameIgnoreCase("Work")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Work");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_detachesFromNotes_thenDeletes() {
        Category category = category(1L, "Work");
        Note note = new Note();
        note.setId(10L);
        note.getCategories().add(category);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(noteRepository.findByCategories_Id(1L)).thenReturn(List.of(note));

        categoryService.delete(1L);

        assertThat(note.getCategories()).doesNotContain(category);
        verify(noteRepository).saveAll(List.of(note));
        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_unknownCategory_throwsNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void findAll_returnsAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(category(1L, "A"), category(2L, "B")));

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(2);
    }
}

package com.ensolvers.notes.service;

import com.ensolvers.notes.dto.NoteRequest;
import com.ensolvers.notes.exception.ResourceNotFoundException;
import com.ensolvers.notes.model.Category;
import com.ensolvers.notes.model.Note;
import com.ensolvers.notes.repository.CategoryRepository;
import com.ensolvers.notes.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private NoteService noteService;

    private Note note(Long id, String title) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        return note;
    }

    private Category category(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    @Test
    void create_trimsTitle_resolvesCategories_andSaves() {
        NoteRequest request = new NoteRequest();
        request.setTitle("  My note  ");
        request.setContent("body");
        request.setCategoryIds(Set.of(1L));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "Work")));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note created = noteService.create(request);

        assertThat(created.getTitle()).isEqualTo("My note");
        assertThat(created.getContent()).isEqualTo("body");
        assertThat(created.getCategories()).extracting(Category::getName).containsExactly("Work");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void create_withUnknownCategory_throwsNotFound() {
        NoteRequest request = new NoteRequest();
        request.setTitle("Title");
        request.setCategoryIds(Set.of(99L));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(noteRepository, never()).save(any());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withNullCategoryIds_doesNotTouchCategories() {
        Note existing = note(1L, "old");
        existing.setCategories(Set.of(category(5L, "Keep")));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        NoteRequest request = new NoteRequest();
        request.setTitle("  new title  ");
        request.setContent("new body");
        request.setCategoryIds(null);

        Note updated = noteService.update(1L, request);

        assertThat(updated.getTitle()).isEqualTo("new title");
        assertThat(updated.getContent()).isEqualTo("new body");
        assertThat(updated.getCategories()).extracting(Category::getName).containsExactly("Keep");
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void update_withCategoryIds_replacesCategories() {
        Note existing = note(1L, "old");
        existing.setCategories(Set.of(category(5L, "Old")));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category(7L, "New")));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        NoteRequest request = new NoteRequest();
        request.setTitle("t");
        request.setCategoryIds(Set.of(7L));

        Note updated = noteService.update(1L, request);

        assertThat(updated.getCategories()).extracting(Category::getName).containsExactly("New");
    }

    @Test
    void setArchived_updatesFlag() {
        Note existing = note(1L, "t");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note archived = noteService.setArchived(1L, true);

        assertThat(archived.isArchived()).isTrue();
    }

    @Test
    void addCategory_addsToNote() {
        Note existing = note(1L, "t");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category(2L, "Tag")));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.addCategory(1L, 2L);

        assertThat(result.getCategories()).extracting(Category::getName).contains("Tag");
    }

    @Test
    void addCategory_unknownCategory_throwsNotFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note(1L, "t")));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.addCategory(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeCategory_removesById() {
        Note existing = note(1L, "t");
        existing.getCategories().add(category(2L, "Tag"));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = noteService.removeCategory(1L, 2L);

        assertThat(result.getCategories()).isEmpty();
    }

    @Test
    void delete_removesNote() {
        Note existing = note(1L, "t");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));

        noteService.delete(1L);

        verify(noteRepository).delete(existing);
    }

    @Test
    void list_withoutCategoryIds_usesArchivedQuery() {
        when(noteRepository.findByArchivedOrderByUpdatedAtDesc(false)).thenReturn(List.of(note(1L, "a")));

        List<Note> result = noteService.list(false, null);

        assertThat(result).hasSize(1);
        verify(noteRepository).findByArchivedOrderByUpdatedAtDesc(false);
    }

    @Test
    void list_withCategoryIds_usesFilteredQuery() {
        List<Long> ids = List.of(1L, 2L);
        when(noteRepository.findByArchivedAndCategoryIds(true, ids)).thenReturn(List.of(note(1L, "a")));

        List<Note> result = noteService.list(true, ids);

        assertThat(result).hasSize(1);
        verify(noteRepository).findByArchivedAndCategoryIds(true, ids);
        verify(noteRepository, never()).findByArchivedOrderByUpdatedAtDesc(anyBoolean());
    }
}

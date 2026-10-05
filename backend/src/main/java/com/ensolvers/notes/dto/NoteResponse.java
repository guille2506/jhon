package com.ensolvers.notes.dto;

import com.ensolvers.notes.model.Note;

import java.time.Instant;
import java.util.List;
import java.util.Comparator;

public record NoteResponse(
        Long id,
        String title,
        String content,
        boolean archived,
        Instant createdAt,
        Instant updatedAt,
        List<CategoryDto> categories
) {

    public static NoteResponse from(Note note) {
        List<CategoryDto> categories = note.getCategories().stream()
                .map(CategoryDto::from)
                .sorted(Comparator.comparing(CategoryDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.isArchived(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                categories
        );
    }
}

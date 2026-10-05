package com.ensolvers.notes.dto;

import com.ensolvers.notes.model.Category;

public record CategoryDto(Long id, String name) {

    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}

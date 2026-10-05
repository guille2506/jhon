package com.ensolvers.notes.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class NoteRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    // Optional set of category ids to associate on create/update
    private Set<Long> categoryIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}

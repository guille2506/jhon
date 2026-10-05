package com.ensolvers.notes.controller;

import com.ensolvers.notes.dto.CategoryRequest;
import com.ensolvers.notes.exception.ConflictException;
import com.ensolvers.notes.model.Category;
import com.ensolvers.notes.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private Category category(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    @Test
    void list_returnsCategories() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(category(1L, "Work"), category(2L, "Home")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Work"))
                .andExpect(jsonPath("$[1].name").value("Home"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(category(3L, "Ideas"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Ideas"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Ideas"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: Category name is required"));
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new ConflictException("Category already exists: Work"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Work"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Category already exists: Work"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }
}

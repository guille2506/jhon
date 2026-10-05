package com.ensolvers.notes.controller;

import com.ensolvers.notes.dto.NoteRequest;
import com.ensolvers.notes.exception.ResourceNotFoundException;
import com.ensolvers.notes.model.Note;
import com.ensolvers.notes.service.NoteService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    private Note note(Long id, String title) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent("body");
        return note;
    }

    @Test
    void list_returnsNotes() throws Exception {
        when(noteService.list(anyBoolean(), any())).thenReturn(List.of(note(1L, "First")));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("First"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(noteService.create(any(NoteRequest.class))).thenReturn(note(5L, "New"));

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "New", "content", "body"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void create_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title: Title is required"));
    }

    @Test
    void getById_whenMissing_returns404() throws Exception {
        when(noteService.getById(99L)).thenThrow(new ResourceNotFoundException("Note not found: 99"));

        mockMvc.perform(get("/api/notes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Note not found: 99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());

        verify(noteService).delete(1L);
    }

    @Test
    void archive_returnsArchivedNote() throws Exception {
        Note archived = note(1L, "t");
        archived.setArchived(true);
        when(noteService.setArchived(eq(1L), eq(true))).thenReturn(archived);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/notes/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
    }
}

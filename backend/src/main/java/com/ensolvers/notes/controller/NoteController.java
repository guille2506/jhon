package com.ensolvers.notes.controller;

import com.ensolvers.notes.dto.NoteRequest;
import com.ensolvers.notes.dto.NoteResponse;
import com.ensolvers.notes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> list(
            @RequestParam(name = "archived", defaultValue = "false") boolean archived,
            @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds) {
        return noteService.list(archived, categoryIds).stream()
                .map(NoteResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable Long id) {
        return NoteResponse.from(noteService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        NoteResponse response = NoteResponse.from(noteService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return NoteResponse.from(noteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public NoteResponse archive(@PathVariable Long id) {
        return NoteResponse.from(noteService.setArchived(id, true));
    }

    @PatchMapping("/{id}/unarchive")
    public NoteResponse unarchive(@PathVariable Long id) {
        return NoteResponse.from(noteService.setArchived(id, false));
    }

    @PostMapping("/{noteId}/categories/{categoryId}")
    public NoteResponse addCategory(@PathVariable Long noteId, @PathVariable Long categoryId) {
        return NoteResponse.from(noteService.addCategory(noteId, categoryId));
    }

    @DeleteMapping("/{noteId}/categories/{categoryId}")
    public NoteResponse removeCategory(@PathVariable Long noteId, @PathVariable Long categoryId) {
        return NoteResponse.from(noteService.removeCategory(noteId, categoryId));
    }
}

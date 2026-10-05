package com.ensolvers.notes.repository;

import com.ensolvers.notes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByArchivedOrderByUpdatedAtDesc(boolean archived);

    List<Note> findByCategories_Id(Long categoryId);

    @Query("""
            SELECT DISTINCT n FROM Note n JOIN n.categories c
            WHERE n.archived = :archived AND c.id IN :categoryIds
            ORDER BY n.updatedAt DESC
            """)
    List<Note> findByArchivedAndCategoryIds(@Param("archived") boolean archived,
                                            @Param("categoryIds") List<Long> categoryIds);
}

package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    // findBy queries
    Optional<Tag> findByName(String name);

    List<Tag> findByColor(String color);

    List<Tag> findByNameContainingIgnoreCase(String name);

    List<Tag> findAllByOrderByNameAsc();

    // existsBy queries
    boolean existsByName(String name);

    // Pagination
    Page<Tag> findAll(Pageable pageable);

    // Custom queries
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.pageTags pt WHERE pt.page.user.id = :userId")
    List<Tag> findTagsByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM Tag t WHERE SIZE(t.pageTags) = 0")
    List<Tag> findUnusedTags();

    @Query("SELECT COUNT(pt) FROM PageTag pt WHERE pt.tag.id = :tagId")
    long countPagesByTagId(@Param("tagId") String tagId);

    // Top queries
    List<Tag> findTop10ByOrderByCreatedAtDesc();
}
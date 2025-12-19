package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, String> {

        // findBy queries
        List<Page> findByUserId(String userId);

        List<Page> findByWorkspaceId(String workspaceId);

        List<Page> findByUserIdAndIsFavoriteTrue(String userId);

        List<Page> findByUserIdAndIsArchivedTrue(String userId);

        List<Page> findByTitleContainingIgnoreCase(String title);

        List<Page> findByCreatedAtAfter(LocalDateTime date);

        List<Page> findByUserIdOrderByCreatedAtDesc(String userId);

        // existsBy queries
        boolean existsByIdAndUserId(String id, String userId);

        boolean existsByWorkspaceId(String workspaceId);

        // Sorting
        List<Page> findByUserId(String userId, Sort sort);

        // Pagination
        org.springframework.data.domain.Page<Page> findAll(Pageable pageable);

        org.springframework.data.domain.Page<Page> findByUserId(String userId, Pageable pageable);

        org.springframework.data.domain.Page<Page> findByWorkspaceId(String workspaceId, Pageable pageable);

        // Custom queries
        @Query("SELECT p FROM Page p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Page> searchPages(@Param("keyword") String keyword);

        @Query("SELECT p FROM Page p WHERE p.user.id = :userId AND " +
                        "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<Page> searchUserPages(@Param("userId") String userId, @Param("keyword") String keyword);

        @Query("SELECT p FROM Page p WHERE p.workspace.isDefault = true AND p.user.id = :userId")
        List<Page> findInboxPages(@Param("userId") String userId);

        @Query("SELECT DISTINCT p FROM Page p JOIN p.pageTags pt WHERE pt.tag.id = :tagId")
        List<Page> findPagesByTagId(@Param("tagId") String tagId);

        // Count queries
        long countByUserId(String userId);

        long countByWorkspaceId(String workspaceId);

        long countByUserIdAndIsFavoriteTrue(String userId);
        
        // Count pages created between dates (for weekly stats)
        @Query("SELECT COUNT(p) FROM Page p WHERE p.user.id = :userId AND p.createdAt >= :startDate AND p.createdAt < :endDate")
        long countByUserIdAndCreatedAtBetween(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        // Top queries
        List<Page> findTop10ByUserIdOrderByUpdatedAtDesc(String userId);
}

package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

    // findBy queries
    List<Workspace> findByOwnerId(String ownerId);

    Optional<Workspace> findByOwnerIdAndIsDefaultTrue(String ownerId);

    List<Workspace> findByNameContainingIgnoreCase(String name);

    // existsBy queries
    boolean existsByNameAndOwnerId(String name, String ownerId);

    boolean existsByOwnerIdAndIsDefaultTrue(String ownerId);

    // Sorting
    List<Workspace> findByOwnerId(String ownerId, Sort sort);

    List<Workspace> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    // Pagination
    Page<Workspace> findAll(Pageable pageable);

    Page<Workspace> findByOwnerId(String ownerId, Pageable pageable);

    // Custom queries
    @Query("SELECT w FROM Workspace w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Workspace> searchWorkspaces(@Param("keyword") String keyword);

    @Query("SELECT COUNT(p) FROM Page p WHERE p.workspace.id = :workspaceId")
    long countPagesByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Query("SELECT w FROM Workspace w WHERE w.owner.id = :ownerId AND SIZE(w.pages) = 0")
    List<Workspace> findEmptyWorkspaces(@Param("ownerId") String ownerId);

    // Count queries
    long countByOwnerId(String ownerId);
}
package com.notekeeper.notekeeper.repository;

import com.notekeeper.notekeeper.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    List<WorkspaceMember> findByUserId(String userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.owner.id = :ownerId AND wm.role = 'OWNER'")
    List<WorkspaceMember> findOwnedWorkspacesByOwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId")
    long countByWorkspaceId(@Param("workspaceId") String workspaceId);

    void deleteByWorkspaceIdAndUserId(String workspaceId, String userId);
}

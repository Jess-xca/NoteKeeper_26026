package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.dto.WorkspaceMemberDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkspaceMemberService {

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DTOMapper dtoMapper;

    public String addMember(String workspaceId, String userId, String role) {
        if (!WorkspaceRole.isValid(role)) {
            return "invalid role";
        }

        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        if (!workspaceOpt.isPresent()) {
            return "workspace not found";
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return "user not found";
        }

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            return "user already member";
        }

        WorkspaceMember member = new WorkspaceMember(workspaceOpt.get(), userOpt.get(),
                WorkspaceRole.valueOf(role.toUpperCase()));
        WorkspaceMember saved = workspaceMemberRepository.save(member);
        return saved.getId();
    }

    public String removeMember(String workspaceId, String userId) {
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
        if (!memberOpt.isPresent()) {
            return "member not found";
        }

        WorkspaceMember member = memberOpt.get();
        if (member.getRole() == WorkspaceRole.OWNER) {
            return "cannot remove owner";
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        return "success";
    }

    public String updateMemberRole(String workspaceId, String userId, String newRole) {
        if (!WorkspaceRole.isValid(newRole)) {
            return "invalid role";
        }

        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
        if (!memberOpt.isPresent()) {
            return "member not found";
        }

        WorkspaceMember member = memberOpt.get();
        if (member.getRole() == WorkspaceRole.OWNER && !newRole.equalsIgnoreCase("OWNER")) {
            return "cannot change owner role";
        }

        member.setRole(WorkspaceRole.valueOf(newRole.toUpperCase()));
        workspaceMemberRepository.save(member);
        return "success";
    }

    public List<WorkspaceMemberDTO> getWorkspaceMembers(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        return members.stream()
                .map(dtoMapper::toWorkspaceMemberDTO)
                .collect(Collectors.toList());
    }

    public List<WorkspaceMemberDTO> getUserWorkspaces(String userId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByUserId(userId);
        return members.stream()
                .map(dtoMapper::toWorkspaceMemberDTO)
                .collect(Collectors.toList());
    }

    public boolean isUserMember(String workspaceId, String userId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    public boolean hasPermission(String workspaceId, String userId, String requiredRole) {
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
        if (!memberOpt.isPresent()) {
            return false;
        }

        WorkspaceRole userRole = memberOpt.get().getRole();
        return getRoleLevel(userRole) >= getRoleLevel(WorkspaceRole.valueOf(requiredRole.toUpperCase()));
    }

    private int getRoleLevel(WorkspaceRole role) {
        switch (role) {
            case OWNER:
                return 3;
            case EDITOR:
                return 2;
            case VIEWER:
                return 1;
            default:
                return 0;
        }
    }
}

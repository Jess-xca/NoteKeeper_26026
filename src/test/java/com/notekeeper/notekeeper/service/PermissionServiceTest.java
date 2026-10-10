package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.exception.UnauthorizedException;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.PageShareRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PageRepository pageRepository;
    @Mock
    private PageShareRepository pageShareRepository;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Page page;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspace = org.mockito.Mockito.mock(Workspace.class);
    }

    @Test
    void viewerCanReadAndCannotEdit() {
        stubPage();
        member("viewer", WorkspaceRole.VIEWER);
        when(pageShareRepository.findByPageIdAndSharedWithId("page-1", "viewer")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> permissionService.validatePageAccess("page-1", "viewer", "READ"));
        assertThrows(UnauthorizedException.class,
                () -> permissionService.validatePageAccess("page-1", "viewer", "EDIT"));
    }

    @Test
    void editorCanEditPage() {
        stubPage();
        member("editor", WorkspaceRole.EDITOR);
        when(pageShareRepository.findByPageIdAndSharedWithId("page-1", "editor")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> permissionService.validatePageAccess("page-1", "editor", "EDIT"));
    }

    @Test
    void ownerCanManageWorkspace() {
        User owner = org.mockito.Mockito.mock(User.class);
        when(owner.getId()).thenReturn("owner");
        when(workspace.getOwner()).thenReturn(owner);
        when(workspaceRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));

        assertDoesNotThrow(() -> permissionService.validateWorkspaceAccess(
                "workspace-1", "owner", WorkspaceRole.OWNER));
    }

    @Test
    void viewerCannotManageWorkspace() {
        User owner = org.mockito.Mockito.mock(User.class);
        when(owner.getId()).thenReturn("owner");
        when(workspace.getOwner()).thenReturn(owner);
        when(workspaceRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));

        WorkspaceMember viewer = org.mockito.Mockito.mock(WorkspaceMember.class);
        when(viewer.getRole()).thenReturn(WorkspaceRole.VIEWER);
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId("workspace-1", "viewer"))
                .thenReturn(Optional.of(viewer));

        assertThrows(UnauthorizedException.class, () -> permissionService.validateWorkspaceAccess(
                "workspace-1", "viewer", WorkspaceRole.OWNER));
    }

    private void stubPage() {
        User author = org.mockito.Mockito.mock(User.class);
        when(author.getId()).thenReturn("author");
        when(workspace.getId()).thenReturn("workspace-1");
        page = org.mockito.Mockito.mock(Page.class);
        when(page.getUser()).thenReturn(author);
        when(page.getWorkspace()).thenReturn(workspace);
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(page));
    }

    private void member(String userId, WorkspaceRole role) {
        WorkspaceMember member = org.mockito.Mockito.mock(WorkspaceMember.class);
        when(member.getRole()).thenReturn(role);
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId("workspace-1", userId))
                .thenReturn(Optional.of(member));
    }
}

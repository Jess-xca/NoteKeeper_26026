package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.messaging.AppEvent;
import com.notekeeper.notekeeper.messaging.EventPublisher;
import com.notekeeper.notekeeper.model.Notification;
import com.notekeeper.notekeeper.model.NotificationType;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.NotificationRepository;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Two (and more) real users against the real controllers and an in-memory database:
 * owner, editor, viewer, outsider and admin.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sharingtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=test-secret-key-that-is-long-enough-for-hs256",
        "google.client.id=test-client",
        "google.client.secret=test-secret",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test"
})
@AutoConfigureMockMvc
@Transactional
class SharingAndSettingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // No real email/SMS/RabbitMQ in tests; we only check what would have been sent
    @MockitoBean
    private EventPublisher eventPublisher;

    private User owner;
    private User editor;
    private User viewer;
    private User outsider;
    private User admin;
    private Workspace workspace;
    private Page page;

    @BeforeEach
    void people() {
        owner = newUser("owner", "USER");
        editor = newUser("editor", "USER");
        viewer = newUser("viewer", "USER");
        outsider = newUser("outsider", "USER");
        admin = newUser("admin", "ADMIN");

        workspace = workspaceRepository.save(new Workspace("Team notes", owner, false));
        workspaceMemberRepository.save(new WorkspaceMember(workspace, owner, WorkspaceRole.OWNER));
        page = pageRepository.save(new Page("Plan", "<p>Launch plan for Friday</p>", owner, workspace));
    }

    // ---------------------------------------------------------------- sharing

    @Test
    void ownerInvitesEditorAndViewerAndBothAccept() throws Exception {
        invite(owner, editor, "EDITOR").andExpect(status().isCreated());
        invite(owner, viewer, "VIEWER").andExpect(status().isCreated());

        // The invitee is told by email, not only by the bell icon
        verify(eventPublisher, atLeastOnce()).publish(argThat((AppEvent e) -> "email".equals(e.getType())
                && editor.getEmail().equals(e.getRecipient())));

        accept(editor).andExpect(status().isOk());
        accept(viewer).andExpect(status().isOk());

        mockMvc.perform(get("/api/workspaces/{id}/members/check/{userId}", workspace.getId(), editor.getId())
                .with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EDITOR"));
        mockMvc.perform(get("/api/workspaces/{id}/members/check/{userId}", workspace.getId(), viewer.getId())
                .with(as(owner)))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void onlyTheOwnerCanInviteAndInvitesAreEditorOrViewerOnly() throws Exception {
        makeMember(editor, WorkspaceRole.EDITOR);

        invite(editor, outsider, "VIEWER").andExpect(status().isForbidden());
        invite(outsider, viewer, "VIEWER").andExpect(status().isForbidden());
        invite(owner, outsider, "OWNER").andExpect(status().isBadRequest());
    }

    @Test
    void editorCanEditButViewerCannot() throws Exception {
        makeMember(editor, WorkspaceRole.EDITOR);
        makeMember(viewer, WorkspaceRole.VIEWER);
        String body = "{\"title\":\"Edited\",\"content\":\"new text\"}";

        mockMvc.perform(put("/api/pages/{id}", page.getId()).with(as(editor))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Edited"));

        mockMvc.perform(put("/api/pages/{id}", page.getId()).with(as(viewer))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(viewer)))
                .andExpect(status().isOk());
    }

    @Test
    void onlyEditorsAndOwnersCanCreatePagesInTheWorkspace() throws Exception {
        makeMember(editor, WorkspaceRole.EDITOR);
        makeMember(viewer, WorkspaceRole.VIEWER);
        String body = "{\"title\":\"New\",\"content\":\"hi\",\"workspace\":{\"id\":\"" + workspace.getId() + "\"}}";

        mockMvc.perform(post("/api/pages").with(as(viewer)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/pages").with(as(outsider)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/pages").with(as(editor)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                // the author is the signed-in editor, whatever the client sends
                .andExpect(jsonPath("$.user.id").value(editor.getId()));
    }

    @Test
    void outsiderCannotListWorkspacePagesOrMembers() throws Exception {
        makeMember(viewer, WorkspaceRole.VIEWER);

        mockMvc.perform(get("/api/pages/workspace/{id}", workspace.getId()).with(as(outsider)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/workspaces/{id}/members", workspace.getId()).with(as(outsider)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pages/workspace/{id}", workspace.getId()).with(as(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(page.getId()));
    }

    @Test
    void pageSharesCanOnlyBeManagedByThePageOwnerAndNeverSpoofed() throws Exception {
        String body = "{\"pageId\":\"" + page.getId() + "\",\"email\":\"" + viewer.getEmail()
                + "\",\"permission\":\"VIEW\",\"sharedById\":\"" + owner.getId() + "\"}";

        // outsider pretends to be the owner through the body: still refused
        mockMvc.perform(post("/api/shares").with(as(outsider)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/shares/page/{id}", page.getId()).with(as(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/shares").with(as(owner)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        // and the person it was shared with can now read it
        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(viewer))).andExpect(status().isOk());
    }

    // ------------------------------------------------------- shared with me

    @Test
    void sharedWithMeShowsOtherPeoplesPagesWithMyRoleAndNeverMyOwn() throws Exception {
        makeMember(viewer, WorkspaceRole.VIEWER);
        makeMember(editor, WorkspaceRole.EDITOR);

        mockMvc.perform(get("/api/pages/shared-with-me").with(as(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(page.getId()))
                .andExpect(jsonPath("$[0].myRole").value("VIEWER"))
                .andExpect(jsonPath("$[0].owner.id").value(owner.getId()))
                .andExpect(jsonPath("$[0].workspace.name").value("Team notes"))
                .andExpect(jsonPath("$[0].shared").value(true));

        mockMvc.perform(get("/api/pages/shared-with-me").with(as(editor)))
                .andExpect(jsonPath("$[0].myRole").value("EDITOR"));

        // the owner's own pages are not "shared with me"; neither is anything for an outsider
        mockMvc.perform(get("/api/pages/shared-with-me").with(as(owner)))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/pages/shared-with-me").with(as(outsider)))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/pages/shared-with-me/count").with(as(viewer)))
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void sharedWithMeDropsPagesOnceAccessIsRemoved() throws Exception {
        makeMember(viewer, WorkspaceRole.VIEWER);
        mockMvc.perform(get("/api/pages/shared-with-me").with(as(viewer)))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/workspaces/{id}/members/{userId}", workspace.getId(), viewer.getId())
                .with(as(owner))).andExpect(status().isOk());

        mockMvc.perform(get("/api/pages/shared-with-me").with(as(viewer)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------- recent history

    @Test
    void openingAPageAddsItToRecentHistoryOncePerPageNewestFirst() throws Exception {
        makeMember(viewer, WorkspaceRole.VIEWER);
        Page second = pageRepository.save(new Page("Second", "more", owner, workspace));

        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(viewer))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/{id}", second.getId()).with(as(viewer))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(viewer))).andExpect(status().isOk());

        mockMvc.perform(get("/api/pages/recent-history").with(as(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(page.getId()))
                .andExpect(jsonPath("$[0].shared").value(true))
                .andExpect(jsonPath("$[0].lastViewedAt").exists())
                .andExpect(jsonPath("$[1].id").value(second.getId()));

        // history is private to each person
        mockMvc.perform(get("/api/pages/recent-history").with(as(owner)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void refusedPageOpensAreNotRecorded() throws Exception {
        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(outsider))).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pages/recent-history").with(as(outsider)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recentHistoryForgetsDeletedPagesAndRemovedAccessAndCanBeCleared() throws Exception {
        makeMember(viewer, WorkspaceRole.VIEWER);
        Page temp = pageRepository.save(new Page("Temp", "x", owner, workspace));
        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(viewer))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/{id}", temp.getId()).with(as(viewer))).andExpect(status().isOk());

        mockMvc.perform(delete("/api/pages/{id}", temp.getId()).with(as(owner))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/recent-history").with(as(viewer)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(page.getId()));

        mockMvc.perform(delete("/api/workspaces/{id}/members/{userId}", workspace.getId(), viewer.getId())
                .with(as(owner))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/recent-history").with(as(viewer)))
                .andExpect(jsonPath("$.length()").value(0));

        makeMember(viewer, WorkspaceRole.VIEWER);
        mockMvc.perform(delete("/api/pages/recent-history").with(as(viewer))).andExpect(status().isOk());
        mockMvc.perform(get("/api/pages/recent-history").with(as(viewer)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ------------------------------------------------------------- admin only

    @Test
    void systemWideListsAreForAdminsOnly() throws Exception {
        mockMvc.perform(get("/api/pages").with(as(owner))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/workspaces").with(as(owner))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pages/search").param("keyword", "plan").with(as(owner)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pages").with(as(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/workspaces").with(as(admin))).andExpect(status().isOk());
    }

    @Test
    void adminCanReadAndDeleteAnyPageButNotEditIt() throws Exception {
        mockMvc.perform(get("/api/pages/{id}", page.getId()).with(as(admin))).andExpect(status().isOk());
        mockMvc.perform(put("/api/pages/{id}", page.getId()).with(as(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Hacked\",\"content\":\"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/pages/{id}", page.getId()).with(as(admin))).andExpect(status().isOk());
    }

    @Test
    void normalUsersCannotDeleteOthersPages() throws Exception {
        makeMember(editor, WorkspaceRole.EDITOR);
        mockMvc.perform(delete("/api/pages/{id}", page.getId()).with(as(editor))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/pages/{id}", page.getId()).with(as(outsider))).andExpect(status().isForbidden());
    }

    // --------------------------------------------------------------- settings

    @Test
    void settingsAreAdminOnlyAndHaveSafeDefaults() throws Exception {
        mockMvc.perform(get("/api/admin/settings").with(as(owner))).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/admin/settings").with(as(owner)).contentType(MediaType.APPLICATION_JSON)
                .content(settingsJson(true, "USER", true, true, true))).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/settings").with(as(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowRegistration").value(true))
                .andExpect(jsonPath("$.defaultRole").value("USER"))
                .andExpect(jsonPath("$.requireTwoFactor").value(true))
                .andExpect(jsonPath("$.emailNotifications").value(true))
                .andExpect(jsonPath("$.smsNotifications").value(true));
    }

    @Test
    void settingsCanBeSavedButNeverHandOutTheAdminRole() throws Exception {
        mockMvc.perform(put("/api/admin/settings").with(as(admin)).contentType(MediaType.APPLICATION_JSON)
                .content(settingsJson(false, "EDITOR", false, false, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowRegistration").value(false))
                .andExpect(jsonPath("$.defaultRole").value("EDITOR"));

        mockMvc.perform(get("/api/admin/settings").with(as(admin)))
                .andExpect(jsonPath("$.requireTwoFactor").value(false))
                .andExpect(jsonPath("$.emailNotifications").value(false));

        mockMvc.perform(put("/api/admin/settings").with(as(admin)).contentType(MediaType.APPLICATION_JSON)
                .content(settingsJson(true, "ADMIN", true, true, true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationSwitchBlocksSignUpAndDefaultRoleIsApplied() throws Exception {
        String username = "newbie" + shortId();
        String register = "{\"username\":\"" + username + "\",\"email\":\"" + username
                + "@example.com\",\"password\":\"Passw0rd!\",\"firstName\":\"New\",\"lastName\":\"Bie\"}";

        saveSettings(false, "USER", true);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isBadRequest());

        saveSettings(true, "EDITOR", true);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EDITOR"));
    }

    @Test
    void publicSignUpCannotPickItsOwnRole() throws Exception {
        String username = "sneaky" + shortId();
        String body = "{\"username\":\"" + username + "\",\"email\":\"" + username
                + "@example.com\",\"password\":\"Passw0rd!\",\"firstName\":\"Sne\",\"lastName\":\"Aky\",\"role\":\"ADMIN\"}";

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void twoFactorSwitchControlsWhetherLoginAsksForACode() throws Exception {
        User login = newUser("login", "USER");
        login.setPassword(passwordEncoder.encode("Passw0rd!"));
        userRepository.save(login);
        String credentials = "{\"username\":\"" + login.getUsername() + "\",\"password\":\"Passw0rd!\"}";

        saveSettings(true, "USER", true);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFactor").value(true));

        saveSettings(true, "USER", false);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFactor").value(false));
    }

    // ---------------------------------------------------------------- helpers

    private User newUser(String prefix, String role) {
        String id = prefix + shortId();
        User u = new User(id, id + "@example.com", "secret", prefix, "Tester");
        u.setRole(role);
        return userRepository.save(u);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private RequestPostProcessor as(User u) {
        return user(new UserPrincipal(u));
    }

    private void makeMember(User u, WorkspaceRole role) {
        workspaceMemberRepository.save(new WorkspaceMember(workspace, u, role));
    }

    private org.springframework.test.web.servlet.ResultActions invite(User by, User who, String role)
            throws Exception {
        return mockMvc.perform(post("/api/invitations/send").with(as(by))
                .param("workspaceId", workspace.getId())
                .param("email", who.getEmail())
                .param("role", role));
    }

    private org.springframework.test.web.servlet.ResultActions accept(User who) throws Exception {
        Notification invitation = notificationRepository.findByUserIdOrderByCreatedAtDesc(who.getId()).stream()
                .filter(n -> n.getType() == NotificationType.WORKSPACE_INVITE)
                .findFirst()
                .orElseThrow();
        return mockMvc.perform(post("/api/invitations/{id}/accept", invitation.getId()).with(as(who)));
    }

    private void saveSettings(boolean allowRegistration, String defaultRole, boolean requireTwoFactor)
            throws Exception {
        mockMvc.perform(put("/api/admin/settings").with(as(admin)).contentType(MediaType.APPLICATION_JSON)
                .content(settingsJson(allowRegistration, defaultRole, requireTwoFactor, true, true)))
                .andExpect(status().isOk());
    }

    private static String settingsJson(boolean allowRegistration, String defaultRole, boolean requireTwoFactor,
            boolean email, boolean sms) {
        return "{\"allowRegistration\":" + allowRegistration + ",\"defaultRole\":\"" + defaultRole
                + "\",\"requireTwoFactor\":" + requireTwoFactor + ",\"emailNotifications\":" + email
                + ",\"smsNotifications\":" + sms + "}";
    }
}

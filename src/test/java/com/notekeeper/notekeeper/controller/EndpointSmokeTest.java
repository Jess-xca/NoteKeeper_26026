package com.notekeeper.notekeeper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notekeeper.notekeeper.messaging.EventPublisher;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Walks through every endpoint the website uses, in the order a real person would,
 * with real JWTs. A call fails the test when it answers 5xx or an unexpected status.
 * All failures are printed together so one run shows the whole picture.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:smoketest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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
// Not @Transactional on purpose: every request commits on its own, like on a real server
class EndpointSmokeTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private EventPublisher eventPublisher;

    private final List<String> problems = new ArrayList<>();
    private int checked = 0;

    private String bearer;      // the member
    private String adminBearer; // the administrator

    // ---------------------------------------------------------------- the journey

    @Test
    void everyEndpointTheWebsiteUsesAnswersSensibly() throws Exception {
        User admin = new User("boss", "boss@example.com", passwordEncoder.encode("Passw0rd!"), "Big", "Boss");
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);
        adminBearer = "Bearer " + jwtUtil.generateToken(admin.getUsername());

        // ---- admin settings (also turn 2FA off so the test can sign in with one call)
        hit(admin(get("/api/admin/settings")));
        hit(admin(put("/api/admin/settings").contentType(JSON).content(
                "{\"allowRegistration\":true,\"defaultRole\":\"USER\",\"requireTwoFactor\":false,"
                        + "\"emailNotifications\":true,\"smsNotifications\":true}")));

        // ---- sign up, sign in
        hit(post("/api/auth/register").contentType(JSON).content(registration("maya")), 201);
        hit(post("/api/auth/register").contentType(JSON).content(registration("maya")), 400); // duplicate
        hit(post("/api/auth/login").contentType(JSON).content("{\"username\":\"maya\",\"password\":\"wrong\"}"), 400);
        MvcResult login = hit(post("/api/auth/login").contentType(JSON)
                .content("{\"username\":\"maya\",\"password\":\"Passw0rd!\"}"));
        JsonNode loginBody = json.readTree(login.getResponse().getContentAsString());
        bearer = "Bearer " + loginBody.get("token").asText();
        String myId = loginBody.get("user").get("id").asText();

        hit(member(get("/api/admin/settings")), 403);

        // ---- MongoDB read screen: admin only, and it must not fail when Mongo is not connected
        hit(admin(get("/api/admin/mongo/status")));
        hit(admin(get("/api/admin/mongo/page-revisions")));
        hit(admin(get("/api/admin/mongo/delivery-logs?channel=sms")));
        hit(member(get("/api/admin/mongo/status")), 403);
        hit(member(get("/api/admin/mongo/delivery-logs")), 403);
        hit(member(get("/api/auth/me")));
        hit(member(get("/api/auth/verify")));
        hit(member(post("/api/auth/logout")));
        hit(post("/api/auth/forgot-password").contentType(JSON).content("{\"email\":\"maya@example.com\"}"));
        hit(post("/api/auth/forgot-password").contentType(JSON).content("{}"), 400);
        hit(get("/api/auth/verify-reset-token").param("token", "nope"));
        hit(post("/api/auth/reset-password").contentType(JSON)
                .content("{\"token\":\"nope\",\"newPassword\":\"Abcdef1!\"}"), 400, 404);
        hit(post("/api/auth/send-2fa").contentType(JSON).content("{\"userId\":\"" + myId + "\"}"));
        hit(post("/api/auth/verify-2fa").contentType(JSON)
                .content("{\"userId\":\"" + myId + "\",\"code\":\"000000\"}"), 400);
        hit(get("/api/pages/my"), 401, 403); // no token

        // ---- a second person to share with
        MvcResult second = hit(post("/api/users").contentType(JSON).content(
                "{\"username\":\"noah\",\"email\":\"noah@example.com\",\"password\":\"Passw0rd!\","
                        + "\"firstName\":\"Noah\",\"lastName\":\"Ndayi\"}"), 201);
        String noahId = idOf(second);

        // ---- users
        hit(get("/api/users"), 401, 403);                                    // the public must not list people
        hit(member(get("/api/users")), 403);
        hit(admin(get("/api/users")));
        hit(member(get("/api/users/{id}", myId)));
        hit(member(get("/api/users/{id}", noahId)), 403);                   // not somebody else's account
        hit(admin(get("/api/users/{id}", noahId)));
        hit(member(get("/api/users/username/maya")));
        hit(member(get("/api/users/username/noah")), 403);
        hit(admin(get("/api/users/by-location").param("locationName", "Kigali")));
        hit(admin(get("/api/users/by-province-code").param("code", "KG")));
        hit(admin(get("/api/users/search").param("keyword", "may")));
        hit(member(get("/api/users/search").param("keyword", "may")), 403);
        hit(admin(get("/api/users/paginated").param("page", "0").param("size", "5")));
        hit(admin(get("/api/users/sorted").param("sortBy", "username")));
        hit(member(get("/api/users/count")));
        hit(member(get("/api/users/check-username").param("username", "free-name")));
        hit(member(put("/api/users/{id}", myId).contentType(JSON)
                .content("{\"firstName\":\"Maya\",\"lastName\":\"Mukamana\",\"phoneNumber\":\"0788000000\"}")));
        hit(member(put("/api/users/admin/{id}", myId).contentType(JSON).content("{\"role\":\"ADMIN\"}")), 403); // no self-promotion
        hit(admin(put("/api/users/admin/{id}", noahId).contentType(JSON).content("{\"firstName\":\"Noah\"}")));
        hit(member(put("/api/users/{id}", noahId).contentType(JSON).content("{\"firstName\":\"Hacked\"}")), 403);
        hit(member(delete("/api/users/{id}", noahId)), 403);
        hit(admin(put("/api/users/{id}/change-password", noahId).contentType(JSON)
                .content("{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"Passw0rd!2\"}")), 403);
        hit(member(put("/api/users/{id}/change-password", myId).contentType(JSON)
                .content("{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"Passw0rd!2\"}")));
        hit(member(put("/api/users/{id}/change-password", myId).contentType(JSON)
                .content("{\"currentPassword\":\"Passw0rd!2\",\"newPassword\":\"Passw0rd!\"}")));

        // ---- locations (public lookups)
        MvcResult province = hit(admin(post("/api/locations").contentType(JSON)
                .content("{\"name\":\"Kigali City\",\"code\":\"KGL\",\"type\":\"PROVINCE\"}")), 201);
        String provinceId = idOf(province);
        hit(admin(post("/api/locations/with-parent").param("parentCode", "KGL").contentType(JSON)
                .content("{\"name\":\"Gasabo\",\"code\":\"GSB\",\"type\":\"DISTRICT\"}")), 201);
        hit(post("/api/locations").contentType(JSON).content("{\"name\":\"X\",\"code\":\"X\",\"type\":\"PROVINCE\"}"), 401, 403);
        hit(member(post("/api/locations").contentType(JSON).content("{\"name\":\"X\",\"code\":\"X\",\"type\":\"PROVINCE\"}")), 403);
        hit(get("/api/locations"));
        hit(get("/api/locations/{id}", provinceId));
        hit(get("/api/locations/code/KGL"));
        hit(get("/api/locations/provinces"));
        hit(get("/api/locations/type/DISTRICT"));
        hit(get("/api/locations/{id}/children", provinceId));
        hit(get("/api/locations/search").param("keyword", "Kig"));
        hit(get("/api/locations/paginated").param("page", "0").param("size", "5"));
        hit(get("/api/locations/type/PROVINCE/paginated").param("page", "0").param("size", "5"));
        hit(admin(put("/api/locations/{id}", provinceId).contentType(JSON)
                .content("{\"name\":\"Kigali\",\"code\":\"KGL\",\"type\":\"PROVINCE\"}")));

        // ---- profile
        hit(member(get("/api/profiles/my")));
        hit(get("/api/profiles"), 401, 403);                                 // never public
        hit(member(get("/api/profiles")), 403);
        MvcResult myProfile = hit(member(get("/api/profiles/my")));
        String profileId = idOf(myProfile);
        hit(member(put("/api/profiles/{id}", profileId).contentType(JSON).content("{\"bio\":\"x\"}")), 403);
        hit(admin(get("/api/profiles/user/{id}", myId)));
        hit(admin(get("/api/profiles")));
        hit(admin(get("/api/profiles/theme/LIGHT")));
        hit(admin(get("/api/profiles/paginated")));
        hit(admin(get("/api/profiles/count")));
        hit(admin(get("/api/profiles/count/theme/LIGHT")));
        hit(admin(get("/api/profiles/{id}", profileId)));
        hit(admin(put("/api/profiles/{id}", profileId).contentType(JSON)
                .content("{\"bio\":\"Hello\",\"theme\":\"DARK\",\"language\":\"fr\"}")));

        // ---- workspaces
        MvcResult inbox = hit(member(get("/api/workspaces/my/inbox")));
        String inboxId = idOf(inbox);
        MvcResult ws = hit(member(post("/api/workspaces").contentType(JSON)
                .content("{\"name\":\"Study\",\"icon\":\"📚\",\"description\":\"Exam notes\",\"owner\":{\"id\":\""
                        + myId + "\"}}")), 201);
        String wsId = idOf(ws);
        hit(member(post("/api/workspaces").contentType(JSON)
                .content("{\"name\":\"Forged\",\"owner\":{\"id\":\"" + noahId + "\"}}")), 201);
        hit(admin(get("/api/workspaces/owner/{id}", noahId)));
        // another person's data through an id in the URL is refused; an admin may look
        hit(member(get("/api/workspaces/owner/{id}", noahId)), 403);
        hit(member(get("/api/pages/user/{id}", noahId)), 403);
        hit(member(get("/api/pages/favorites/{id}", noahId)), 403);
        hit(member(get("/api/notifications/user/{id}", noahId)), 403);
        hit(admin(get("/api/pages/user/{id}", noahId)));
        hit(member(get("/api/workspaces/{id}", wsId)));
        hit(member(get("/api/workspaces/my")));
        hit(member(get("/api/workspaces/my/shared")));
        hit(member(get("/api/workspaces/owner/{id}", myId)));
        hit(member(get("/api/workspaces/inbox/{id}", myId)));
        hit(member(get("/api/workspaces/empty/{id}", myId)));
        hit(member(get("/api/workspaces/owner/{id}/sorted", myId).param("sortBy", "name")));
        hit(member(get("/api/workspaces/owner/{id}/paginated", myId)));
        hit(member(get("/api/workspaces/count/owner/{id}", myId)));
        hit(member(get("/api/workspaces/{id}/count-pages", wsId)));
        hit(member(put("/api/workspaces/{id}", wsId).contentType(JSON)
                .content("{\"name\":\"Study 2026\",\"description\":\"Exam notes\",\"icon\":\"📚\"}")));
        // system-wide lists: admin only
        hit(member(get("/api/workspaces")), 403);
        hit(member(get("/api/workspaces/paginated")), 403);
        hit(member(get("/api/workspaces/search").param("keyword", "st")), 403);
        hit(member(get("/api/workspaces/count")), 403);
        hit(admin(get("/api/workspaces")));
        hit(admin(get("/api/workspaces/paginated")));
        hit(admin(get("/api/workspaces/search").param("keyword", "st")));
        hit(admin(get("/api/workspaces/count")));

        // ---- pages
        MvcResult page = hit(member(post("/api/pages").contentType(JSON)
                .content("{\"title\":\"Biology\",\"content\":\"<p>Cells and tissue</p>\",\"icon\":\"🧬\","
                        + "\"workspace\":{\"id\":\"" + wsId + "\"}}")), 201);
        String pageId = idOf(page);
        hit(member(post("/api/pages/quick-note").param("userId", myId).param("title", "Idea")
                .param("content", "Buy flowers")));
        hit(member(get("/api/pages/{id}", pageId)));
        hit(member(get("/api/pages/my")));
        hit(member(get("/api/pages/shared-with-me")));
        hit(member(get("/api/pages/shared-with-me/count")));
        hit(member(get("/api/pages/recent-history")));
        hit(member(get("/api/pages/user/{id}", myId)));
        hit(member(get("/api/pages/workspace/{id}", wsId)));
        hit(member(get("/api/pages/favorites/{id}", myId)));
        hit(member(get("/api/pages/archived/{id}", myId)));
        hit(member(get("/api/pages/inbox/{id}", myId)));
        hit(member(get("/api/pages/recent/{id}", myId)));
        hit(member(get("/api/pages/search/user/{id}", myId).param("keyword", "bio")));
        hit(member(get("/api/pages/user/{id}/sorted", myId).param("sortBy", "title")));
        hit(member(get("/api/pages/user/{id}/paginated", myId).param("page", "0").param("size", "5")));
        hit(member(get("/api/pages/count/user/{id}", myId)));
        hit(member(get("/api/pages/count/workspace/{id}", wsId)));
        hit(member(get("/api/pages/count/favorites/{id}", myId)));
        hit(member(get("/api/pages/weekly-stats/{id}", myId)));
        hit(member(get("/api/pages/activity/{id}", myId)));
        hit(member(put("/api/pages/{id}", pageId).contentType(JSON)
                .content("{\"title\":\"Biology 101\",\"content\":\"<p>Cells</p>\"}")));
        hit(member(put("/api/pages/{id}/toggle-favorite", pageId)));
        hit(member(put("/api/pages/{id}/toggle-archive", pageId)));
        hit(member(put("/api/pages/{id}/toggle-archive", pageId)));
        hit(member(put("/api/pages/{id}/move", pageId).param("workspaceId", inboxId)));
        hit(member(put("/api/pages/{id}/move", pageId).param("workspaceId", wsId)));
        hit(member(get("/api/pages")), 403);
        hit(member(get("/api/pages/paginated")), 403);
        hit(member(get("/api/pages/search").param("keyword", "bio")), 403);
        hit(admin(get("/api/pages")));
        hit(admin(get("/api/pages/paginated")));
        hit(admin(get("/api/pages/search").param("keyword", "bio")));
        hit(admin(get("/api/pages/{id}", pageId)));

        // ---- tags
        MvcResult tag = hit(member(post("/api/tags").contentType(JSON)
                .content("{\"name\":\"exam\",\"color\":\"#22C55E\"}")), 201);
        String tagId = idOf(tag);
        hit(member(get("/api/tags")));
        hit(member(get("/api/tags/{id}", tagId)));
        hit(member(get("/api/tags/name/exam")));
        hit(member(get("/api/tags/user/{id}", myId)));
        hit(member(get("/api/tags/unused")));
        hit(member(get("/api/tags/search").param("keyword", "ex")));
        hit(member(get("/api/tags/paginated")));
        hit(member(get("/api/tags/count")));
        hit(member(get("/api/tags/{id}/usage-count", tagId)));
        hit(member(get("/api/tags/check-name").param("name", "brand-new")));
        hit(member(put("/api/tags/{id}", tagId).contentType(JSON)
                .content("{\"name\":\"exams\",\"color\":\"#16A34A\"}")));

        // ---- attachments
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());
        MvcResult upload = hit(member(multipart("/api/attachments/upload").file(file)
                .param("pageId", pageId).param("userId", myId)));
        String attachmentId = idOf(upload);
        hit(member(get("/api/attachments/{id}", attachmentId)));
        hit(member(get("/api/attachments/page/{id}", pageId)));
        hit(member(get("/api/attachments/{id}/download", attachmentId)));
        hit(member(put("/api/attachments/{id}", attachmentId).param("newFileName", "renamed.txt")));
        String storedPath = json.readTree(upload.getResponse().getContentAsString()).get("filePath").asText();
        hit(get("/api/attachments/download-by-path").param("path", storedPath));                 // like an <img> tag
        hit(get("/api/attachments/download-by-path").param("path", "pom.xml"), 404);             // outside uploads
        hit(get("/api/attachments/download-by-path").param("path", "../../../../Windows/win.ini"), 404);
        hit(post("/api/attachments/upload").param("pageId", ""), 401, 403);
        hit(multipart("/api/attachments/upload").file(file).param("pageId", "").header("Authorization", bearer), 400);

        // ---- sharing: invite Noah as editor, share a page, notifications
        hit(member(post("/api/invitations/send").param("workspaceId", wsId).param("email", "noah@example.com")
                .param("role", "EDITOR")), 201);
        MvcResult noahLogin = hit(post("/api/auth/login").contentType(JSON)
                .content("{\"username\":\"noah\",\"password\":\"Passw0rd!\"}"));
        String noahBearer = "Bearer " + json.readTree(noahLogin.getResponse().getContentAsString()).get("token").asText();
        MvcResult noahNotes = hit(get("/api/notifications/my").header("Authorization", noahBearer));
        String inviteId = json.readTree(noahNotes.getResponse().getContentAsString()).get(0).get("id").asText();
        hit(get("/api/notifications/my/unread").header("Authorization", noahBearer));
        hit(get("/api/notifications/my/count").header("Authorization", noahBearer));
        hit(get("/api/notifications/user/{id}", noahId).header("Authorization", noahBearer));
        hit(get("/api/notifications/user/{id}/unread", noahId).header("Authorization", noahBearer));
        hit(get("/api/notifications/user/{id}/count", noahId).header("Authorization", noahBearer));
        hit(put("/api/notifications/{id}/mark-read", inviteId).header("Authorization", noahBearer));
        hit(put("/api/notifications/{id}/mark-unread", inviteId).header("Authorization", noahBearer));
        hit(put("/api/notifications/user/{id}/mark-all-read", noahId).header("Authorization", noahBearer));
        hit(put("/api/notifications/{id}/mark-unread", inviteId).header("Authorization", noahBearer));
        hit(post("/api/invitations/{id}/accept", inviteId).header("Authorization", noahBearer));
        hit(get("/api/workspaces/my/shared").header("Authorization", noahBearer));
        hit(get("/api/pages/shared-with-me").header("Authorization", noahBearer));
        hit(get("/api/pages/shared-with-me/count").header("Authorization", noahBearer));
        hit(get("/api/pages/{id}", pageId).header("Authorization", noahBearer));
        hit(get("/api/pages/recent-history").header("Authorization", noahBearer));
        hit(post("/api/invitations/{id}/decline", inviteId).header("Authorization", noahBearer), 400, 404);
        hit(member(get("/api/workspaces/{id}/members", wsId)));
        hit(member(get("/api/workspaces/{id}/members/check/{u}", wsId, noahId)));
        hit(member(put("/api/workspaces/{id}/members/{u}/role", wsId, noahId).param("role", "VIEWER")));
        hit(member(post("/api/workspaces/{id}/members", wsId).param("userId", noahId).param("role", "EDITOR")),
                200, 201, 400);

        MvcResult share = hit(member(post("/api/shares").contentType(JSON).content(
                "{\"pageId\":\"" + pageId + "\",\"email\":\"boss@example.com\",\"permission\":\"VIEW\"}")), 201);
        String shareId = idOf(share);
        hit(member(get("/api/shares/page/{id}", pageId)));
        hit(member(get("/api/shares/shared-by-me")));
        hit(admin(get("/api/shares/mine")));
        hit(member(put("/api/shares/{id}", shareId).contentType(JSON).content("{\"permission\":\"EDIT\"}")));
        hit(member(delete("/api/shares/{id}", shareId)));

        // ---- clean-up deletes, and a notification created by hand
        hit(member(post("/api/notifications").param("userId", myId).param("title", "Hi")
                .param("message", "Welcome").param("type", "INFO")), 201);
        hit(member(delete("/api/workspaces/{id}/members/{u}", wsId, noahId)));
        hit(member(delete("/api/attachments/{id}", attachmentId)));
        hit(member(delete("/api/tags/{id}", tagId)));
        hit(member(delete("/api/pages/recent-history")));
        hit(member(delete("/api/pages/{id}", pageId)));
        hit(member(delete("/api/workspaces/{id}", wsId)));
        hit(admin(delete("/api/users/{id}", noahId)));

        // ---- the old trouble spots: odd input must not turn into a 500
        hit(post("/api/auth/login").contentType(JSON).content("{}"), 400);
        hit(post("/api/auth/login").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("username", "x"), 415);
        hit(post("/api/auth/login").contentType(JSON).content("not json"), 400);
        hit(member(get("/api/pages/{id}", UUID.randomUUID().toString())), 403, 404);
        hit(member(get("/api/workspaces/{id}", UUID.randomUUID().toString())), 403, 404);
        hit(member(get("/api/users/{id}", UUID.randomUUID().toString())), 403); // not yours, so not even "missing"
        hit(admin(get("/api/users/{id}", UUID.randomUUID().toString())), 404);
        hit(member(get("/api/does-not-exist")), 404);

        System.out.println("ENDPOINT SMOKE TEST: " + checked + " calls, " + problems.size() + " problems");
        problems.forEach(p -> System.out.println("  PROBLEM " + p));
        assertTrue(problems.isEmpty(), "Endpoint problems:\n" + String.join("\n", problems));
    }

    // ---------------------------------------------------------------- helpers

    private MockHttpServletRequestBuilder member(MockHttpServletRequestBuilder b) {
        return b.header("Authorization", bearer);
    }

    private MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder b) {
        return b.header("Authorization", adminBearer);
    }

    /** Runs one call; no status list means "any 2xx is fine". 5xx is never fine. */
    private MvcResult hit(MockHttpServletRequestBuilder request, int... expected) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        int status = result.getResponse().getStatus();
        checked++;
        boolean ok = expected.length == 0
                ? status >= 200 && status < 300
                : Arrays.stream(expected).anyMatch(s -> s == status);
        if (!ok || status >= 500) {
            String query = result.getRequest().getQueryString();
            problems.add(result.getRequest().getMethod() + " " + result.getRequest().getRequestURI()
                    + (query == null ? "" : "?" + query) + " -> " + status
                    + (expected.length == 0 ? " (wanted 2xx)" : " (wanted " + Arrays.toString(expected) + ")")
                    + " " + abbreviate(result.getResponse().getContentAsString()));
        }
        return result;
    }

    /** Reads "id" from a response body; a body that is not valid JSON is reported as a problem. */
    private String idOf(MvcResult result) {
        try {
            return json.readTree(result.getResponse().getContentAsString()).get("id").asText();
        } catch (Exception e) {
            problems.add(result.getRequest().getMethod() + " " + result.getRequest().getRequestURI()
                    + " -> answered " + result.getResponse().getStatus() + " but the body is not usable JSON ("
                    + e.getClass().getSimpleName() + ")");
            return "missing-id";
        }
    }

    private static String abbreviate(String s) {
        return s.length() > 160 ? s.substring(0, 160) + "..." : s;
    }

    private static String registration(String username) {
        return "{\"username\":\"" + username + "\",\"email\":\"" + username + "@example.com\","
                + "\"password\":\"Passw0rd!\",\"firstName\":\"" + username + "\",\"lastName\":\"Tester\"}";
    }
}

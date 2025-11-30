package com.notekeeper.notekeeper.config;

import com.notekeeper.notekeeper.model.*;
import com.notekeeper.notekeeper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    public void run(String... args) throws Exception {
        if (locationRepository.count() == 0) {
            System.out.println("\n========================================");
            System.out.println(" Loading sample data...");
            System.out.println("========================================\n");

            loadLocations();
            loadUsersAndProfiles();
            loadWorkspaces();
            loadTags();
            loadPages();
            loadWorkspaceMembers();

            System.out.println("\n========================================");
            System.out.println(" Sample data loaded successfully!");
            System.out.println(" Statistics:");
            System.out.println("   - Locations: " + locationRepository.count());
            System.out.println("   - Users: " + userRepository.count());
            System.out.println("   - Profiles: " + userProfileRepository.count());
            System.out.println("   - Workspaces: " + workspaceRepository.count());
            System.out.println("   - Pages: " + pageRepository.count());
            System.out.println("   - Tags: " + tagRepository.count());
            System.out.println("   - Workspace Members: " + workspaceMemberRepository.count());
            System.out.println("\n TEST USERS:");
            System.out.println("   Username: derrick_mutabazi | Password: password123");
            System.out.println("   Username: ketsia_keza      | Password: password123");
            System.out.println("   Username: alice_uwase      | Password: password123");
            System.out.println("   Username: admin_user       | Password: admin123");
            System.out.println("========================================\n");
        }
    }

    private void loadLocations() {
        System.out.println("📍 Loading locations...");

        // KIGALI PROVINCE
        Location kigali = new Location("Kigali", "01", LocationType.PROVINCE);
        kigali = locationRepository.save(kigali);

        Location gasabo = new Location("Gasabo", "0101", LocationType.DISTRICT);
        gasabo.setParent(kigali);
        gasabo = locationRepository.save(gasabo);

        Location remera = new Location("Remera", "010101", LocationType.SECTOR);
        remera.setParent(gasabo);
        locationRepository.save(remera);

        Location kicukiro = new Location("Kicukiro", "0102", LocationType.DISTRICT);
        kicukiro.setParent(kigali);
        kicukiro = locationRepository.save(kicukiro);

        Location niboye = new Location("Niboye", "010201", LocationType.SECTOR);
        niboye.setParent(kicukiro);
        locationRepository.save(niboye);

        // EASTERN PROVINCE
        Location eastern = new Location("Eastern", "02", LocationType.PROVINCE);
        eastern = locationRepository.save(eastern);

        Location rwamagana = new Location("Rwamagana", "0201", LocationType.DISTRICT);
        rwamagana.setParent(eastern);
        rwamagana = locationRepository.save(rwamagana);

        Location fumbwe = new Location("Fumbwe", "020101", LocationType.SECTOR);
        fumbwe.setParent(rwamagana);
        locationRepository.save(fumbwe);

        // SOUTHERN PROVINCE
        Location southern = new Location("Southern", "03", LocationType.PROVINCE);
        southern = locationRepository.save(southern);

        Location huye = new Location("Huye", "0301", LocationType.DISTRICT);
        huye.setParent(southern);
        huye = locationRepository.save(huye);

        Location tumba = new Location("Tumba", "030101", LocationType.SECTOR);
        tumba.setParent(huye);
        locationRepository.save(tumba);

        System.out.println("✅ Loaded " + locationRepository.count() + " locations");
    }

    private void loadUsersAndProfiles() {
        System.out.println("👤 Loading users and profiles...");

        // Get locations for users
        Location remera = locationRepository.findByCode("010101").orElse(null);
        Location niboye = locationRepository.findByCode("010201").orElse(null);
        Location fumbwe = locationRepository.findByCode("020101").orElse(null);
        Location tumba = locationRepository.findByCode("030101").orElse(null);

        // User 1 - Derrick Mutabazi (Kigali - Remera) - ADMIN
        User derrick = new User("derrick_mutabazi", "derrick@example.com", "password123", "Derrick", "Mutabazi");
        derrick.setLocation(remera);
        derrick.setPhoneNumber("+250788123456");
        derrick.setDateOfBirth(LocalDate.of(1995, 8, 15));
        derrick.setGender("MALE");
        derrick.setRole("ADMIN");
        derrick = userRepository.save(derrick);

        UserProfile derrickProfile = new UserProfile(derrick);
        derrickProfile.setBio("Software developer passionate about Spring Boot and React");
        derrickProfile.setTheme("DARK");
        derrickProfile.setLanguage("en");
        derrickProfile.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Derrick");
        userProfileRepository.save(derrickProfile);

        // User 2 - Ketsia Keza (Kigali - Niboye) - EDITOR
        User ketsia = new User("ketsia_keza", "ketsia@example.com", "password123", "Ketsia", "Keza");
        ketsia.setLocation(niboye);
        ketsia.setPhoneNumber("+250788234567");
        ketsia.setDateOfBirth(LocalDate.of(1998, 3, 22));
        ketsia.setGender("FEMALE");
        ketsia.setRole("EDITOR");
        ketsia = userRepository.save(ketsia);

        UserProfile ketsiaProfile = new UserProfile(ketsia);
        ketsiaProfile.setBio("Project manager and tech enthusiast. Love organizing things!");
        ketsiaProfile.setTheme("LIGHT");
        ketsiaProfile.setLanguage("en");
        ketsiaProfile.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Ketsia");
        userProfileRepository.save(ketsiaProfile);

        // User 3 - Alice Uwase (Eastern - Fumbwe) - USER
        User alice = new User("alice_uwase", "alice@example.com", "password123", "Alice", "Uwase");
        alice.setLocation(fumbwe);
        alice.setPhoneNumber("+250788345678");
        alice.setDateOfBirth(LocalDate.of(2002, 11, 5));
        alice.setGender("FEMALE");
        alice.setRole("USER");
        alice = userRepository.save(alice);

        UserProfile aliceProfile = new UserProfile(alice);
        aliceProfile.setBio("Computer Science student at Rwanda Coding Academy. Aspiring software engineer.");
        aliceProfile.setTheme("LIGHT");
        aliceProfile.setLanguage("rw");
        aliceProfile.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Alice");
        userProfileRepository.save(aliceProfile);

        // User 4 - Admin User (Southern - Tumba) - ADMIN
        User admin = new User("admin_user", "admin@notekeeper.com", "admin123", "Admin", "User");
        admin.setLocation(tumba);
        admin.setPhoneNumber("+250788999999");
        admin.setDateOfBirth(LocalDate.of(1990, 1, 1));
        admin.setGender("MALE");
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);

        UserProfile adminProfile = new UserProfile(admin);
        adminProfile.setBio("System administrator");
        adminProfile.setTheme("DARK");
        adminProfile.setLanguage("en");
        adminProfile.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Admin");
        userProfileRepository.save(adminProfile);

        System.out.println("✅ Loaded " + userRepository.count() + " users with profiles");
    }

    private void loadWorkspaces() {
        System.out.println("📁 Loading workspaces...");

        // Get users
        User derrick = userRepository.findByUsername("derrick_mutabazi").orElse(null);
        User ketsia = userRepository.findByUsername("ketsia_keza").orElse(null);
        User alice = userRepository.findByUsername("alice_uwase").orElse(null);
        User admin = userRepository.findByUsername("admin_user").orElse(null);

        if (derrick != null) {
            // Derrick's Inbox (default)
            Workspace derrickInbox = new Workspace("Inbox", derrick, true);
            derrickInbox.setDescription("Quick capture notes");
            derrickInbox.setIcon("📥");
            workspaceRepository.save(derrickInbox);

            // Derrick's Work workspace
            Workspace derrickWork = new Workspace("Work Projects", derrick, false);
            derrickWork.setDescription("All work-related notes and tasks");
            derrickWork.setIcon("💼");
            workspaceRepository.save(derrickWork);
        }

        if (ketsia != null) {
            // Ketsia's Inbox (default)
            Workspace ketsiaInbox = new Workspace("Inbox", ketsia, true);
            ketsiaInbox.setDescription("Quick notes");
            ketsiaInbox.setIcon("📥");
            workspaceRepository.save(ketsiaInbox);

            // Ketsia's Personal workspace
            Workspace ketsiaPersonal = new Workspace("Personal", ketsia, false);
            ketsiaPersonal.setDescription("Personal notes and ideas");
            ketsiaPersonal.setIcon("🏠");
            workspaceRepository.save(ketsiaPersonal);
        }

        if (alice != null) {
            // Alice's Inbox (default)
            Workspace aliceInbox = new Workspace("Inbox", alice, true);
            aliceInbox.setDescription("Quick capture");
            aliceInbox.setIcon("📥");
            workspaceRepository.save(aliceInbox);

            // Alice's Study workspace
            Workspace aliceStudy = new Workspace("Study Notes", alice, false);
            aliceStudy.setDescription("School assignments and notes");
            aliceStudy.setIcon("📚");
            workspaceRepository.save(aliceStudy);
        }

        if (admin != null) {
            // Admin's Inbox (default)
            Workspace adminInbox = new Workspace("Inbox", admin, true);
            adminInbox.setDescription("Admin notes");
            adminInbox.setIcon("📥");
            workspaceRepository.save(adminInbox);
        }

        System.out.println("✅ Loaded " + workspaceRepository.count() + " workspaces");
    }

    private void loadTags() {
        System.out.println("🏷️ Loading tags...");

        Tag important = new Tag("Important", "#FF0000");
        tagRepository.save(important);

        Tag work = new Tag("Work", "#0066FF");
        tagRepository.save(work);

        Tag personal = new Tag("Personal", "#00CC66");
        tagRepository.save(personal);

        Tag urgent = new Tag("Urgent", "#FF6600");
        tagRepository.save(urgent);

        Tag ideas = new Tag("Ideas", "#9933FF");
        tagRepository.save(ideas);

        Tag study = new Tag("Study", "#FF33CC");
        tagRepository.save(study);

        Tag meeting = new Tag("Meeting", "#33CCFF");
        tagRepository.save(meeting);

        System.out.println("✅ Loaded " + tagRepository.count() + " tags");
    }

    private void loadPages() {
        System.out.println("📄 Loading pages...");

        // Get users and workspaces
        User derrick = userRepository.findByUsername("derrick_mutabazi").orElse(null);
        User ketsia = userRepository.findByUsername("ketsia_keza").orElse(null);
        User alice = userRepository.findByUsername("alice_uwase").orElse(null);

        if (derrick != null) {
            Workspace derrickInbox = workspaceRepository.findByOwnerIdAndIsDefaultTrue(derrick.getId()).orElse(null);
            Workspace derrickWork = workspaceRepository.findByOwnerId(derrick.getId()).stream()
                    .filter(w -> !w.getIsDefault()).findFirst().orElse(null);

            if (derrickInbox != null) {
                // Page in Inbox
                Page page1 = new Page("Meeting Notes", "Discussed Q1 2025 goals and objectives for the project",
                        derrick, derrickInbox);
                page1.setIcon("📝");
                page1.setIsFavorite(true);
                pageRepository.save(page1);

                Page page2 = new Page("Quick Idea", "New feature idea: Add dark mode to dashboard", derrick,
                        derrickInbox);
                page2.setIcon("💡");
                pageRepository.save(page2);
            }

            if (derrickWork != null) {
                // Pages in Work workspace
                Page page3 = new Page("Sprint Planning", "Tasks for the upcoming sprint - Backend API development",
                        derrick, derrickWork);
                page3.setIcon("📋");
                pageRepository.save(page3);

                Page page4 = new Page("Bug Reports", "List of bugs to fix: User authentication, Page deletion", derrick,
                        derrickWork);
                page4.setIcon("🐛");
                pageRepository.save(page4);
            }
        }

        if (ketsia != null) {
            Workspace ketsiaInbox = workspaceRepository.findByOwnerIdAndIsDefaultTrue(ketsia.getId()).orElse(null);
            Workspace ketsiaPersonal = workspaceRepository.findByOwnerId(ketsia.getId()).stream()
                    .filter(w -> !w.getIsDefault()).findFirst().orElse(null);

            if (ketsiaInbox != null) {
                Page page5 = new Page("Shopping List", "Groceries: Rice, Beans, Vegetables, Fruits", ketsia,
                        ketsiaInbox);
                page5.setIcon("🛒");
                pageRepository.save(page5);
            }

            if (ketsiaPersonal != null) {
                Page page6 = new Page("Book Recommendations", "1. Atomic Habits 2. The Lean Startup 3. Clean Code",
                        ketsia, ketsiaPersonal);
                page6.setIcon("📚");
                page6.setIsFavorite(true);
                pageRepository.save(page6);

                Page page7 = new Page("Travel Plans", "Summer vacation: Visit Akagera National Park", ketsia,
                        ketsiaPersonal);
                page7.setIcon("✈️");
                pageRepository.save(page7);
            }
        }

        if (alice != null) {
            Workspace aliceInbox = workspaceRepository.findByOwnerIdAndIsDefaultTrue(alice.getId()).orElse(null);
            Workspace aliceStudy = workspaceRepository.findByOwnerId(alice.getId()).stream()
                    .filter(w -> !w.getIsDefault()).findFirst().orElse(null);

            if (aliceInbox != null) {
                Page page8 = new Page("Random Thoughts", "Project idea: Note-taking app with AI features", alice,
                        aliceInbox);
                page8.setIcon("🤔");
                pageRepository.save(page8);
            }

            if (aliceStudy != null) {
                Page page9 = new Page("Web Tech Notes",
                        "Spring Boot: @RestController, @Service, JPA Repository patterns", alice, aliceStudy);
                page9.setIcon("💻");
                page9.setIsFavorite(true);
                pageRepository.save(page9);

                Page page10 = new Page("Database Design", "ERD for final project: Users, Locations, Pages, Workspaces",
                        alice, aliceStudy);
                page10.setIcon("🗄️");
                pageRepository.save(page10);
            }
        }

        System.out.println("✅ Loaded " + pageRepository.count() + " pages");
    }

    private void loadWorkspaceMembers() {
        System.out.println("👥 Loading workspace members...");

        // Get users
        User derrick = userRepository.findByUsername("derrick_mutabazi").orElse(null);
        User ketsia = userRepository.findByUsername("ketsia_keza").orElse(null);
        User alice = userRepository.findByUsername("alice_uwase").orElse(null);

        if (derrick != null && ketsia != null && alice != null) {
            // Get workspaces
            Workspace derrickWork = workspaceRepository.findByOwnerId(derrick.getId()).stream()
                    .filter(w -> !w.getIsDefault()).findFirst().orElse(null);
            Workspace ketsiaPersonal = workspaceRepository.findByOwnerId(ketsia.getId()).stream()
                    .filter(w -> !w.getIsDefault()).findFirst().orElse(null);

            // Add members to workspaces
            if (derrickWork != null) {
                WorkspaceMember member1 = new WorkspaceMember(derrickWork, ketsia, WorkspaceRole.EDITOR);
                workspaceMemberRepository.save(member1);
            }

            if (ketsiaPersonal != null) {
                WorkspaceMember member2 = new WorkspaceMember(ketsiaPersonal, alice, WorkspaceRole.VIEWER);
                workspaceMemberRepository.save(member2);
            }
        }

        System.out.println("✅ Loaded " + workspaceMemberRepository.count() + " workspace members");
    }
}
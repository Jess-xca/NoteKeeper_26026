# NoteKeeper

NoteKeeper is a lightweight note-taking web application built with Spring Boot, Spring Data JPA (Hibernate), and follows a layered architecture (controller → service → repository). It provides a REST API for managing users, profiles, locations, workspaces, pages (notes), tags and more. The project is designed as a small, extensible backend you can use as a starter for personal productivity applications.

## Features

- RESTful endpoints for core resources: Users, Profiles, Locations, Workspaces, Pages, Tags
- DTO-based API layer with `DTOMapper` to decouple persistence models from API models
- Location hierarchy support (province → district → sector etc.) with code-prefix queries
- Collection features: pagination, sorting, case-insensitive search, and filtering
- Simple initialization (data loader) and sensible defaults (Inbox workspace per user)

## Getting started

Prerequisites

- Java 17+ (or the JDK version configured in the project)
- Maven 3.6+

Clone the repository:

```powershell
git clone <repo-url>
cd notekeeper
```

## Running the app

You can run the app from the command line using Maven:

```powershell
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/api`

Build a package (skip tests during quick iteration):

```powershell
mvn -DskipTests package
```

If you prefer to run the packaged jar:

```powershell
java -jar target/notekeeper-0.0.1-SNAPSHOT.jar
```

## API documentation

See `API_DOCUMENTATION.md` for the full reference. Key endpoints exposed by this project:

- `/api/users`
- `/api/profiles`
- `/api/locations`
- `/api/workspaces`
- `/api/pages`
- `/api/tags`

## Code review notes

Quick orientation for reviewers:

- Project layout (src/main/java/com/notekeeper/notekeeper):

  - `controller/` — REST controllers and request mapping
  - `service/` — business logic
  - `repository/` — Spring Data JPA interfaces
  - `model/` — JPA entities
  - `dto/` and `mapper/` — DTO classes and mapping logic
  - `config/` — application initialization (data loader, etc.)

- Important files to check during review:
  - `DTOMapper.java` — entity ↔ DTO conversions
  - controllers for each resource (e.g., `UserController.java`)
  - `application.properties` — runtime configuration

When changing entities, update DTOs, mapper, and controller tests where applicable.

## Testing

Unit and integration tests are located under `src/test/java`. Run tests with:

```powershell
mvn test
```

During active development you can use `-DskipTests` for faster builds.

## Project structure

- `controller/` — REST controllers and HTTP mappings
- `service/` — business logic and transactional boundaries
- `repository/` — Spring Data JPA interfaces and custom JPQL queries
- `model/` — JPA entities (database mapping)
- `dto/`, `mapper/` — DTO classes and mapping logic
- `config/` — bootstrapping and initial data loader

## Database & entities

Note: persistence is via JPA/Hibernate. The application uses UUID string ids for primary keys.

User

- id (String, UUID)
- username (String, unique)
- email (String, unique)
- password (String, write-only in DTO)
- firstName, lastName (String)
- createdAt (LocalDateTime)
- location (Location)
- profile (UserProfile)

UserProfile

- id (String, UUID)
- user (User)
- bio, avatarUrl, theme, language (String)

Location

- id (String, UUID)
- name (String)
- code (String, unique)
- type (enum: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE)
- parent (Location)

Workspace

- id, name, description, icon, owner (User), isDefault (boolean)

Page (note)

- id, title, content, workspace, user, isFavorite, isArchived, timestamps

Tag

- id, name (unique), color, createdAt

## Key classes

- `DTOMapper` — central mapping logic between entities and DTOs
- `UserController`, `PageController`, `LocationController`, `WorkspaceController`, `TagController` — REST entry points
- `UserService`, `PageService`, etc. — business rules and persistence coordination
- Repositories (`UserRepository`, `LocationRepository`, ...) — derived queries and JPQL for specific needs (e.g., prefix code search)

## Common behaviors & constraints

- Username and email are unique (repository-level checks). API returns 409 on conflict.
- Password is accepted on create/update but excluded from responses (write-only via DTO/Jackson).
- Location deletion is prevented when children or users exist (returns 409).
- Search endpoints are case-insensitive and support partial matches (`LIKE %keyword%`).
- Province-level user lookups use code-prefix queries (`location.code LIKE '01%'`).

## Testing & build

- Unit and integration tests in `src/test/java` (run with `mvn test`).
- Build artifact: `target/notekeeper-0.0.1-SNAPSHOT.jar`.



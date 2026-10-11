# NoteKeeper problem, users, and requirements

## Problem statement

Students and small teams lose notes across chats, files, and personal devices. They need one place to write notes, keep them in a workspace, and share them without giving every person the same power to edit or delete.

NoteKeeper is a web application that stores notes in workspaces and lets the owner decide who can view or edit.

## Target users

- A student who writes and organizes personal notes.
- A teammate who is invited into a workspace as a viewer or an editor.
- An administrator who looks after user accounts.

## Objectives

- Let a person register, sign in, and keep notes in a workspace.
- Let a workspace owner invite someone as a viewer or an editor.
- Keep operational records in PostgreSQL and document-shaped history in MongoDB when the Docker stack is running.
- Send email, SMS, and other notifications through RabbitMQ when the Docker stack is running.

## Scope

In scope:

- Accounts, Google sign-in, and email verification codes.
- Workspaces, pages, tags, sharing, and roles.
- One dashboard for everyone. Administrators see a system overview and a Settings tab on it, and a Mine / Everyone switch on the Pages and Workspaces screens.
- A "Shared with me" screen and a "Recent history" screen.
- The Docker assignment stack: PostgreSQL, MongoDB, and RabbitMQ.

Out of scope:

- A paid SMS network. SMS events are stored in a delivery log.
- Replacing the current login screens.
- Moving existing notes out of the local PostgreSQL database on port 5432.

## Functional requirements

1. A visitor can register and sign in.
2. A signed-in person can create a workspace and a page, then edit that page.
3. A workspace owner can add a member as VIEWER or EDITOR.
4. A viewer can read a shared workspace and cannot manage members.
5. An administrator sees the system overview, the Settings tab, User Management, and the Everyone view of pages and workspaces. A normal user sees none of them.
6. Saving a page in the Docker stack writes the page to PostgreSQL and a copy of that version to MongoDB.
7. Share, invitation, verification-code, and password-reset messages are placed on RabbitMQ in the Docker stack.
8. A workspace owner invites a person by email as VIEWER or EDITOR. The invitee accepts. A viewer can read, an editor can edit, only the owner manages members and deletes the workspace.
9. "Shared with me" lists pages from other people's workspaces the person was invited to. Their own pages never appear there.
10. "Recent history" lists the pages the person opened, newest first, and can be cleared.
11. An administrator can turn registration, two-factor sign-in, email, and SMS on or off, and choose the default role for new users (USER or EDITOR).

## Quality attributes

- Usability: the sign-in page and workspace list remain usable at a phone width of 375 pixels. The layout already uses Tailwind breakpoints.
- Performance: page and workspace lists are paginated in the repository layer. Docker PostgreSQL is a new empty database, so list queries stay small during the demo.
- Reliability: the local database on port 5432 is not used by Docker. Docker PostgreSQL is published on port 5433.
- Security: protected API routes require a JWT. Passwords are stored with BCrypt. Workspace actions check OWNER, EDITOR, or VIEWER.

## User stories and acceptance criteria

### Story 1: Sign in

As a registered student, I want to sign in, so that I can reach my notes.

Acceptance criteria:

- An empty username or password shows the message "Please fill in all fields".
- A visitor who opens the dashboard is sent to the sign-in page.
- A request to a protected API route without a token is rejected.

### Story 2: Write a page

As a signed-in student, I want to create and edit a page in a workspace, so that my notes are saved.

Acceptance criteria:

- After save, the page is stored in PostgreSQL and is still there after refresh.
- In the Docker stack, MongoDB also stores one history document for that save.
- The editor can be used from a narrow browser window.

### Story 3: Invite a teammate

As a workspace owner, I want to invite a person as a viewer or an editor, so that I control what they can change.

Acceptance criteria:

- A viewer can read and cannot edit or delete the workspace.
- An editor can edit a page and cannot manage members.
- The owner can manage members.
- The invitation creates an email event. In the Docker stack that event is queued on RabbitMQ. If the invited person has a phone number, an SMS event is queued as well.

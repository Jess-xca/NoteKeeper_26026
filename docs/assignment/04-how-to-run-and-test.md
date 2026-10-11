# How to run and how to test

## Two ways to run

### Current local server

From `NoteKeeper_26026`:

```powershell
mvn spring-boot:run
```

This uses PostgreSQL database `notekeeper_db` on port 5432, the same as before. It does not require MongoDB or RabbitMQ. Email is sent directly through SMTP.

From `NoteKeeper_Frontend_26026`:

```powershell
npm start
```

The site is at `http://localhost:3000`. The API is at `http://localhost:8080`.

### Assignment stack with Docker

From `NoteKeeper_26026`:

```powershell
docker compose up --build
```

This starts:

- PostgreSQL on host port 5433. This is a new database. It does not replace port 5432.
- MongoDB on host port 27017.
- RabbitMQ on host port 5672. The management page is `http://localhost:15672` (guest / guest).
- The API on host port 8080, with Spring profile `docker`.
- The website on host port 3000.

In this mode, email, SMS, and app events go through RabbitMQ. Saving a page also writes a history document to MongoDB. If Docker is not running, this mode does not start. The local command above still works.

Stop it with `docker compose down`.

## Automated tests

`mvn test` uses an in-memory database. It does not connect to PostgreSQL on port 5432.

```powershell
mvn test
```

Frontend, from `NoteKeeper_Frontend_26026`:

```powershell
npm test -- --watchAll=false
```

A run is successful when the command finishes with no failed tests.

## What the automated tests check

- A viewer cannot edit a page or manage a workspace. An editor can edit. An owner can manage members.
- A protected route without a token is rejected. A valid token is accepted.
- An email, an SMS, and a page-shared event each have a type, a recipient, and a body.
- The RabbitMQ publisher addresses the `email`, `sms`, and `app-events` queues.
- A visitor who opens the dashboard is shown the sign-in page.
- A normal user who opens an admin route is sent to the dashboard. The old admin dashboard address leads an admin to the normal dashboard.
- Backend, two real users plus an admin: invite and accept; an editor can edit and a viewer gets 403; an outsider gets 403; a page appears in the invitee's "Shared with me" and never in the owner's; opening a page records history, and a page removed from access drops out of it.
- Backend: only an admin can read the system-wide page and workspace lists, an admin can read and delete but not edit someone else's page, and a public sign-up cannot choose its own role.
- Backend: the admin settings are saved, switching registration off blocks sign-up, switching two-factor off lets password sign-in skip the code, and a normal user gets 403 on the settings.
- Frontend: only an admin sees the system overview, the Settings tab, and the Mine / Everyone switch. Everyone adds an Author column.
- Frontend: Shared with me shows the role and who shared; Recent history groups by Today, Yesterday, and Earlier and can be cleared.
- An empty sign-in form shows "Please fill in all fields".

## Browser checklist for the Docker stack

1. Open `http://localhost:3000`, sign in, and reach the dashboard.
2. Create a workspace, create a page, type some text, save, and refresh. The text is still there.
3. Invite another user as a viewer. Sign in as that user and confirm they can read and cannot edit.
4. Narrow the window to about 375 pixels and confirm the sign-in form and workspace cards still fit.
5. Sign in as the invitee and open "Shared with me": the page is there with a Viewer or Editor badge. Open it, then check "Recent history".
6. Sign in as an admin. The dashboard shows the system overview above your own notes. Open the Settings tab, switch registration off, and confirm the register page now refuses a new account. Switch it back on.
7. As the admin, open Pages and choose Everyone: every page shows with its author.
8. Open RabbitMQ at `http://localhost:15672` (Queues tab) and confirm the `email`, `sms` and `app-events` queues exist. Messages are consumed at once, so look at the message rates.
9. As the admin, open **MongoDB Data** in the sidebar. **Page revisions** shows one entry for every save, with a View button for the full text. **Delivery log** shows the e-mail, SMS and app events that went through RabbitMQ, with filters. No MongoDB cluster or account is needed: the database is the `mongo` container in this compose file. For a raw view, MongoDB Compass can connect to `mongodb://127.0.0.1:27017` (not `localhost`, which can resolve to IPv6 on Windows), or open mongo-express at http://localhost:8081.
10. Sign in with 2FA on. The server console (`docker compose logs -f backend`, or the `mvn` window) prints one plain line, `[2FA] PIN for <email>: 123456`. The PIN is never shown in the UI. Use the e-mail you receive.

The **MongoDB Data** screen is admin-only and says "MongoDB is not connected" in a local run, because only the Docker stack uses MongoDB.


## Scripts

- `.\scripts\seed-demo-data.ps1`: fills the running Docker stack with demo users, workspaces, pages, shares and revisions.
- `.\scripts\docker-smoke.ps1`: calls every endpoint against http://localhost:8080 and prints PASS/FAIL (136 checks, exit code 1 on any failure).


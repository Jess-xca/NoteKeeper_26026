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
- A normal user who opens an admin route is sent to the dashboard.
- An empty sign-in form shows "Please fill in all fields".

## Browser checklist for the Docker stack

1. Open `http://localhost:3000`, sign in, and reach the dashboard.
2. Create a workspace, create a page, type some text, save, and refresh. The text is still there.
3. Invite another user as a viewer. Sign in as that user and confirm they can read and cannot edit.
4. Narrow the window to about 375 pixels and confirm the sign-in form and workspace cards still fit.
5. Open RabbitMQ at `http://localhost:15672` and confirm a share or sign-in action created a message. In MongoDB, confirm a document in `page_revisions`.

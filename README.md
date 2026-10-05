# Notes — Full Stack Exercise

A small single-page application to **create, edit, delete, archive and filter notes**, with
**categories** that can be attached to notes and used as filters.

The project is split into two independent applications:

| Folder      | Stack                                   | Responsibility               |
|-------------|-----------------------------------------|------------------------------|
| `backend`   | Spring Boot 3 (Java 21) + Spring Data JPA | REST API + persistence layer |
| `frontend`  | React 18 + Vite + TypeScript            | Single Page Application (UI) |

Data is persisted in **PostgreSQL** through the JPA/Hibernate ORM (no in-memory storage or mocks).

---

## Features

### Phase 1
- Create, edit and delete notes
- Archive / unarchive notes
- List active notes
- List archived notes

### Phase 2
- Create and delete categories
- Add / remove categories to a note
- Filter notes by one or more categories
- Search notes by text (matches title, content and category name)

---

## Architecture

The backend follows the **Service Layer pattern** with clear separation of concerns:

```
controller/   REST endpoints (HTTP layer, DTO <-> service)
service/      Business logic, transactions
repository/   Spring Data JPA repositories (DAO layer)
model/        JPA entities (Note, Category)
dto/          Request/response objects (decoupled from entities)
config/       CORS configuration
exception/    Domain exceptions + global handler
```

### REST API

| Method | Endpoint                                         | Description                          |
|--------|--------------------------------------------------|--------------------------------------|
| GET    | `/api/notes?archived=false&categoryIds=1,2`      | List notes (filter by archived/categories) |
| POST   | `/api/notes`                                     | Create a note                        |
| PUT    | `/api/notes/{id}`                                | Update a note                        |
| DELETE | `/api/notes/{id}`                                | Delete a note                        |
| PATCH  | `/api/notes/{id}/archive`                        | Archive a note                       |
| PATCH  | `/api/notes/{id}/unarchive`                      | Unarchive a note                     |
| POST   | `/api/notes/{noteId}/categories/{categoryId}`    | Attach a category to a note          |
| DELETE | `/api/notes/{noteId}/categories/{categoryId}`    | Detach a category from a note        |
| GET    | `/api/categories`                                | List categories                      |
| POST   | `/api/categories`                                | Create a category                    |
| DELETE | `/api/categories/{id}`                           | Delete a category                    |

---

## Testing

**Backend** — 29 tests (JUnit 5 + Mockito + Spring MockMvc): unit tests for the
service layer and web-layer tests (`@WebMvcTest`) for the controllers, covering
validation, error handling (400/404/409) and the happy paths.

```bash
cd backend
./mvnw test
```

**Frontend** — 13 tests (Vitest + Testing Library): the pure search filter, the
`NoteCard` component and an `App`-level integration test (mocked API) for loading
and searching notes.

```bash
cd frontend
npm test
```

---

## Requirements

You only need **three** tools installed; everything else (Maven, the JDK build deps, the
database) is bootstrapped by the run script / Maven wrapper.

| Tool             | Version used / minimum            | Notes                                            |
|------------------|-----------------------------------|--------------------------------------------------|
| **JDK**          | 21 (Temurin/Oracle 21.0.x)        | Required to build & run the backend              |
| **Node.js**      | 20+ (developed on 24.12.0)        | Required for the frontend                        |
| **npm**          | 10+ (developed on 11.6.2)         | Ships with Node.js                               |
| **Docker**       | 24+ with Docker Compose v2        | Runs PostgreSQL 16 (one-command startup)         |
| **Maven**        | *not required*                    | Provided via the Maven Wrapper (`./mvnw`, 3.9.9) |
| **PostgreSQL**   | 16 (via Docker)                   | Or a local instance, see "Manual run"           |

> The exercise targets a Linux/macOS environment. The single-command script is `run.sh`.

---

## Quick start (one command)

From the project root, on Linux/macOS:

```bash
./run.sh
```

This will:

1. Start a **PostgreSQL 16** container via Docker Compose and wait until it is healthy.
2. Start the **Spring Boot backend** on `http://localhost:8080`
   (the database **schema is created automatically** by Hibernate on startup).
3. Install frontend dependencies (first run only) and start the **Vite dev server**.

Then open **http://localhost:5173** in your browser.

Press `Ctrl+C` to stop the frontend and backend. To stop and remove the database container:

```bash
./run.sh --stop
```

### Configuration

The script reads these environment variables (sensible defaults shown):

```
DB_NAME=notesdb  DB_USER=notes  DB_PASSWORD=notes  DB_PORT=5432  SERVER_PORT=8080
```

---

## Manual run (without the script)

**1. Database** (any PostgreSQL works; with Docker):

```bash
docker compose up -d
```

**2. Backend:**

```bash
cd backend
./mvnw spring-boot:run
# API on http://localhost:8080
```

The backend connects to `jdbc:postgresql://localhost:5432/notesdb` with user/password
`notes`/`notes` by default. Override with the `DB_URL`, `DB_USER`, `DB_PASSWORD` env vars.

**3. Frontend:**

```bash
cd frontend
npm install
npm run dev
# SPA on http://localhost:5173 (proxies /api to the backend)
```

---

## Notes on persistence

- `spring.jpa.hibernate.ddl-auto=update` creates/updates the schema on startup, so no manual
  migration step is needed for this exercise.
- Categories are linked to notes through a `note_categories` join table (many-to-many).
- Deleting a category detaches it from all notes first to keep referential integrity.

---

## Login

This application has **no authentication** — it is a single-user notes app, so no login
screen or default credentials are required.

## Live deployment
http://144.22.63.154

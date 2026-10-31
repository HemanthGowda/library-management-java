# Intern Roadmap

This project ships with **partial** functionality on purpose. Use the implemented user registration, login, and book-update slices as the pattern library. The **Author** resource has no schema and no code—design it from scratch.

Product requirements for each remaining capability are in [tasks/README.md](tasks/README.md), one file per capability. This roadmap is the suggested pace. Follow the task file for what the product must do.

Suggested pace: 1–2 milestones per week. Adjust freely.

## Roles reminder

| Role | Intended capabilities |
|------|------------------------|
| `MEMBER` | Register, browse catalog, borrow/return (future) |
| `LIBRARIAN` | Manage catalog (books/authors), manage loans |
| `ADMIN` | Everything librarians can do + user administration |

---

## Phase 0 — Orientation (Day 1)

**Goal:** Run the stack and understand the reference slices.

Acceptance criteria:

- [ ] `docker compose up --build` succeeds
- [ ] Swagger UI loads and login works with the seeded admin
- [ ] You can identify controller → service → repository for users and books
- [ ] You can explain how JWT auth and `@PreAuthorize` protect `PUT /api/v1/books/{id}`

---

## Phase 1 — Author domain design (Week 1)

**Goal:** Propose the Author resource with **no production code yet**.

Deliverables:

- [ ] Short design note (`docs/design/author.md`) covering:
  - Fields (e.g. name, biography, birth year, nationality)
  - Indexes and uniqueness rules
  - Relationship to `Book` (embedded author name today vs. `authorId` reference)
  - API sketch (paths, methods, roles)
- [ ] Review/approval from mentor before coding

Acceptance criteria:

- [ ] Design answers create/read/update/delete + list/search
- [ ] Migration plan described (indexes + optional seed authors)
- [ ] Authz matrix defined per endpoint

---

## Phase 2 — Author persistence + migration (Week 1–2)

**Goal:** Create Mongo collection support the way books/users do.

Deliverables:

- [ ] `AuthorDocument` model
- [ ] `AuthorRepository`
- [ ] Mongock change unit for indexes (and optional seeds)
- [ ] Unit tests for any mapping/normalization helpers

Acceptance criteria:

- [ ] App starts with the new migration applied
- [ ] Unique constraints behave as designed
- [ ] No Author HTTP API yet (or only behind a feature flag—prefer none until Phase 3)

---

## Phase 3 — Author API vertical slice (Week 2)

**Goal:** Implement at least one full Author operation end-to-end, then complete CRUD.

Recommended order (mirror the starter style):

1. `POST /api/v1/authors` (create)
2. `GET /api/v1/authors/{id}`
3. `GET /api/v1/authors` (pagination + search)
4. `PUT /api/v1/authors/{id}`
5. `DELETE /api/v1/authors/{id}`

Acceptance criteria for each endpoint:

- [ ] Validation + `ApiError` responses
- [ ] Correct role checks
- [ ] Service unit tests
- [ ] Integration tests (auth success/failure + validation)
- [ ] OpenAPI annotations updated

---

## Phase 4 — Wire Authors into Books (Week 3)

**Goal:** Replace or complement the free-text `authorName` on books.

Options (pick one with mentor):

- Store `authorId` on `BookDocument` and resolve author details in responses
- Keep `authorName` denormalized and sync on author rename

Acceptance criteria:

- [ ] Migration strategy for existing seeded books
- [ ] Book update/create validation rejects unknown authors (if using references)
- [ ] Tests cover the relationship rules

Also complete remaining Book APIs using the update slice as a template:

- [ ] `POST /api/v1/books`
- [ ] `GET /api/v1/books/{id}`
- [ ] `GET /api/v1/books`
- [ ] `DELETE /api/v1/books/{id}`

---

## Phase 5 — Loans / borrow & return (Week 3–4)

**Goal:** Introduce a new aggregate from scratch (similar ambition to Authors).

Suggested model: `Loan` with `bookId`, `memberId`, `borrowedAt`, `dueAt`, `returnedAt`, `status`.

Acceptance criteria:

- [ ] Member can borrow an available copy
- [ ] Available copy count decrements/increments correctly
- [ ] Cannot borrow when no copies remain
- [ ] Return marks loan complete and restores availability
- [ ] Librarians can list overdue loans
- [ ] Full test + OpenAPI coverage

---

## Phase 6 — User administration & hardening (Week 4+)

Stretch goals:

- [ ] Admin endpoints to list users, change roles, enable/disable accounts
- [ ] Refresh tokens or shorter-lived access tokens
- [ ] Audit fields / request logging
- [ ] CI workflow running `mvn test`
- [ ] Rate limiting / lockout for login brute force

---

## Definition of Done (every feature)

1. Code follows existing package and naming patterns
2. Validations and authorization are explicit
3. Mongock migration added when persistence shape changes
4. Unit + integration tests pass locally
5. Swagger documents the new surface area
6. Roadmap checkboxes updated in this file

## Reference endpoints (already done)

Use these as the gold standard:

- `POST /api/v1/users`
- `POST /api/v1/auth/login`
- `PUT /api/v1/books/{id}`

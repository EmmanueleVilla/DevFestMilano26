# Data Model: APOD App

Entities derived from `spec.md` ("Key Entities") and `research.md` (storage/sync decisions).
Field names are logical (domain) names — Room/Firestore mapping notes follow each entity where it
differs.

## AstronomyPicture (domain model)

A single day's NASA APOD entry. Identity is its `date` — NASA publishes at most one entry per
calendar date (spec Assumption).

| Field | Type | Notes |
|---|---|---|
| `date` | `LocalDate` | Identity. ISO `yyyy-MM-dd` on the wire and in storage. |
| `title` | `String` | Required. |
| `explanation` | `String` | Required; full text (Today screen shows a 2–3 line preview of this same field, not a separate field). |
| `url` | `String` (URL) | Required. Standard-resolution image or video reference. |
| `hdUrl` | `String?` (URL) | Optional; present only for some image entries, never for video. |
| `mediaType` | `MediaType` enum: `IMAGE`, `VIDEO` | Drives the Today/Detail video badge (FR-002). |
| `copyright` | `String?` | Optional; omitted entirely in the UI when absent (FR-005, edge case). |

**Validation rules**: `date`, `title`, `explanation`, `url`, `mediaType` are always present once an
entry is successfully fetched (an incomplete fetch is an Error state, not a partial entity —
research.md / plan.md Technical Context). `hdUrl` is only ever non-null when `mediaType == IMAGE`.

## Favorite (Room entity: `favorites` table)

A user's saved reference to an `AstronomyPicture`. Room is the single local source of truth the UI
reads from (research.md "Sync").

| Field | Type | Notes |
|---|---|---|
| `date` | `String` (PK, ISO `yyyy-MM-dd`) | Same identity as `AstronomyPicture.date`. |
| `title` | `String` | Denormalized copy of the picture's title, so Favorites can render its grid without re-fetching each picture. |
| `explanation` | `String` | Denormalized, needed if the user reopens a favorite's Detail view while offline. |
| `url` | `String` | Denormalized thumbnail/media source. |
| `hdUrl` | `String?` | Denormalized. |
| `mediaType` | `String` (stored as `IMAGE`/`VIDEO`) | Denormalized. |
| `copyright` | `String?` | Denormalized. |
| `syncStatus` | `String` enum: `LOCAL_ONLY`, `SYNCED` | FR-010. `LOCAL_ONLY` when saved while logged out; `SYNCED` when saved while logged in or migrated on login (FR-015). |
| `ownerUid` | `String?` | Firebase `uid` this favorite is synced under; `null` when `syncStatus == LOCAL_ONLY`. |
| `savedAtEpochMillis` | `Long` | Used only for stable Favorites grid ordering; not user-facing. |

**Validation rules**: a row with `syncStatus == SYNCED` always has a non-null `ownerUid`; a row
with `syncStatus == LOCAL_ONLY` always has a null `ownerUid`. Primary key `date` means saving an
already-favorited date again is an update, not a duplicate insert — this is how the save-control
toggle behavior (FR-006, edge case: "save acts as a toggle") is implemented at the data layer:
insert-or-replace on save, delete-by-`date` on unsave.

**State transitions** (ties to spec User Story 4 / FR-013–FR-015):

```text
(no row) --save while logged out--> LOCAL_ONLY (ownerUid = null)
(no row) --save while logged in--> SYNCED (ownerUid = current uid)
LOCAL_ONLY --user logs in--> SYNCED (ownerUid = newly signed-in uid)   [FR-015 migration]
LOCAL_ONLY or SYNCED --unsave--> (row deleted; if it was SYNCED, the mirrored Firestore doc is also deleted)
```

## CachedTodayEntry (Room entity: `cached_today` table)

The last successfully fetched `AstronomyPicture`, kept so the Today screen has something to show
(per spec Edge Cases, offline/fetch-failure is always an Error state over the previous content,
never a blank screen) — this is a single-row cache, not a history/archive (plan.md explicitly
scopes archive browsing out).

| Field | Type | Notes |
|---|---|---|
| `date` | `String` (PK) | Always overwritten — only the most recent successful fetch is kept. |
| `title`, `explanation`, `url`, `hdUrl`, `mediaType`, `copyright` | same as `AstronomyPicture` | Mirrors the domain model 1:1; no favorite-specific fields. |
| `fetchedAtEpochMillis` | `Long` | Used to decide whether a background refresh is due; not shown to the user. |

## UserSession (DataStore Preferences, not Room)

Small, non-sensitive session flags — never tokens or credentials (research.md "Secrets handling").

| Field | Type | Notes |
|---|---|---|
| `isSignedIn` | `Boolean` | Mirrors `FirebaseAuth.currentUser != null`; cached here only so UI can render synchronously before Firebase's own async state is confirmed. |
| `uid` | `String?` | Convenience copy of the current Firebase `uid`, `null` when signed out. |
| `email` | `String?` | Convenience copy, for display only (e.g., an account menu) — not used for any security decision. |

**Validation rules**: `uid`/`email` are always null together with `isSignedIn == false`, and both
non-null when `isSignedIn == true`. This entity is a read-through cache of Firebase Auth's own
state, never the source of truth for whether a request is authorized.

## Relationships

- `Favorite.date` and `CachedTodayEntry.date` both reference the same logical identity as
  `AstronomyPicture.date`, but there is no foreign key between the three — a favorite can exist
  for a date that is no longer the cached "today" entry, and the cached today entry is replaced
  independently of favorites.
- `Favorite.ownerUid` relates to `UserSession.uid` only in the sense that sync use cases compare
  them to decide what to migrate on login/logout; there is no enforced referential integrity
  across Room and DataStore (different storage engines).
- The Firestore mirror (`users/{uid}/favorites/{date}`, see `contracts/firestore-schema.md`) is a
  per-user, per-date document whose shape matches `Favorite` minus `syncStatus`/`ownerUid` (both
  implicit: every document under a user's collection is, by definition, that user's synced
  favorite).

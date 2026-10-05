# Contract: Favorites Repository (domain port)

This is the internal interface contract between `domain` (use cases) and `data` (Room/Firestore
implementations) — the boundary constitution Principle I requires `domain` not to cross. Presented
as a contract because it's the interface every presentation ViewModel in this feature ultimately
depends on (indirectly, through use cases).

## Interface: `FavoritesRepository` (declared in `domain/repository`)

| Method | Signature (conceptual) | Behavior |
|---|---|---|
| Observe all favorites | `observeFavorites(): Flow<List<Favorite>>` | Emits the current Room `favorites` table contents, and re-emits on any change (insert/update/delete) — backs the Favorites grid (FR-008) directly, including its empty state (FR-009) when the list is empty. |
| Observe one favorite's status | `observeFavoriteStatus(date: LocalDate): Flow<SyncStatus?>` | Emits `null` when not favorited, otherwise the current `LOCAL_ONLY`/`SYNCED` status — backs the Today/Detail favorite-toggle appearance (FR-004, FR-006). |
| Save a favorite | `suspend fun saveFavorite(picture: AstronomyPicture): Result<Unit>` | Insert-or-replace by `date` (toggle semantics, data-model.md). Sets `syncStatus`/`ownerUid` from the current session: `SYNCED` + current `uid` if signed in, else `LOCAL_ONLY` + null. If `SYNCED`, also writes the mirrored Firestore document (`contracts/firestore-schema.md`); a Firestore write failure does not fail the local save (Room remains source of truth; Firestore's own offline queue will retry per research.md). |
| Remove a favorite | `suspend fun removeFavorite(date: LocalDate): Result<Unit>` | Deletes the Room row by `date`. If it was `SYNCED`, also deletes the mirrored Firestore document. |
| Migrate local favorites on login | `suspend fun migrateLocalFavoritesToAccount(uid: String): Result<Unit>` | For every `LOCAL_ONLY` row: set `syncStatus = SYNCED`, `ownerUid = uid`, and write it to Firestore (FR-015). Called once, right after a successful sign-in. |

## Interface: `TodayPictureRepository` (declared in `domain/repository`)

| Method | Signature (conceptual) | Behavior |
|---|---|---|
| Get today's picture | `suspend fun getTodayPicture(): Result<AstronomyPicture>` | Fetches from the NASA APOD API (`contracts/apod-api.md`); on success, also overwrites the `cached_today` Room row. On failure, the use case layer (not this method) decides whether to fall back to the cache or surface Error — this method itself always reflects the live network attempt. |
| Observe the cached today entry | `observeCachedTodayPicture(): Flow<AstronomyPicture?>` | Emits the last successfully cached entry, or `null` if none yet exists (first-ever launch with no connectivity). |

## Interface: `SessionRepository` (declared in `domain/repository`)

| Method | Signature (conceptual) | Behavior |
|---|---|---|
| Observe session | `observeSession(): Flow<UserSession>` | Backs every screen's knowledge of signed-in state (e.g., whether a newly saved favorite becomes `LOCAL_ONLY` or `SYNCED`). |
| Sign in with email/password | `suspend fun signInWithEmail(email: String, password: String): Result<Unit>` | Delegates to Firebase Auth; on success, triggers `migrateLocalFavoritesToAccount`. |
| Sign in with Google | `suspend fun signInWithGoogle(): Result<Unit>` | Delegates to Firebase Auth via the Credential Manager flow (research.md); on success, triggers `migrateLocalFavoritesToAccount`. |
| Continue without an account | `suspend fun continueWithoutAccount(): Unit` | No Firebase Auth call — simply allows navigation past the Login screen (FR-012). |
| Sign out | `suspend fun signOut(): Unit` | Delegates to Firebase Auth; local `favorites` rows are not deleted, but future saves/removals revert to `LOCAL_ONLY` behavior per the current (now signed-out) session. |

## Notes for implementers

- None of these interfaces reference `Room`, `Firestore`, `Retrofit`, or `Firebase` types in their
  signatures — only `domain/model` types (`AstronomyPicture`, `Favorite`, `SyncStatus`,
  `UserSession`) and standard Kotlin/coroutines types, per constitution Principle I.
- `Result<Unit>` (or a feature-specific sealed `Outcome`) is used instead of throwing, so
  ViewModels can map failures directly to the spec's per-screen Error state (FR-017) without
  catching exceptions across the `domain`/`presentation` boundary.

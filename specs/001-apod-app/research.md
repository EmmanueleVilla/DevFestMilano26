# Phase 0 Research: APOD App

All NEEDS CLARIFICATION items from the Technical Context are resolved below. Each entry was
verified against current library documentation via Context7 (per constitution Principle V) unless
noted otherwise.

## Architecture: MVI state holder shape

**Decision**: One `ViewModel` per screen (`TodayViewModel`, `DetailViewModel`,
`FavoritesViewModel`, `LoginViewModel`), each exposing a single `StateFlow<ScreenState>` and
accepting a sealed `Intent` type via a single `onIntent(intent: Intent)` entry point. One-off
effects (navigation, snackbars) go through a `Channel`/`SharedFlow` of a sealed `Effect` type, not
through `State`, so they aren't redelivered on recomposition/process restart.

**Rationale**: Matches constitution Principle I's "single state holder per feature" requirement
and is the standard MVI shape already assumed by `compose-skill`. Keeping `State`/`Intent`/`Effect`
as plain sealed classes in `domain`-adjacent `presentation` code (not `data`) keeps the ViewModel
free of Android/Firebase/Room types, so it stays trivially unit-testable.

**Alternatives considered**: A single app-wide state holder (rejected — couples unrelated screens
and contradicts "per feature" state holder guidance); exposing `LiveData` instead of `StateFlow`
(rejected — the project is Compose + coroutines-first, `StateFlow` is the native fit, no reason to
add a LiveData adapter layer).

## Dependency injection: Hilt

**Decision**: Hilt for all DI — `@HiltAndroidApp` on the `Application` class, `@AndroidEntryPoint`
on `MainActivity`, `@HiltViewModel` + constructor injection on every ViewModel, retrieved in
Compose via `hiltViewModel()`.

**Rationale**: Explicitly requested, and verified via Context7 (`/websites/dagger_dev_hilt`) as the
current, standard approach: `@HiltViewModel` constructor injection, consumed through the
`ViewModelComponent`, is still the documented pattern — no API changes to account for.

**Alternatives considered**: Manual DI / Koin — rejected, Hilt was explicitly requested in this
feature's input and is already idiomatic for this stack.

## Local persistence: Room + Preferences DataStore

**Decision**: Room owns all structured, queryable local data — a single `favorites` table (see
data-model.md) plus a small `cached_today` table holding the last successfully fetched Today
entry for offline display. Preferences DataStore holds only small, non-sensitive session/UI state
(e.g., "is a user currently signed in", last-seen onboarding dismissal) — never tokens or
credentials.

**Rationale**: Verified via Context7 (`/websites/developer_android_training_data-storage_room`,
`/websites/developer_android_topic_libraries_architecture_datastore`). Room's current guidance:
DAOs should expose `Flow` for observed reads and `suspend fun` for writes (Room 3.x is
coroutines-first). DataStore's `edit`/`data` Flow API is unchanged and appropriate for the small
amount of session state this app needs. Splitting "structured, queried data" (Room) from "a
handful of key-value flags" (DataStore) avoids forcing everything through one mechanism.

**Alternatives considered**: Using DataStore (Proto) for favorites too — rejected, favorites need
relational querying (list, lookup by date, delete) that Room's DAO/SQL model fits far better than
a single serialized blob. Plain `SharedPreferences` — rejected, DataStore is the current
Jetpack-recommended replacement and the project already targets modern Jetpack libraries.

## Networking: Retrofit + kotlinx.serialization

**Decision**: A small Retrofit `ApodApiService` interface with a single `suspend fun
getAstronomyPictureOfTheDay(date: String? = null): ApodDto` call against
`https://api.nasa.gov/planetary/apod`, using `kotlinx-serialization-json`'s
`Json.asConverterFactory(...)` as the converter and an `OkHttpClient` with a logging interceptor
enabled only in debug builds.

**Rationale**: Verified via Context7 (`/lysine-dev/retrofit`): Kotlin `suspend` functions are
natively supported by Retrofit (no extra call-adapter dependency), and the
`kotlinx-serialization` converter module integrates with a single `addConverterFactory(...)` call
— consistent with the project already using Kotlin serialization for Navigation Compose routes
(see below), so one serialization library covers both needs.

**Alternatives considered**: Ktor client — a reasonable alternative but would introduce a second
HTTP stack alongside Firebase's own networking; Retrofit is the more conventional choice for a
small single-endpoint REST client and was explicitly implied by the "Compose + MVI" conventional
Android stack.

## Image loading: Coil 3 (Compose)

**Decision**: Coil's `AsyncImage` for the Today/Detail hero image and Favorites grid thumbnails,
using `ImageRequest.Builder(...).crossfade(true)` and explicit `loading`/`error` slots so the
design brief's dedicated loading/error treatments (not a generic spinner) can be implemented per
screen rather than relying on Coil's defaults.

**Rationale**: Verified via Context7 (`/websites/coil-kt_github_io_coil`): `AsyncImage` supports
per-state content slots (`loading`, `error`, or a full `state`-driven content lambda), which maps
directly onto the design brief's requirement for a custom ink-line loading motif and a rust-toned
error frame instead of Coil's default circular progress indicator.

**Alternatives considered**: Glide (rejected — no first-party Compose API as smooth as Coil's);
`rememberAsyncImagePainter` with manual `Image(...)` (viable, slightly more boilerplate than
`AsyncImage`'s content-slot API for this feature's needs — not chosen).

## Navigation: Navigation Compose, type-safe routes

**Decision**: `androidx.navigation:navigation-compose` with `kotlinx.serialization`-backed,
type-safe destinations (`@Serializable data object Today`, `@Serializable data class
Detail(val date: String)`, etc.) and `composable<Route> { ... }` / `navController.navigate(Route(...))`
instead of string routes.

**Rationale**: Verified via Context7 (`/websites/developer_android_develop_ui_compose`): this is
the currently documented approach (navigation-compose 2.10.x + the Kotlin serialization Gradle
plugin), replacing the older string-route + `NavType` argument pattern. Using the same
`kotlinx.serialization` dependency as the Retrofit converter avoids a second JSON library.

**Alternatives considered**: String-based routes with manual argument parsing — rejected, it's the
now-superseded pattern for this exact library version.

## Authentication: Firebase Auth (email/password + Google)

**Decision**: Firebase Authentication for both the email/password flow and Google sign-in, with
Google sign-in obtained through Credential Manager (`GetGoogleIdOption` → `GetCredentialRequest`)
and exchanged for a Firebase credential via `GoogleAuthProvider.getCredential(...)` +
`FirebaseAuth.signInWithCredential(...)`. "Continue without an account" simply skips Firebase Auth
entirely — the app has no concept of an anonymous Firebase Auth user for this feature, since local
favorites don't need a `uid` at all.

**Rationale**: Firebase's own Android quickstart (`/firebase/quickstart-android`) confirms
email/password sign-in and Google sign-in as the two supported flows this feature needs.
Credential Manager is Google's current recommended replacement for the deprecated
`GoogleSignInClient`/`GoogleSignInOptions` APIs; the exact current call signatures (`GetGoogleIdOption`
construction, exception types) MUST be re-verified via Context7/Google's docs at implementation
time per constitution Principle V, since this research pass found the high-level flow but not a
single authoritative current code sample.

**Alternatives considered**: The legacy `GoogleSignInClient` API — rejected as it is
Google-deprecated in favor of Credential Manager; rolling custom email/password auth — rejected,
reinvents a solved, security-sensitive problem the spec doesn't ask for.

## Sync: Cloud Firestore, Room as local cache

**Decision**: Room is always the single source of truth the UI reads from. When the user is
logged in, a sync use case mirrors favorites between Room and a per-user Firestore collection
(`users/{uid}/favorites/{date}`, see `contracts/firestore-schema.md`) using Firestore's
snapshot listeners (so remote changes flow back into Room) and Firestore's default persistent
offline cache (so the sync layer itself tolerates transient connectivity loss). Login triggers a
one-time migration of any existing `LOCAL_ONLY` rows to `SYNCED`, writing them up to Firestore
(FR-015).

**Rationale**: Verified via Context7 (`/firebase/firebase-android-sdk`): `PersistentCacheSettings`
is the default local cache since recent SDK versions (no manual opt-in required), and
`addSnapshotListener` is the documented mechanism for observing remote changes. Keeping Room as the
single UI-facing source of truth (rather than reading directly from Firestore in the UI layer)
means Today/Detail/Favorites behave identically whether the user is online, offline, or logged
out — only the sync use case needs to know about Firestore at all, which keeps `domain` free of
Firebase types per constitution Principle I.

**Alternatives considered**: Reading Firestore directly from the presentation layer when logged in
— rejected, it would special-case every screen for auth state and break the "domain has no
data-layer dependency" rule; Firebase Realtime Database — rejected, Firestore's per-document model
and offline persistence fit this feature's "list of favorite documents" shape better.

## Secrets handling (constitution Principle II)

**Decision**: The NASA APOD API key is read from a gitignored `local.properties` entry and exposed
to app code only as a generated `BuildConfig` field (never committed, never a literal in source).
`google-services.json` is added to the project as usual for Firebase but is checked for accidental
sensitive overrides before commit; Firebase Auth/Firestore tokens are never read out of the SDK or
persisted by this app's own code — Room/DataStore store only non-sensitive identifiers (`uid`,
`email`, sync status), never tokens.

**Rationale**: Satisfies "no hardcoded secrets" and "no plaintext token storage" directly;
`BuildConfig` fields generated from `local.properties` are the standard Android pattern for
per-developer/per-environment keys that must stay out of version control.

**Alternatives considered**: Checking the NASA API key into `libs.versions.toml` or source directly
— rejected outright, violates constitution Principle II.

## Design tokens: Stitch "Observatory Logbook" design system

**Decision**: The color and typography values already authored in the project's Stitch workspace
(`projects/207351743317047103`, design system `Observatory Logbook`) are the canonical source for
`MaterialTheme.colorScheme` and `MaterialTheme.typography` — not the more generic placeholder
values in `design-brief.md`. Concretely: surface `#0B0D10` / raised surface `#14171B`, primary
(copper) `#B87333` with pressed state `#D98E54`, on-surface `#F2EFEA`, muted on-surface `#9C958B`,
hairline `#2A2D31`, synced indicator `#6E8B74`, error/rust `#9E4B3D`; display type in EB Garamond,
body in Work Sans, labels/metadata in JetBrains Mono; zero border-radius throughout; hairlines
instead of shadows for elevation. The four Stitch screens (`Today`, `Detail`, `Favorites`,
`Account Access`) confirm the spec's four screens 1:1.

**Rationale**: The Stitch project is the more specific, decided artifact (an actual design-system
export with exact hex/type values); `design-brief.md`'s "observatory logbook" direction described
the same intent in generic terms before this was available. Using the Stitch tokens as the
single source avoids two slightly different copies of the same palette drifting apart.

**Alternatives considered**: Keeping `design-brief.md`'s rounder, less specific token set as
canonical — rejected now that the more precise Stitch design system exists; the two are consistent
in intent, so this is a refinement, not a contradiction.

## Security review skills

**Decision**: Treat `auth-assessment`, `secure-storage-audit`, and `network-security-check`
(MASVS-aligned skills already available in this environment) as mandatory review passes once the
corresponding code exists — auth-assessment after the Login screen/Firebase Auth integration is
implemented, secure-storage-audit after Room/DataStore wiring, network-security-check after the
Retrofit/Firestore network layer is in place. These are implementation-time/tasks-phase
activities, not part of this plan's design artifacts.

**Rationale**: The feature's input explicitly calls for "mobile-security-skills"; these skills
audit actual code against MASVS controls, so they belong after implementation exists, not during
planning. Constitution Principle II already encodes the baseline (no hardcoded secrets, no
plaintext tokens) that these skills will verify.

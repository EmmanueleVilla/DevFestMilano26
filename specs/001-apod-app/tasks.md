---

description: "Task list template for feature implementation"
---

# Tasks: APOD App

**Input**: Design documents from `/specs/001-apod-app/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md,
`.specify/memory/constitution.md` (all present)

**Tests**: Constitution Principle III mandates unit tests for every domain use case, repository,
and ViewModel — those test tasks below are not optional. Compose UI/instrumented tests are
included in the Polish phase as good practice (quickstart.md relies on `connectedAndroidTest`) but
are not individually gated per story, since the constitution doesn't mandate them.

**Organization**: Tasks are grouped by user story (from spec.md, in priority order P1→P4) to enable
independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)
- File paths below are relative to `app/src/main/java/com/shadowings/devfestmilano26/` unless
  stated otherwise (test paths are called out explicitly with their `test`/`androidTest` root)

## Path Conventions

Single Android module (`app/`), per `plan.md` Project Structure — no separate backend/frontend
split. Production code under `apod/{data,domain,presentation}` and `core/{di,navigation,ui/theme}`;
unit tests mirror the same package layout under `app/src/test/java/...`; instrumented tests under
`app/src/androidTest/java/...`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependency wiring

- [ ] T001 Add Gradle version catalog entries to `gradle/libs.versions.toml` for Hilt, Room + KSP,
      AndroidX Preferences DataStore, Navigation Compose, `kotlinx-serialization-json`, Retrofit +
      `converter-kotlinx-serialization`, OkHttp logging interceptor, Coil 3 Compose, the Firebase
      BoM (`firebase-auth`, `firebase-firestore`), and Credential Manager (`androidx.credentials`,
      `googleid`)
- [ ] T002 Apply the KSP, Hilt Android, Kotlin serialization, and Google Services Gradle plugins in
      `build.gradle.kts` (root) and `app/build.gradle.kts`, and wire in the dependencies added in
      T001
- [ ] T003 [P] Add `NASA_APOD_API_KEY` to `local.properties` (gitignored) and expose it via a
      generated `BuildConfig` field in `app/build.gradle.kts` — per research.md "Secrets handling"
      and constitution Principle II, this key MUST NOT appear as a literal in committed source
- [ ] T004 [P] Add `google-services.json` to `app/` from the Firebase console project, with
      Email/Password and Google sign-in providers enabled and Firestore created in Native mode
      (quickstart.md Prerequisites)
- [ ] T005 Create the empty package skeleton per `plan.md` Project Structure:
      `core/di`, `core/navigation`, `core/ui/theme`, `apod/data/{remote,local,repository}`,
      `apod/domain/{model,repository,usecase}`, `apod/presentation/{today,detail,favorites,login}`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented —
every screen depends on the theme, every story depends on the domain models/repositories below.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Implement the Hilt Application class (`@HiltAndroidApp`) and register it in
      `AndroidManifest.xml`
- [ ] T007 Convert `MainActivity` to `@AndroidEntryPoint` and have it host an empty `NavHost`
- [ ] T008 [P] Define the Observatory Logbook color tokens — surface `#0B0D10`, surface raised
      `#14171B`, primary copper `#B87333` with pressed state `#D98E54`, on-surface `#F2EFEA`,
      muted on-surface `#9C958B`, hairline `#2A2D31`, synced `#6E8B74`, error/rust `#9E4B3D` (per
      research.md "Design tokens", sourced from the Stitch design system) — in
      `core/ui/theme/Color.kt`
- [ ] T009 [P] Define the Observatory Logbook typography tokens — EB Garamond for display/titles,
      Work Sans for body text, JetBrains Mono for labels/metadata (research.md "Design tokens") —
      in `core/ui/theme/Type.kt`, declaring the three font families as theme font resources
- [ ] T010 Wire T008's colors and T009's typography into `MaterialTheme.colorScheme`/
      `MaterialTheme.typography` in `core/ui/theme/Theme.kt` — per constitution Principle IV, every
      screen built in later phases MUST consume only these theme roles, never raw hex colors or
      ad-hoc dimensions (depends on: T008, T009)
- [ ] T011 [P] Define domain model `AstronomyPicture` (identity is `date`; "`hdUrl` is only ever
      non-null when `mediaType == IMAGE`" per data-model.md) in
      `apod/domain/model/AstronomyPicture.kt`
- [ ] T012 [P] Define domain model `Favorite` and `SyncStatus` enum (`LOCAL_ONLY`, `SYNCED`),
      enforcing "a row with `syncStatus == SYNCED` always has a non-null `ownerUid`; a row with
      `syncStatus == LOCAL_ONLY` always has a null `ownerUid`" (data-model.md) in
      `apod/domain/model/Favorite.kt`
- [ ] T013 [P] Define domain model `UserSession`, enforcing "`uid`/`email` are always null together
      with `isSignedIn == false`, and both non-null when `isSignedIn == true`" (data-model.md) in
      `apod/domain/model/UserSession.kt`
- [ ] T014 Define repository interfaces `TodayPictureRepository`, `FavoritesRepository`, and
      `SessionRepository` exactly per `contracts/favorites-repository.md` in
      `apod/domain/repository/` — no Room/Firestore/Retrofit/Firebase imports anywhere in this
      package, per constitution Principle I (depends on: T011, T012, T013)
- [ ] T015 [P] Implement the Room entity `FavoriteEntity` + `FavoriteDao`, with an
      insert-or-replace-by-`date` write and a delete-by-`date` write — "Primary key `date` means
      saving an already-favorited date again is an update, not a duplicate insert" (data-model.md)
      — in `apod/data/local/FavoriteEntity.kt` and `apod/data/local/FavoriteDao.kt`
- [ ] T016 [P] Implement the Room entity `CachedTodayEntity` + `CachedTodayDao` as a single-row
      upsert — "Always overwritten — only the most recent successful fetch is kept"
      (data-model.md) — in `apod/data/local/CachedTodayEntity.kt` and
      `apod/data/local/CachedTodayDao.kt`
- [ ] T017 Implement `AppDatabase` (Room), registering both entities and both DAOs, in
      `apod/data/local/AppDatabase.kt` (depends on: T015, T016)
- [ ] T018 [P] Implement a Preferences DataStore-backed session source (`isSignedIn`, `uid`,
      `email` — never tokens, per research.md "Secrets handling") in
      `apod/data/local/SessionPreferences.kt`
- [ ] T019 [P] Implement `ApodApiService` (Retrofit) + `ApodDto` + a DTO→domain mapper exactly per
      `contracts/apod-api.md` (including the `media_type` → `IMAGE`/`VIDEO` mapping) in
      `apod/data/remote/ApodApiService.kt` and `apod/data/remote/ApodDto.kt`
- [ ] T020 [P] Implement `FirestoreFavoritesSource` — reads/writes/deletes against
      `users/{uid}/favorites/{date}` exactly per `contracts/firestore-schema.md` — in
      `apod/data/remote/FirestoreFavoritesSource.kt`
- [ ] T021 Implement `TodayPictureRepositoryImpl` (fetches via T019; on a successful fetch,
      overwrites the T016 cache) in `apod/data/repository/TodayPictureRepositoryImpl.kt`
      (depends on: T017, T019)
- [ ] T022 Implement `FavoritesRepositoryImpl` — Room (T015) is always the source of truth the UI
      reads from; `saveFavorite` is insert-or-replace plus a conditional Firestore (T020) mirror
      write when `syncStatus == SYNCED`; `removeFavorite` deletes locally plus a conditional
      Firestore delete; `migrateLocalFavoritesToAccount` re-labels every `LOCAL_ONLY` row to
      `SYNCED` with the given `uid` and writes it to Firestore (FR-015) — in
      `apod/data/repository/FavoritesRepositoryImpl.kt` (depends on: T015, T020)
- [ ] T023 Implement `SessionRepositoryImpl` — Firebase Auth email/password sign-in, Google
      sign-in via Credential Manager (`GetGoogleIdOption` → `GoogleAuthProvider.getCredential` →
      `FirebaseAuth.signInWithCredential`; re-verify the exact current Credential Manager API via
      Context7 before writing this, per research.md's open flag), `continueWithoutAccount` (no
      Firebase call), and `signOut`, all backed by T018's DataStore cache — in
      `apod/data/repository/SessionRepositoryImpl.kt` (depends on: T018)
- [ ] T024 [P] Hilt DI modules — `NetworkModule` (OkHttp + Retrofit), `DatabaseModule` (Room),
      `DataStoreModule`, `FirebaseModule` (`FirebaseAuth`/`FirebaseFirestore` instances), and
      `RepositoryModule` (binding T014's interfaces to T021/T022/T023's implementations) — in
      `core/di/` (depends on: T014, T021, T022, T023)
- [ ] T025 Define type-safe navigation routes (`Today`, `Detail(date: String)`, `Favorites`,
      `Login`, all `@Serializable` per research.md "Navigation") and the `NavHost` skeleton in
      `core/navigation/`, choosing the start destination by whether the user has already passed
      the Login screen once (a DataStore flag) (depends on: T007, T014)
- [ ] T026 [P] Unit test `FavoritesRepositoryImpl` against an in-memory Room database and fakes for
      Firestore/session, covering the save/remove toggle and the FR-015 migration rule, in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/data/repository/FavoritesRepositoryImplTest.kt`
      (depends on: T022)
- [ ] T027 [P] Unit test `TodayPictureRepositoryImpl` and `SessionRepositoryImpl` against fakes in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/data/repository/` (depends on: T021, T023)

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - View Today's Picture (Priority: P1) 🎯 MVP

**Goal**: Opening the app shows today's picture — full-width image or video badge, title, date,
expandable explanation preview, and a favorite toggle — with correct loading and error states.

**Independent Test**: Launch the app (online and offline, with and without a prior cache) and
verify the Today screen's loading, content, and error states all render correctly, and that
tapping the favorite mark persists across an app restart — no other screen is required.

### Implementation for User Story 1

- [ ] T028 [P] [US1] Implement `GetTodayPictureUseCase` — fetch via `TodayPictureRepository`; on
      failure, fall back to the cached entry; surface the Error state only when neither a fresh
      fetch nor a cache is available — in `apod/domain/usecase/GetTodayPictureUseCase.kt`
- [ ] T029 [P] [US1] Implement `ObserveFavoriteStatusUseCase` (wraps
      `FavoritesRepository.observeFavoriteStatus(date)`) in
      `apod/domain/usecase/ObserveFavoriteStatusUseCase.kt`
- [ ] T030 [P] [US1] Implement `ToggleFavoriteUseCase` (save when not yet favorited, remove when
      already favorited — the toggle behavior from `contracts/favorites-repository.md`) in
      `apod/domain/usecase/ToggleFavoriteUseCase.kt`
- [ ] T031 [US1] Unit test `GetTodayPictureUseCase`, `ObserveFavoriteStatusUseCase`, and
      `ToggleFavoriteUseCase` against fake repositories in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/domain/usecase/` (depends on: T028, T029, T030)
- [ ] T032 [US1] Define `TodayState` (`Loading`/`Content`/`Error`, with `Content` holding the
      picture, its favorite status, and an expanded/collapsed explanation flag) and `TodayIntent`
      (`Expand`, `ToggleFavorite`, `Retry`, `OpenDetail`) in
      `apod/presentation/today/TodayContract.kt`
- [ ] T033 [US1] Implement `TodayViewModel` (`@HiltViewModel`), combining T028 and T029 into
      `TodayState` and handling every `TodayIntent` from T032, in
      `apod/presentation/today/TodayViewModel.kt` (depends on: T028, T029, T030, T032)
- [ ] T034 [US1] Unit test `TodayViewModel`'s state transitions (loading → content, expand toggle,
      favorite toggle, error → retry) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/presentation/today/TodayViewModelTest.kt`
      (depends on: T033)
- [ ] T035 [US1] Implement `TodayScreen` (full-width `AsyncImage` with the video badge overlay,
      title/date/explanation preview + expand, the favorite mark, and the design-brief's ink-line
      loading motif and rust error frame), wired as the `NavHost`'s post-login start destination,
      in `apod/presentation/today/TodayScreen.kt` (depends on: T025, T033)

**Checkpoint**: User Story 1 is fully functional and independently testable (MVP)

---

## Phase 4: User Story 2 - View Full Detail and Save or Share (Priority: P2)

**Goal**: From Today, open a Detail screen with the full explanation, conditional credit, a
permalink, a save/remove toggle, and a share action.

**Independent Test**: Open a picture's Detail screen and verify the full explanation, the
credit/copyright line (present vs. omitted), the permalink, the save/remove toggle, and the share
action all work, independent of the Favorites or Login screens.

### Implementation for User Story 2

- [ ] T036 [US2] Implement `ObserveCachedTodayPictureUseCase`, resolving the full
      `AstronomyPicture` for a given `date` from T021's cache, and extend the Detail navigation
      route to carry that `date`, in `apod/domain/usecase/ObserveCachedTodayPictureUseCase.kt`
      (depends on: T021, T025)
- [ ] T037 [US2] Unit test `ObserveCachedTodayPictureUseCase` in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/domain/usecase/ObserveCachedTodayPictureUseCaseTest.kt`
      (depends on: T036)
- [ ] T038 [US2] Define `DetailState`/`DetailIntent`/`DetailEffect` (`Loading`/`Content`/`Error`;
      `ToggleSave`, `Share`, `OpenPermalink`; `OpenShareSheet`, `OpenBrowser`) in
      `apod/presentation/detail/DetailContract.kt`
- [ ] T039 [US2] Implement `DetailViewModel`, reusing US1's `ObserveFavoriteStatusUseCase` (T029)
      and `ToggleFavoriteUseCase` (T030) plus T036, showing the credit line only when `copyright`
      is present (data-model.md), in `apod/presentation/detail/DetailViewModel.kt`
      (depends on: T029, T030, T036, T038)
- [ ] T040 [US2] Unit test `DetailViewModel` (credit shown only when present, save/remove toggle,
      share effect emission) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/presentation/detail/DetailViewModelTest.kt`
      (depends on: T039)
- [ ] T041 [US2] Implement `DetailScreen` (full explanation, conditional credit, a permalink that
      opens the browser, a save control sharing Today's visual contract, share via the Android
      share intent), and wire Today's image-tap/"read more" navigation into it, in
      `apod/presentation/detail/DetailScreen.kt` (depends on: T035, T039)

**Checkpoint**: User Stories 1 AND 2 both work independently

---

## Phase 5: User Story 3 - Browse Saved Favorites (Priority: P3)

**Goal**: A Favorites screen listing saved pictures as a grid (thumbnail/title/date), with an
empty state and a per-item local-only/synced indicator.

**Independent Test**: Save one or more pictures, open the Favorites screen, and verify the grid,
the zero-favorites empty state, and the local-only/synced label per item.

### Implementation for User Story 3

- [ ] T042 [P] [US3] Implement `ObserveFavoritesUseCase` (wraps
      `FavoritesRepository.observeFavorites()`) in `apod/domain/usecase/ObserveFavoritesUseCase.kt`
- [ ] T043 [US3] Unit test `ObserveFavoritesUseCase` (empty list, populated list, local-only vs.
      synced entries) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/domain/usecase/ObserveFavoritesUseCaseTest.kt`
      (depends on: T042)
- [ ] T044 [US3] Define `FavoritesState` (`Loading`/`Empty`/`Content`/`Error`) and `FavoritesIntent`
      (`OpenFavorite(date)`, `Retry`) in `apod/presentation/favorites/FavoritesContract.kt`
- [ ] T045 [US3] Implement `FavoritesViewModel`, mapping T042's list to
      `Empty`/`Content`/`Error`, in `apod/presentation/favorites/FavoritesViewModel.kt`
      (depends on: T042, T044)
- [ ] T046 [US3] Unit test `FavoritesViewModel` (empty-state mapping, content mapping, local/synced
      label mapping) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/presentation/favorites/FavoritesViewModelTest.kt`
      (depends on: T045)
- [ ] T047 [US3] Implement `FavoritesScreen` (the staggered asymmetric grid from design-brief.md,
      thumbnail/title/date, the LOCAL/SYNCED tag, the empty-state frame), wired into the `NavHost`
      with an entry point reachable from Today/Detail, in
      `apod/presentation/favorites/FavoritesScreen.kt` (depends on: T025, T041, T045)

**Checkpoint**: User Stories 1, 2, AND 3 all work independently

---

## Phase 6: User Story 4 - Use the App With or Without an Account (Priority: P4)

**Goal**: A Login screen offering email/password, Google, and "continue without an account";
logging in migrates existing local-only favorites to synced and keeps them synced across devices.

**Independent Test**: Complete the login flow (email/password or Google) and confirm favorites
saved afterward are "synced" and pre-existing ones migrate; separately, choose "continue without
an account" and confirm favorites saved afterward stay "local only".

### Implementation for User Story 4

- [ ] T048 [P] [US4] Implement `SyncFavoritesOnLoginUseCase` (wraps
      `FavoritesRepository.migrateLocalFavoritesToAccount`, invoked right after a successful
      sign-in per FR-015) in `apod/domain/usecase/SyncFavoritesOnLoginUseCase.kt`
- [ ] T049 [US4] Unit test `SyncFavoritesOnLoginUseCase` (every `LOCAL_ONLY` row becomes `SYNCED`
      with the signed-in `uid`) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/domain/usecase/SyncFavoritesOnLoginUseCaseTest.kt`
      (depends on: T048)
- [ ] T050 [US4] Define `LoginState` (`Default`/`SigningIn`/`Error`) and `LoginIntent`
      (`SubmitEmailPassword`, `SignInWithGoogle`, `ContinueWithoutAccount`) in
      `apod/presentation/login/LoginContract.kt`
- [ ] T051 [US4] Implement `LoginViewModel`, wrapping T023's sign-in methods and invoking T048 on
      success, in `apod/presentation/login/LoginViewModel.kt` (depends on: T023, T048, T050)
- [ ] T052 [US4] Unit test `LoginViewModel` (invalid credentials → Error, successful sign-in →
      migration invoked, continue-without-account → no Firebase call) in
      `app/src/test/java/com/shadowings/devfestmilano26/apod/presentation/login/LoginViewModelTest.kt`
      (depends on: T051)
- [ ] T053 [US4] Implement `LoginScreen` (email/password fields, a Google sign-in button via
      Credential Manager, "continue without an account", per design-brief.md "Login"), wired as
      the `NavHost`'s first-run start destination, in `apod/presentation/login/LoginScreen.kt`
      (depends on: T025, T051)
- [ ] T054 [US4] Register/unregister a Firestore snapshot listener on `users/{uid}/favorites`
      while signed in, applying remote adds/updates/deletes into Room (research.md "Sync"
      remote→local direction) in `apod/data/repository/FavoritesRepositoryImpl.kt`
      (depends on: T022, T023)
- [ ] T055 [US4] Add a "sync your favorites" entry point on `FavoritesScreen`, shown only when
      signed out with at least one `LOCAL_ONLY` favorite, navigating to Login — so FR-015's
      migration is reachable after first run, not only at first launch — in
      `apod/presentation/favorites/FavoritesScreen.kt` (depends on: T047, T053)

**Checkpoint**: All four user stories are independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validation and hardening that spans every user story

- [ ] T056 [P] Room DAO instrumented tests (`FavoriteDao`, `CachedTodayDao`) in
      `app/src/androidTest/java/com/shadowings/devfestmilano26/apod/data/local/`
- [ ] T057 [P] Compose UI instrumented smoke tests for Today, Detail, Favorites, and Login in
      `app/src/androidTest/java/com/shadowings/devfestmilano26/apod/presentation/*/`
- [ ] T058 Run the `auth-assessment`, `secure-storage-audit`, and `network-security-check` skills
      against the built app (research.md "Security review skills"; constitution Principle II) and
      remediate any findings
- [ ] T059 Execute every scenario in `quickstart.md` end-to-end on a device/emulator and record the
      results
- [ ] T060 Run `./gradlew build` (full build + unit tests) as the final constitution Principle III
      completion gate

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories (the theme,
  domain models, repositories, DI graph, and navigation skeleton every story needs)
- **User Stories (Phase 3–6)**: All depend on Foundational completion
  - US1 has no dependency on US2/US3/US4
  - US2 depends on US1's `ObserveFavoriteStatusUseCase`/`ToggleFavoriteUseCase` (reused, not
    reimplemented) and on T035 (TodayScreen) existing to navigate from
  - US3 depends on T041 (DetailScreen) existing as its navigation target, but its own
    ViewModel/use case work is independent of US1/US2 internals
  - US4 depends on T047 (FavoritesScreen) existing for its "sync your favorites" entry point
    (T055), but Login itself is otherwise independent
- **Polish (Phase 7)**: Depends on all four user stories being complete

### Within Each User Story

- Use cases before ViewModels before screens (Compose UI)
- Unit tests for a use case/ViewModel are written alongside it, immediately after its
  implementation task, per constitution Principle III

### Parallel Opportunities

- T003, T004 can run in parallel (Setup)
- T008, T009 (color/typography token files) can run in parallel; T010 (Theme.kt) depends on both
- T011, T012, T013 (domain models) can run in parallel; T014 (repository interfaces) depends on
  all three
- T015, T016 (Room entities), T018 (DataStore), T019 (Retrofit), T020 (Firestore source) can all
  run in parallel; T017/T021/T022/T023 depend on specific subsets of them
- T026, T027 (foundational repository unit tests) can run in parallel once their respective
  implementations exist
- T028, T029, T030 (US1 use cases) can run in parallel; T042 (US3) and T048 (US4) are each
  independently parallelizable once Foundational is done, since they don't share files with US1/US2
  work

---

## Parallel Example: User Story 1

```bash
# Launch all three US1 use cases together:
Task: "Implement GetTodayPictureUseCase in apod/domain/usecase/GetTodayPictureUseCase.kt"
Task: "Implement ObserveFavoriteStatusUseCase in apod/domain/usecase/ObserveFavoriteStatusUseCase.kt"
Task: "Implement ToggleFavoriteUseCase in apod/domain/usecase/ToggleFavoriteUseCase.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: run quickstart.md Scenario 1 and Scenario 5 (Today's loading/error/
   offline-cache behavior) independently
5. Demo if ready — this is already a usable, if narrow, app

### Incremental Delivery

1. Setup + Foundational → foundation ready
2. Add User Story 1 → validate via quickstart.md Scenario 1 → MVP
3. Add User Story 2 → validate via quickstart.md Scenario 2
4. Add User Story 3 → validate via quickstart.md Scenario 3
5. Add User Story 4 → validate via quickstart.md Scenario 4 (and re-run Scenario 5 for the
   offline/error paths, since login introduces new failure modes)
6. Polish phase → full quickstart.md pass + security skills + `./gradlew build`

### Solo Developer Strategy

Given this feature's single-developer scope (one Gradle module, one cohesive `apod` feature),
sequential delivery in priority order (P1 → P2 → P3 → P4 → Polish) is recommended over parallel
story work — later stories reuse earlier stories' use cases (US2 reuses US1's;
US4's migration depends on US1–US3's favorites behavior being correct first).

---

## Notes

- [P] tasks touch different files and have no incomplete-task dependencies
- [Story] labels map tasks to spec.md's user stories for traceability
- Every domain use case, repository implementation, and ViewModel has a paired unit test task,
  per constitution Principle III — do not skip these even though the task format marks them as
  separate, later-numbered tasks
- Commit after each task or logical group
- Stop at any checkpoint to validate a story independently before continuing

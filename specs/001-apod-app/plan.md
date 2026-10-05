# Implementation Plan: APOD App

**Branch**: `001-apod-app` | **Date**: 2026-10-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-apod-app/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

A single-module Android app (Kotlin + Jetpack Compose) presenting NASA's Astronomy Picture of the
Day across four screens — Today, Detail, Favorites, Login — built as one feature using MVI and
clean architecture layering (`data`/`domain`/`presentation`). Favorites are always read from a
local Room database (so the app works fully offline and while logged out); when the user is
logged in via Firebase Auth (email/password or Google), a sync layer mirrors Room favorites to a
per-user Cloud Firestore collection in both directions, and local-only favorites are migrated to
"synced" on login. Visual design follows the "Observatory Logbook" design system authored in the
project's Stitch workspace (`projects/207351743317047103`), wired into the app's Material 3 theme
rather than hardcoded per screen.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (existing project toolchain, `gradle/libs.versions.toml`)

**Primary Dependencies**: Jetpack Compose (Material 3, BOM `2026.02.01`), Hilt (DI), Room (local
persistence), AndroidX Preferences DataStore (session/UI prefs), Navigation Compose with
type-safe (`kotlinx.serialization`) routes, Retrofit + `kotlinx-serialization-json` converter +
OkHttp (NASA APOD REST client), Coil 3 Compose (`AsyncImage`) for remote images, Firebase
Authentication (email/password + Google via Credential Manager), Cloud Firestore (favorites sync)

**Storage**: Room (SQLite) as the single local source of truth for favorites and the last
successfully loaded "today" entry (offline fallback); Preferences DataStore for small session/UI
state (never credentials/tokens — Firebase Auth SDK owns its own token storage); Cloud Firestore
as the remote sync target for favorites, scoped per authenticated user

**Testing**: JUnit4 + `kotlinx-coroutines-test` for domain use cases, repositories (against an
in-memory Room database and fakes for Firestore/Retrofit), and ViewModels; `androidx.compose.ui.test`
(instrumented) for screen-level UI tests — all already present in `gradle/libs.versions.toml`

**Target Platform**: Android phones, minSdk 26 / targetSdk 37 / compileSdk 37 (matches
`app/build.gradle.kts`)

**Project Type**: mobile-app — single Gradle module (`app`), internally layered per feature

**Performance Goals**: Today screen content visible within 2s under normal network conditions
(spec SC-001); Favorites grid scrolls smoothly (no dropped-frame jank) at typical favorites-list
sizes

**Constraints**: Must work with zero network connectivity for previously loaded content (today's
cached entry, all favorites) per the spec's loading/error states and local-only favorites
guarantee; must satisfy constitution Principle II (OWASP MASVS v2) — no hardcoded secrets, no
plaintext token storage

**Scale/Scope**: 4 screens, 1 feature module, consumer-scale usage (NASA public API + Firebase
free-tier-appropriate sync; no custom backend)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Checked against `.specify/memory/constitution.md` v1.0.0:

| Principle | Check | Result |
|---|---|---|
| I. MVI + Clean Architecture | Project Structure below defines `data`/`domain`/`presentation` per feature, with `domain` free of `data`/`presentation` imports, and one MVI state holder (ViewModel) per screen | PASS |
| II. Security by Default (OWASP MASVS v2) | No hardcoded secrets: NASA API key loaded from local, gitignored config (`local.properties` → `BuildConfig`), never committed; Firebase Auth tokens stay inside the Firebase SDK's own secure storage — the app never persists raw tokens in Room/DataStore | PASS (see research.md "Secrets handling") |
| III. Test-Driven Quality Gate | Project Structure reserves unit test coverage for every domain use case, repository, and ViewModel; `./gradlew build` remains the completion gate | PASS |
| IV. Material 3 Design System Only | The Stitch "Observatory Logbook" design tokens (colors, type scale) are translated once into `MaterialTheme.colorScheme`/`MaterialTheme.typography` in the theme package; screens consume theme roles only, never raw hex/dimensions | PASS (flagged as an explicit task in Phase 1 / tasks phase so it isn't skipped) |
| V. Verified Library Usage (Context7) | Hilt, Room, DataStore, Firebase Auth/Firestore, Coil, Retrofit, and Navigation Compose APIs were each verified against current docs via Context7 before being chosen — see research.md | PASS |

No violations — Complexity Tracking table is not needed.

**Post-Phase-1 re-check**: the data model and contracts below introduce a sync reconciliation
concept (Room ↔ Firestore) but no new project, framework, or pattern beyond what's already
justified above; still PASS, no re-evaluation changes.

## Project Structure

### Documentation (this feature)

```text
specs/001-apod-app/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   ├── apod-api.md
│   ├── favorites-repository.md
│   └── firestore-schema.md
├── design-brief.md      # Already produced (/design-brief command)
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
# Option 1: Single project (Android app, mobile-app type) — matches this feature
app/src/main/java/com/shadowings/devfestmilano26/
├── DevFestMilano26App.kt          # @HiltAndroidApp application class
├── MainActivity.kt                 # @AndroidEntryPoint, hosts the NavHost
├── core/
│   ├── di/                         # Hilt modules: network, database, datastore, firebase
│   ├── navigation/                 # Type-safe route definitions + NavHost wiring
│   └── ui/theme/                   # Color.kt, Theme.kt, Type.kt — Observatory Logbook tokens
└── apod/                           # The single feature covering all 4 screens
    ├── data/
    │   ├── remote/                  # ApodApiService (Retrofit), DTOs, FirestoreFavoritesSource
    │   ├── local/                   # Room: FavoriteEntity, FavoriteDao, AppDatabase; DataStore session source
    │   └── repository/              # Repository implementations (data → domain mapping)
    ├── domain/
    │   ├── model/                   # AstronomyPicture, Favorite, SyncStatus, UserSession
    │   ├── repository/              # Repository interfaces (ports), no Android/Firebase imports
    │   └── usecase/                 # GetTodayPicture, ToggleFavorite, ObserveFavorites,
    │                                 # SignIn*, ContinueWithoutAccount, SyncFavoritesOnLogin
    └── presentation/
        ├── today/                   # TodayScreen, TodayViewModel, TodayState/Intent
        ├── detail/                  # DetailScreen, DetailViewModel, DetailState/Intent
        ├── favorites/                # FavoritesScreen, FavoritesViewModel, FavoritesState/Intent
        └── login/                   # LoginScreen, LoginViewModel, LoginState/Intent

app/src/test/java/com/shadowings/devfestmilano26/apod/
├── domain/usecase/                 # Use case unit tests
├── data/repository/                 # Repository tests (in-memory Room + fakes)
└── presentation/*/                  # ViewModel unit tests (one dir per screen)

app/src/androidTest/java/com/shadowings/devfestmilano26/apod/
├── data/local/                      # Room DAO instrumented tests
└── presentation/*/                  # Compose UI tests per screen
```

**Structure Decision**: Single Android Gradle module (`app`), no separate backend/frontend split —
the app talks directly to the NASA APOD REST API and Firebase, both external managed services.
All four screens are treated as one feature (`apod/`) sharing one `domain`/`data` layer, since they
operate on the same entities (`AstronomyPicture`, `Favorite`) rather than being independent
business domains. `core/` holds cross-cutting setup (DI wiring, navigation graph, theme) that every
screen depends on but that doesn't itself belong to the `data`/`domain`/`presentation` split.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally left empty.

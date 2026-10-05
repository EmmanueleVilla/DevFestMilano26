# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`DevFestMilano26` — a single-module Android app (`app/`), Kotlin + Jetpack Compose, applicationId
`com.shadowings.devfestmilano26`, minSdk 26 / targetSdk 37 / compileSdk 37. Dependency versions
are managed through the Gradle version catalog at `gradle/libs.versions.toml`.

The codebase is currently at its initial scaffold (stock `MainActivity` + Compose theme files,
no feature code yet), so there is no existing architecture to preserve — new code must establish
the structure described in "Architecture rules" below from the first feature.

## Commands

Run all Gradle commands from the repository root via the wrapper (`./gradlew`), not a bare `gradle`.

- Build debug APK: `./gradlew assembleDebug`
- Full build (compile + lint + unit tests): `./gradlew build`
- Run unit tests (`app/src/test`): `./gradlew test`
- Run a single unit test class: `./gradlew test --tests "com.shadowings.devfestmilano26.ExampleUnitTest"`
- Run a single unit test method: `./gradlew test --tests "com.shadowings.devfestmilano26.ExampleUnitTest.addition_isCorrect"`
- Run instrumented tests (`app/src/androidTest`, requires a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`

## Architecture rules (from `.specify/memory/constitution.md`)

This project has a ratified constitution at `.specify/memory/constitution.md` (v1.0.0). Its rules
are binding for all code in this repo:

- **MVI + Clean Architecture**: every feature is structured into `data` / `domain` / `presentation`
  packages. `domain` must not depend on `data` or `presentation`. Business logic lives in domain
  use cases, not in ViewModels or Composables. State/intents flow unidirectionally through a single
  state holder per feature.
- **Security (OWASP MASVS v2)**: no hardcoded secrets/API keys/credentials in source, build files,
  or version control. No sensitive data (tokens, etc.) persisted in plaintext — use
  Android's secure storage (EncryptedSharedPreferences, Keystore-backed storage).
- **Test-driven quality gate**: every feature ships with unit tests for its domain layer (use
  cases), repositories, and ViewModels. `./gradlew build` (build + tests) must pass before a
  feature is considered done.
- **Material 3 only**: all UI colors come from `MaterialTheme.colorScheme`, all typography from
  `MaterialTheme.typography` (defined in `app/src/main/java/.../ui/theme/`). No raw hex colors or
  ad-hoc dimensions in composables.
- **Context7 before library use**: verify any external library's current API via the Context7 MCP
  before using it or upgrading its version — don't rely on memory/training data for API shape.

## Spec Kit workflow

This repo uses [GitHub Spec Kit](https://github.com/github/spec-kit) for spec-driven development
(`.specify/`, skills under `.claude/skills/speckit-*`). Typical flow for new feature work:

1. `/speckit-specify` — create/update the feature specification
2. `/speckit-clarify` (optional) — resolve ambiguous areas before planning
3. `/speckit-plan` — produce the implementation plan
4. `/speckit-tasks` — generate dependency-ordered tasks
5. `/speckit-analyze` (optional) — cross-check spec/plan/tasks consistency
6. `/speckit-implement` — execute the tasks
7. `/speckit-converge` — reconcile the codebase against spec/plan/tasks and append any remaining work

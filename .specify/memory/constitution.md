<!--
Sync Impact Report
Version change: none → 1.0.0 (initial ratification)
Modified principles: N/A (new constitution, no prior ratified version)
Added sections:
- Core Principles: I. MVI + Clean Architecture, II. Security by Default (OWASP MASVS v2),
  III. Test-Driven Quality Gate, IV. Material 3 Design System Only, V. Verified Library Usage
  (Context7)
- Technology Stack
- Development Workflow
- Governance
Removed sections: none
Follow-up TODOs: none
-->

# DevFestMilano26 Constitution

## Core Principles

### I. MVI + Clean Architecture
Every feature MUST be structured using the Model-View-Intent (MVI) pattern combined with clean
architecture layering: `data`, `domain`, and `presentation`, per the project's compose-skill
guidance. The `domain` layer MUST NOT depend on `data` or `presentation`; business logic MUST
live in domain use cases, not in ViewModels or Composables. State and intents MUST flow
unidirectionally through a single state holder per feature.
Rationale: this separation keeps business logic testable in isolation and keeps UI layers
replaceable without touching business rules.

### II. Security by Default (OWASP MASVS v2)
All code MUST comply with OWASP MASVS v2 storage and authentication controls. Secrets, API keys,
and credentials MUST NOT be hardcoded in source, build files, or version control. Tokens and
other sensitive data MUST NOT be persisted in plaintext; they MUST use Android's secure storage
mechanisms (e.g., EncryptedSharedPreferences, Keystore-backed storage).
Rationale: mobile apps ship as unprotected binaries, so secrets and sensitive data must never be
recoverable by inspecting the installed artifact.

### III. Test-Driven Quality Gate
Every feature MUST ship with unit tests covering its domain layer (use cases), repositories, and
ViewModels. A feature is NOT considered complete until the full build and test suite pass. Both
the build and the test suite (e.g., `./gradlew build`) MUST pass before a feature is merged.
Rationale: untested business logic and state machines are the most common source of regressions
in MVI-based apps.

### IV. Material 3 Design System Only
All UI MUST be built exclusively through the Material 3 theme: color MUST come from
`MaterialTheme.colorScheme` roles and typography MUST come from `MaterialTheme.typography`.
Composables MUST NOT declare raw hex color values or ad-hoc dimension/sizing values outside the
theme's type scale and spacing tokens.
Rationale: a single themed source of truth keeps the UI consistent and makes future re-theming
or dark-mode support mechanical rather than a rewrite.

### V. Verified Library Usage (Context7)
Before using any external library API — new usage or a version upgrade — its current
documentation MUST be verified via the Context7 MCP rather than relied upon from training data or
memory.
Rationale: library APIs change between versions, and incorrect assumptions about signatures or
behavior introduce defects that are costly to trace back to an API misuse.

## Technology Stack

This project is an Android application written in Kotlin, targeting minSdk 26 / compileSdk 37,
built with Jetpack Compose. The module layout MUST reflect the clean architecture layering
required by Principle I (`data`, `domain`, `presentation` packages per feature). Dependency
versions are centrally managed through the Gradle version catalog (`libs.versions.toml`).

## Development Workflow

Pull requests MUST verify compliance with all Core Principles before merge: MVI/clean
architecture boundaries, absence of hardcoded secrets or plaintext token storage, a passing
build and unit test suite, Material 3-only styling, and Context7-verified library usage where
new APIs are introduced. Any deviation MUST be explicitly justified in the pull request
description and tracked as a follow-up task for remediation.

## Governance

This constitution supersedes any conflicting team convention or prior undocumented practice.
Amendments require updating this document with a version bump following semantic versioning:
MAJOR for incompatible principle removals or redefinitions, MINOR for new or materially expanded
principles or sections, PATCH for clarifications and wording fixes. Every amendment MUST update
`Last Amended` and record its rationale. All pull requests and code reviews MUST verify
compliance with the Core Principles above; unresolved non-compliance blocks merge.

**Version**: 1.0.0 | **Ratified**: 2026-10-05 | **Last Amended**: 2026-10-05

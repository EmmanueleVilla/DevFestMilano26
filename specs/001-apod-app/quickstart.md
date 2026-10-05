# Quickstart: Validating the APOD App Feature

This is a validation guide, not an implementation guide — it assumes the feature described in
`plan.md`/`tasks.md` has been built, and documents how to prove it works end-to-end. No
implementation code is included here (see `data-model.md` and `contracts/` for shapes,
`tasks.md` for the build-out).

## Prerequisites

- A NASA APOD API key in `local.properties` (`NASA_APOD_API_KEY=...`), so `BuildConfig` exposes it
  to `ApodApiService` (research.md "Secrets handling"). `DEMO_KEY` works for manual testing but is
  rate-limited.
- A Firebase project connected via `google-services.json` in `app/`, with Email/Password and
  Google sign-in enabled in the Firebase Auth console, and Firestore created in Native mode.
- A connected Android device or emulator (API 26+) for instrumented/UI tests and manual runs.

## Build and automated checks

```bash
./gradlew build                 # full build + unit tests — the constitution's completion gate
./gradlew test                  # domain/repository/ViewModel unit tests only
./gradlew connectedAndroidTest  # Room DAO + Compose UI instrumented tests (requires a device/emulator)
```

Expected outcome: all of the above succeed with no failing tests, per constitution Principle III.

## Scenario 1 — Today screen, online, first launch (User Story 1)

1. Fresh install, device online, no prior favorites.
2. Launch the app.
3. **Expected**: a brief loading state, then today's picture renders — full-width image/video
   badge, title, date, 2–3 line explanation preview that expands in place when tapped.
4. Tap the favorite mark on the image.
5. **Expected**: the mark switches to its saved appearance; reopening the app later still shows it
   saved (SC-003).

## Scenario 2 — Detail, save/share, and the toggle (User Story 2)

1. From Today, open the Detail screen for the currently displayed picture.
2. **Expected**: full explanation, credit line only if the fetched entry has `copyright`,
   permalink, save control (reflecting whatever was set in Scenario 1), share control.
3. Tap share.
4. **Expected**: the device's standard share sheet opens with the title and permalink.
5. Tap the save control twice.
6. **Expected**: it becomes saved, then unsaved — confirming the insert-or-replace/delete toggle
   behavior in `contracts/favorites-repository.md`.

## Scenario 3 — Favorites grid, empty and populated (User Story 3)

1. With zero favorites, open the Favorites screen.
2. **Expected**: the empty-state message, not a blank grid.
3. Save two or three different days' pictures (requires manually using `date` query against the
   NASA API, or waiting across real days, since archive browsing is out of scope — see
   `spec.md` Assumptions).
4. Reopen Favorites.
5. **Expected**: a grid with thumbnail/title/date per entry, each labeled "local only" (not yet
   logged in).

## Scenario 4 — Login, sync, and migration (User Story 4)

1. From a state with existing `LOCAL_ONLY` favorites (Scenario 3), open Login and sign in with
   email/password (or Google).
2. **Expected**: on success, the Favorites screen's existing entries flip from "local only" to
   "synced" (FR-015) — confirm in the Firebase console that
   `users/{uid}/favorites/{date}` documents now exist for each (`contracts/firestore-schema.md`).
3. Save one more favorite while signed in.
4. **Expected**: it's labeled "synced" immediately (not "local only" then migrated).
5. Sign out via whatever entry point the implementation exposes, then sign back in with the same
   account on a second device/emulator.
6. **Expected**: the synced favorites reappear via the Firestore snapshot listener without the
   user re-saving anything.

## Scenario 5 — Error and offline states (Edge Cases)

1. Disable network connectivity, then fresh-launch the app (no cached Today entry yet).
2. **Expected**: Today shows the Error state with a retry control (FR-017), not a blank screen.
3. Re-enable network and retry.
4. **Expected**: Today loads normally and caches the entry (`CachedTodayEntry`, data-model.md).
5. Disable network again and relaunch.
6. **Expected**: Today now shows the previously cached entry instead of an Error, since a cache
   exists this time.
7. With network disabled, open Favorites.
8. **Expected**: previously saved favorites still render (Room is local-first) regardless of
   connectivity or sign-in state.

## Security spot-checks (ties to constitution Principle II)

- `git grep -i "api_key\s*=" -- '*.kt' '*.kts'` and an inspection of `local.properties` /
  `.gitignore` confirm the NASA API key never appears as a literal in committed source.
- Inspect the app's Room/DataStore files (e.g., via `adb shell run-as ... sqlite3`) to confirm no
  Firebase ID token or refresh token is ever present — only `uid`/`email`/favorite data.
- Once implemented, run the `auth-assessment`, `secure-storage-audit`, and
  `network-security-check` skills against the built app (research.md "Security review skills").

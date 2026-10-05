# Feature Specification: APOD App

**Feature Branch**: `001-apod-app`

**Created**: 2026-10-05

**Status**: Draft

**Input**: User description: "The APOD app (NASA Astronomy Picture of the Day), four screens:
1. Today — image of the day full width, title, date, 2–3 lines of the explanation (expandable), a
badge when media_type is "video", favorite button on the image.
2. Detail — full explanation, credit/copyright if present, permalink, "save to favorites" and
"share".
3. Favorites — grid with thumbnail, title, date; empty state; "local only" vs "synced" indicator.
4. Login (optional) — email/password + Google, "continue without an account"; without login,
favorites stay on the device.
Data fields: title, date, explanation, url/hdurl, media_type, copyright.
States for every screen: loading, empty, error."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Today's Picture (Priority: P1)

A user opens the app and immediately sees today's astronomy picture: a full-width image (or a
clearly marked video), its title, its date, and a short preview of the explanation that they can
expand to read more without leaving the screen.

**Why this priority**: This is the core reason the app exists. Every other screen builds on the
content shown here, and the app delivers value even if nothing else is built yet.

**Independent Test**: Can be fully tested by opening the app and verifying the current day's
title, date, image/video badge, and expandable explanation preview all render correctly — no
other screen is required.

**Acceptance Scenarios**:

1. **Given** the app has successfully fetched today's picture, **When** the user opens the app,
   **Then** they see the full-width image, title, date, and a 2–3 line explanation preview.
2. **Given** today's entry has `media_type` of "video", **When** the Today screen renders,
   **Then** a badge indicating video content is shown in place of/over the media.
3. **Given** the explanation preview is showing, **When** the user taps to expand it, **Then**
   the full explanation text is revealed on the same screen.
4. **Given** the Today screen is showing a picture, **When** the user taps the favorite button on
   the image, **Then** the picture is saved to their favorites.

---

### User Story 2 - View Full Detail and Save or Share (Priority: P2)

A user who wants more than the summary opens the Detail screen to read the full explanation, see
who created or owns the image (when provided), follow a permalink to the original source, save
the picture to their favorites, and share it with someone else.

**Why this priority**: Deepens engagement with a single day's content and is the entry point for
two of the app's key actions (save, share), but the app remains useful without it if only the
Today summary existed.

**Independent Test**: Can be fully tested by opening a picture's Detail screen and verifying the
full explanation, credit/copyright (when present), permalink, save action, and share action all
work, independent of the Favorites or Login screens.

**Acceptance Scenarios**:

1. **Given** a picture has copyright/credit information, **When** the user views its Detail
   screen, **Then** that credit is displayed; **Given** it has none, **Then** the credit line is
   omitted.
2. **Given** the user is on the Detail screen, **When** they tap "save to favorites", **Then**
   the picture is added to their favorites and the control reflects the saved state.
3. **Given** a picture is already saved, **When** the user taps the save control again, **Then**
   it is removed from their favorites.
4. **Given** the user is on the Detail screen, **When** they tap "share", **Then** the system's
   standard share mechanism opens with the picture's title and permalink.
5. **Given** the user is on the Detail screen, **When** they tap the permalink, **Then** they are
   taken to the original source page for that picture.

---

### User Story 3 - Browse Saved Favorites (Priority: P3)

A user who has saved pictures over time opens the Favorites screen to browse them as a grid of
thumbnails, each labeled with its title and date, and can tell at a glance which favorites only
live on this device versus which are backed up to their account.

**Why this priority**: Favorites only become useful once more than one exists, so this screen
depends on User Story 1/2 having saved at least one item, but it is still a self-contained,
independently verifiable piece of functionality.

**Independent Test**: Can be fully tested by saving one or more pictures, opening the Favorites
screen, and verifying the grid, the empty state (with zero favorites), and the local-only/synced
indicator per item.

**Acceptance Scenarios**:

1. **Given** the user has saved one or more pictures, **When** they open the Favorites screen,
   **Then** they see a grid with each favorite's thumbnail, title, and date.
2. **Given** the user has not saved any pictures, **When** they open the Favorites screen,
   **Then** an empty-state message is shown instead of a grid.
3. **Given** a favorite was saved while logged out, **When** it appears in the grid, **Then** it
   is labeled "local only"; **Given** a favorite was saved while logged in, **Then** it is
   labeled "synced".

---

### User Story 4 - Use the App With or Without an Account (Priority: P4)

A user is offered the choice to log in with email/password or a Google account, or to continue
without one. If they skip login, the app works the same way except favorites are kept only on
the current device instead of being backed up to an account.

**Why this priority**: Login is explicitly optional and only affects where favorites are stored;
the first three user stories fully function without it, so it is the lowest-priority, most
deferrable piece of the feature.

**Independent Test**: Can be fully tested by completing the login flow (email/password or Google)
and confirming favorites saved afterward are marked "synced", and separately by choosing "continue
without an account" and confirming favorites saved afterward are marked "local only".

**Acceptance Scenarios**:

1. **Given** a new user opens the app for the first time, **When** they reach the Login screen,
   **Then** they can choose email/password, Google, or "continue without an account".
2. **Given** the user chooses "continue without an account", **When** they save a favorite,
   **Then** it is stored only on the device and labeled "local only".
3. **Given** the user successfully logs in (email/password or Google), **When** they save a
   favorite afterward, **Then** it is labeled "synced".
4. **Given** the user had local-only favorites before logging in, **When** login succeeds,
   **Then** those favorites are migrated to the account and relabeled "synced".

---

### Edge Cases

- What happens when the day's picture cannot be fetched (e.g., no connectivity, source
  unavailable)? The Today and Detail screens show an error state with a way to retry.
- What happens when the Favorites screen has zero saved items? An empty state is shown, distinct
  from the loading and error states.
- What happens when a picture's `media_type` is "video"? The Today and Detail screens show the
  video badge/indicator instead of treating it as a still image; the permalink and share actions
  still work the same way.
- What happens when a picture has no copyright/credit field? The credit line is omitted entirely
  rather than shown empty.
- What happens when the user tries to save a picture that is already in their favorites? The
  save control acts as a toggle — tapping it again removes the favorite instead of duplicating it.
- What happens when login fails (wrong credentials, cancelled Google sign-in)? The user remains on
  the Login screen with an error message and can retry or choose "continue without an account".
- What happens when the user logs out after having synced favorites? Previously synced favorites
  are no longer shown as the device's local favorites; the user returns to a logged-out, local-only
  state (see Assumptions for what "logged out" retains).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display, by default, the current day's astronomy picture: full-width
  image (or video, per FR-002), title, date, and a 2–3 line preview of its explanation.
- **FR-002**: System MUST show a visible badge on the Today screen (and the Detail screen) when
  the picture's `media_type` is "video" rather than an image.
- **FR-003**: Users MUST be able to expand the explanation preview on the Today screen to read the
  full explanation in place.
- **FR-004**: Users MUST be able to mark the currently displayed picture as a favorite directly
  from the Today screen.
- **FR-005**: Users MUST be able to open a Detail screen for the current picture showing its full
  explanation, its credit/copyright when present, and a permalink to the original source.
- **FR-006**: Users MUST be able to save the picture to favorites and remove it from favorites
  from the Detail screen, with the control reflecting the current saved state.
- **FR-007**: Users MUST be able to share the picture's title and permalink from the Detail screen
  using the device's standard share mechanism.
- **FR-008**: System MUST provide a Favorites screen listing every picture the user has saved, each
  shown with a thumbnail, title, and date, arranged as a grid.
- **FR-009**: System MUST show a distinct empty-state message on the Favorites screen when no
  favorites have been saved.
- **FR-010**: System MUST label each favorite as "local only" or "synced" depending on whether it
  is stored only on the current device or backed up to the user's account.
- **FR-011**: System MUST let users use the app fully (view today's picture, view details, save
  favorites, share) without creating an account or logging in.
- **FR-012**: System MUST offer login via email/password or a Google account, alongside a
  "continue without an account" option, and MUST NOT force login before the app can be used.
- **FR-013**: System MUST store favorites saved while logged out only on the current device and
  label them "local only".
- **FR-014**: System MUST sync favorites saved while logged in to the user's account and label
  them "synced".
- **FR-015**: System MUST migrate a user's existing local-only favorites to their account (and
  relabel them "synced") when that user logs in.
- **FR-016**: System MUST show a loading state on the Today, Detail, and Favorites screens while
  their content is being fetched.
- **FR-017**: System MUST show an error state with a retry option on any screen whose content
  fails to load.
- **FR-018**: System MUST persist, for each astronomy picture the user interacts with, its title,
  date, explanation, media URLs (`url`/`hdurl`), `media_type`, and copyright (when present).

### Key Entities

- **Astronomy Picture**: a single day's NASA APOD entry. Attributes: title, date, explanation,
  `url` (standard-resolution image/video reference), `hdurl` (high-resolution image reference,
  when available), `media_type` (image or video), copyright (optional).
- **Favorite**: a user's saved reference to an Astronomy Picture. Attributes: the referenced
  picture, sync status (local-only or synced), and the account it is synced to (if any).
- **User Account**: an optional identity, created via email/password or Google, that favorites can
  be associated with and synced across devices.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can view today's picture's title, date, and explanation within 2 seconds of
  opening the app under normal network conditions.
- **SC-002**: A user can save any picture to favorites in a single tap, from either the Today or
  Detail screen.
- **SC-003**: 100% of favorites a user saves remain available after closing and reopening the app,
  regardless of whether they are logged in.
- **SC-004**: A user can complete every core action (view today's picture, view details, save a
  favorite, share a picture) without ever being required to log in.
- **SC-005**: When content fails to load, every affected screen shows a clear error state with a
  retry option rather than appearing blank or frozen.
- **SC-006**: A user can tell whether any given favorite is "local only" or "synced" without
  opening it, directly from the Favorites grid.

## Assumptions

- The Today screen always shows the current day's picture; browsing an archive of past days is
  out of scope for this feature.
- Logging in migrates existing local-only favorites to the account, relabeling them "synced"
  (FR-015); there is no option to keep them local-only after login.
- "Share" shares the picture's title and permalink through the device's standard share
  mechanism; a custom in-app share UI is out of scope.
- The permalink shown on the Detail screen points to the original NASA APOD page for that date.
- Each calendar date has exactly one published astronomy picture, consistent with the source
  catalog's behavior.
- "Google" login refers to signing in with an existing Google account; no separate account
  creation flow is required for that path.
- Resolving conflicts between favorites synced from multiple devices under the same account is
  out of scope for this feature.
- Password reset and email verification flows are standard and out of scope to specify in detail
  for this feature.

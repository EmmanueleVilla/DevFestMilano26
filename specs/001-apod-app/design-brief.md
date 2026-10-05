# Design Brief: APOD App — "Observatory Logbook"

**Source spec**: [spec.md](./spec.md) (`001-apod-app`)

**Design direction source**: `.specify/memory/design-direction.md` was not found in this
repository. This brief instead uses the direction supplied inline with this command, which it
treats as the canonical "observatory logbook" direction until that memory file exists. Consider
persisting the "Design Language" section below into `.specify/memory/design-direction.md` so
future briefs don't have to restate it.

No code, no component library choices — this is layout, content, and state intent only.

---

## Design Language

Everything below applies across all four screens; per-screen sections call out only what differs.

**Mood**: a ship's logbook on a cold observatory night — ink on dark paper, instruments, hand-set
type. Precise, slightly severe, never decorative for its own sake.

**Color** (dark scheme):

| Role | Value | Use |
|---|---|---|
| Surface | `#0B0D10` | App background, base layer |
| Surface raised | `#14171B` | Sheets, the Login card, raised rows |
| Primary (copper) | `#B87333` | Primary actions, active states, the favorite mark |
| Primary pressed | `#D98E54` | Pressed/active feedback on copper controls |
| On-surface (primary text) | `#F2EFEA` | Titles, body text — warm paper white, not pure white |
| On-surface muted | `#9C958B` | Secondary text, timestamps, helper copy |
| Hairline | `#2A2D31` | All dividers and borders — never a drop shadow |
| Error (rust) | `#9E4B3D` | Error states and destructive feedback — desaturated, not alarm-red |
| Synced indicator | `#6E8B74` | Muted, ink-green "synced" tag — deliberately quiet, not a badge color |

No gradients anywhere, and specifically no purple-to-blue gradient. Elevation is communicated with
a hairline and a flat tonal step (`Surface` → `Surface raised`), never a soft shadow.

**Typography**:

- **Serif display** for anything that reads as a headline or a date-as-headline: the Today
  screen's title, the Detail screen's title, screen titles generally. This is the one place the
  app is allowed to feel literary.
- **Monospace** for everything that reads as metadata or a stamp: dates, the `media_type` video
  badge, the local-only/synced tag, form labels on the Login screen, the permalink. Monospace is
  the app's "instrument" voice — it marks something as data, not prose.
- Body/explanation text uses a plain sans serif reading face (not the serif, not the monospace) —
  the serif is reserved for display, not paragraphs, or long-form text becomes fatiguing.
- Explicitly not Inter-everywhere: the three roles above must be visually distinct typefaces, not
  three weights of one grotesque.

**Layout**:

- Asymmetric by default. Titles and primary media are left- or right-anchored with deliberate
  negative space on the other side, not centered. The Favorites grid uses an uneven/staggered
  column rhythm (varying thumbnail heights) rather than a uniform tile grid.
- Structure is drawn with hairlines (1px, `Hairline` color), not cards-with-shadows. A "section" is
  a rule plus spacing, not a rounded rectangle.
- No floating icon pills (no circular icon buttons overlaid on corners of media). Where an action
  sits on top of the image (the favorite mark on Today), it sits on a flat scrim strip anchored to
  an edge, not a floating pill.
- No nested icon-on-heading cards (no small icon chip stacked above/inside a bordered heading
  block). Section headings are monospace labels on a hairline rule, full width.

**Real states, as first-class design, not afterthoughts**:

- **Default/pressed**: every tappable control has an explicit pressed state using `Primary
  pressed` (copper brightening) or a hairline inversion for secondary controls — never a generic
  opacity dim.
- **Saved**: the favorite mark has a distinct saved appearance (solid copper fill + filled glyph)
  versus unsaved (copper hairline outline + outline glyph) — a toggle you can read at a glance, not
  just a color swap on an icon.
- **Loading**: a logbook-appropriate loading motif — a thin horizontal copper rule that fills
  left-to-right like ink tracing a line, or a monospace "reading sky…" status label. No generic
  spinner, no shimmering gray card skeleton.
- **Empty**: an empty state is a logbook page with nothing written on it yet — a hairline frame,
  a short monospace status line, and a single plain-language sentence. No illustration mascot.
- **Error**: an error state reuses the same logbook-page frame as empty, but in rust, with a
  monospace status line (e.g. `SIGNAL LOST`) and a single retry action styled as a secondary
  (hairline) control, not a filled copper button — copper is reserved for primary/positive actions.

---

## Today

### Layout
Full-bleed media at the top (image, or video treatment per below), anchored to one side with the
title and date breaking into the negative space beside/below it rather than centered under it —
asymmetric, not a centered hero. Below the media: date (monospace) above title (serif display),
then the explanation preview as plain body text.

### Content
- Full-width image (or video badge treatment — see States)
- Title (serif display)
- Date (monospace, reads like a logbook entry stamp)
- 2–3 line explanation preview, expandable in place (no navigation) to the full explanation
- Favorite mark on the media, anchored to a bottom or side edge on a flat scrim strip (not a
  floating pill)
- Video badge, when `media_type` is "video": a monospace tag (e.g. `▶ VIDEO`) on the same scrim
  strip as the favorite mark, not a separate floating element

### States
- **Default**: media + title + date + collapsed explanation + favorite mark (unsaved or saved
  appearance, per Design Language).
- **Loading**: media area shows the ink-line loading motif in place of the image; title/date/
  explanation areas show short monospace placeholder rules (hairline-thin bars), not gray
  skeleton blocks. Favorite mark is hidden until content resolves (there's nothing to favorite
  yet).
- **Empty**: not applicable — today's entry always exists once fetched; absence of content is
  always the result of a fetch failure, not an empty data set. Treat as Error instead.
- **Error**: the logbook error-page frame replaces the media + content area entirely: rust
  monospace status (e.g. `SIGNAL LOST — TODAY'S ENTRY UNAVAILABLE`), one line of plain-language
  explanation, and a hairline-style retry control.

### Also
- **Pressed**: the favorite mark depresses to `Primary pressed` on tap, with the glyph swapping
  shape (outline ↔ filled) on release to confirm the toggle landed.
- **Saved**: filled copper mark, persists across app reopen (reflects favorites state, not local
  UI state).

---

## Detail

### Layout
Reached from Today (tapping the media or an explicit "read more" affordance). Single column,
asymmetric top: media or a smaller recap of it offset to one side, full explanation as the primary
reading column below, then a hairline-separated metadata strip (credit/copyright, permalink), then
the two primary actions.

### Content
- Full explanation (serif display title retained from Today, now paired with full body copy)
- Credit/copyright line, monospace, shown only when the source provided it — omitted entirely
  (not shown empty) when absent
- Permalink to the original source, styled as monospace "instrument" text, not a generic blue link
- "Save to favorites" control, reflecting current saved state (same visual language as the Today
  favorite mark, so a user recognizes it as the same action)
- "Share" control, secondary (hairline) styling — it's an export action, not the primary one

### States
- **Default**: full explanation + credit (if present) + permalink + save + share, all resolved.
- **Loading**: same ink-line motif as Today, scoped to this screen; actions (save/share) are
  hidden until content resolves.
- **Empty**: not applicable, same reasoning as Today — a resolved entry always has content; use
  Error for failure to load.
- **Error**: same logbook error-page frame as Today, scoped to the Detail screen's content area,
  with its own retry control.

### Also
- **Pressed**: save control pressed state mirrors Today's favorite mark; share control gets a
  hairline-invert pressed state (it's secondary, so it never uses copper fill).
- **Saved**: identical visual contract to Today's favorite mark — saving/unsaving from either
  screen must look and feel like the same toggle.

---

## Favorites

### Layout
A staggered, asymmetric grid (varied thumbnail heights per row, not a rigid uniform grid) of
saved entries. Each cell: thumbnail, date (monospace) and title (serif display, smaller scale than
Today/Detail titles) stacked below it, plus a local-only/synced tag.

### Content
- Grid of favorite entries: thumbnail, title, date
- Per-entry local-only/synced indicator: a small monospace tag — `LOCAL` in a neutral/hairline
  treatment, `SYNCED` using the muted ink-green role — read at a glance, not as a loud badge
- Empty-state copy when no favorites exist yet

### States
- **Default**: the staggered grid, populated.
- **Loading**: grid cells render as hairline-framed placeholders with the ink-line motif inside
  each, preserving the staggered rhythm so the layout doesn't "pop" once content resolves.
- **Empty**: the logbook empty-page frame, centered in the available space: a short monospace
  status (e.g. `NO ENTRIES YET`) and one plain-language sentence pointing back to Today ("Favorite
  a picture from Today to start your logbook.").
- **Error**: the logbook error-page frame, rust monospace status (e.g. `COULD NOT LOAD
  FAVORITES`), with a retry control.

### Also
- **Pressed**: tapping a grid cell gives a hairline-invert pressed state on the whole cell (not a
  shadow lift).
- **Saved**: every entry in this grid is, by definition, in the saved state — there is no unsaved
  variant here; the local-only/synced tag is the only per-entry state distinction.

---

## Login (optional)

### Layout
A single, vertically-centered column on the dark surface — deliberately more symmetric and quiet
than the other three screens, since it's a momentary gate rather than content. Email/password
fields and the Google option sit on a `Surface raised` panel with a hairline edge (no shadow,
no rounded "card" aesthetic beyond the tonal step); "continue without an account" sits below the
panel as plain text, visually secondary to both authenticated options.

### Content
- Email + password fields, labeled with monospace field labels (`EMAIL`, `PASSWORD`)
- Google sign-in option
- "Continue without an account" — plain-text, de-emphasized relative to the two sign-in paths
- Brief, monospace-styled reassurance line that skipping login keeps favorites on-device only
  (ties directly to FR-011/FR-013 so the trade-off is visible before the user chooses)

### States
- **Default**: both fields empty, both sign-in options and "continue without an account" all
  available.
- **Loading**: once a sign-in attempt starts (email/password submit or Google), the triggering
  control shows an inline monospace status (e.g. `SIGNING IN…`) in place of its label; the other
  sign-in path and "continue without an account" are disabled (hairline-dimmed) until it resolves.
- **Empty**: not applicable as a content state — the closest analog is the untouched Default
  state above (empty fields), which is not a separate state requiring different layout.
- **Error**: inline rust-colored monospace message directly under the field or control that failed
  (e.g. `INVALID CREDENTIALS`, or `GOOGLE SIGN-IN CANCELLED`), fields remain populated (except
  password), all options re-enabled for retry.

### Also
- **Pressed**: both sign-in controls get the standard pressed treatment (copper brighten for the
  primary email/password submit; hairline-invert for the secondary Google option, since
  email/password is treated as the primary path here and Google as secondary — adjust if product
  intent is the reverse).
- **Design note (not a spec requirement, flagged for product review)**: the spec only places
  Login at first run. Because logging in later migrates local-only favorites to synced (FR-015),
  consider surfacing a quiet, hairline-bordered prompt on the Favorites screen when the user is in
  local-only mode, linking back to this same Login screen — otherwise a user who initially skipped
  login has no later path to it.

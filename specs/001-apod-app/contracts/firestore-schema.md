# Contract: Firestore Sync Schema

## Collection path

```text
users/{uid}/favorites/{date}
```

- `{uid}` — the signed-in user's Firebase Auth `uid`. Firestore security rules (to be authored
  during implementation, per constitution Principle II) MUST restrict read/write on
  `users/{uid}/**` to requests authenticated as that same `uid`.
- `{date}` — `yyyy-MM-dd`, matching `Favorite.date` / `AstronomyPicture.date`. Using the date as
  the document ID (rather than an auto-generated ID) makes "does this user already have this date
  favorited" a direct document lookup instead of a query, and makes re-saving the same date an
  idempotent overwrite — the same toggle semantics as the local Room table (data-model.md).

## Document shape

```json
{
  "title": "A Quiet Night Over the Alps",
  "explanation": "Full multi-paragraph explanation text...",
  "url": "https://apod.nasa.gov/apod/image/....jpg",
  "hdUrl": "https://apod.nasa.gov/apod/image/....jpg",
  "mediaType": "IMAGE",
  "copyright": "Jane Astronomer",
  "savedAtEpochMillis": 1799300000000
}
```

Field names and types mirror `Favorite` (data-model.md) minus `date` (it's the document ID),
`syncStatus`, and `ownerUid` (both implicit: any document under `users/{uid}/favorites/` is, by
definition, that user's `SYNCED` favorite).

## Sync direction

- **Local → remote**: `FavoritesRepository.saveFavorite`/`removeFavorite` write/delete the mirrored
  document whenever the affected row's `syncStatus == SYNCED` (research.md "Sync").
- **Remote → local**: a snapshot listener on `users/{uid}/favorites` (registered while signed in)
  applies remote adds/updates/deletes to the corresponding Room rows, so a favorite saved on
  another device appears in this app's Favorites grid without the user doing anything.
- **Login migration**: `FavoritesRepository.migrateLocalFavoritesToAccount` performs a one-time
  bulk write of pre-existing `LOCAL_ONLY` rows into this collection (FR-015).
- **Sign-out**: no documents are deleted (the account's synced favorites remain in Firestore for
  next time); the local Room rows simply stop receiving remote updates until the next sign-in.

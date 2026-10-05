# Contract: NASA APOD REST API (external dependency)

This is an external contract the app *consumes*, documented here because `data-model.md`'s
`AstronomyPicture` and the Today/Detail screens depend directly on its shape.

## Endpoint

```text
GET https://api.nasa.gov/planetary/apod
```

**Query parameters**:

| Param | Required | Notes |
|---|---|---|
| `api_key` | Yes | Loaded from gitignored config, never hardcoded (research.md "Secrets handling"). |
| `date` | No | `yyyy-MM-dd`. Omitted to request today's entry. Out of scope for this feature beyond the default (no archive browsing — plan.md Structure Decision / spec Assumptions). |

## Response shape (success, `media_type: "image"`)

```json
{
  "date": "2026-10-05",
  "title": "A Quiet Night Over the Alps",
  "explanation": "Full multi-paragraph explanation text...",
  "url": "https://apod.nasa.gov/apod/image/....jpg",
  "hdurl": "https://apod.nasa.gov/apod/image/....jpg",
  "media_type": "image",
  "copyright": "Jane Astronomer"
}
```

## Response shape (success, `media_type: "video"`)

Same shape, except `hdurl` is absent/irrelevant and `url` points at an embeddable video URL
(e.g., a YouTube/Vimeo embed), per NASA's API — `AstronomyPicture.hdUrl` stays `null` whenever
`mediaType == VIDEO` (data-model.md validation rule).

## Error responses

Non-2xx responses (rate limiting, invalid key, upstream outage) have no fixed JSON error shape
this app should depend on — any non-2xx or network failure maps to the domain-level "fetch
failed" case, which every consuming use case surfaces as the Error state defined in `spec.md`
(FR-017), never a partial/empty `AstronomyPicture`.

## Mapping to the domain model

| API field | Domain field (`data-model.md`) |
|---|---|
| `date` | `date` |
| `title` | `title` |
| `explanation` | `explanation` |
| `url` | `url` |
| `hdurl` | `hdUrl` |
| `media_type` | `mediaType` (`"image"` → `IMAGE`, `"video"` → `VIDEO`) |
| `copyright` | `copyright` |

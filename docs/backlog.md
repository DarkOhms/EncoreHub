# EncoreHub backlog

## Reliability

### Make rating-history navigation recreation-safe

Replace the title-based, in-memory rating-history lookup with an ID-based Safe
Args contract. A song ID must identify the selected song across artist changes,
fragment recreation, and process recreation.

Acceptance criteria:

- Navigation passes `songId`, rather than a song title or in-memory object.
- Rating history loads the song and its ratings from the data boundary by ID.
- Instrumentation tests cover navigation for duplicate titles across artists and
  recreation with saved fragment arguments.
- Existing destinations and bottom-navigation behavior remain intact.

## Testability

- Recover an authoritative v5 Room schema and add migration tests before any
  database schema change.
- Add injectable API/client boundaries before testing Spotify and BPM behavior.
- Add injectable Firebase/Analytics and WorkManager seams before integration
  testing those services.
- Add a hermetic Activity/application test setup before UI-navigation smoke tests.

## Data correctness

### Make rating recency ordering explicit

`SongWithRatings.recentPerformanceRating()` treats the final relation entries as
the most recent ratings, but the Room relation query does not define timestamp
ordering. Define and test a chronological ordering contract before using the
result for readiness decisions.

### Prevent cross-artist set-list membership

The join table verifies that both a song and a set list exist, but does not
ensure they share an artist. Define an application-level validation or a
migration-safe schema solution, then add tests proving an artist cannot add
another artist's song to its set list.

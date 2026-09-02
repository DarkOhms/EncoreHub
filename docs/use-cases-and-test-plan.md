# EncoreHub use cases and test traceability

This document records the current behavioral baseline. The initial test suite
covers deterministic domain, Room, and shared ViewModel behavior only. Network,
Firebase, WorkManager, migration, and full UI automation are intentionally out
of scope for this first slice.

| ID | Given / When / Then | Initial test coverage |
| --- | --- | --- |
| UC-01 | **Given** no database exists, **when** the app first creates it, **then** it seeds the Student artist, the `All Songs/Exercises` master list, and C scale. | Deferred: database callback behavior needs an isolated application/database factory. |
| UC-02 | **Given** an artist, **when** a song is added, **then** the song belongs to that artist and its master list. | `SongViewModelIntegrationTest.newSongIsAddedToItsArtistsMasterList` |
| UC-03 | **Given** a duplicate title for the same artist, **when** it is saved, **then** Room does not create a second song. | `SongRoomDatabaseTest.duplicateSongTitleIsIgnoredWithinAnArtist` |
| UC-04 | **Given** the same title under different artists, **when** both are saved, **then** each artist has its own song. | `SongRoomDatabaseTest.songRelationsAreIsolatedByArtist` |
| UC-05 | **Given** a song with ratings, **when** recent performance is calculated, **then** the weighted current behavior is returned. | `SongWithRatingsTest` |
| UC-06 | **Given** a song with ratings and set-list membership, **when** it is deleted, **then** ratings and memberships are deleted with it. | `SongRoomDatabaseTest.deletingSongCascadesToRatingsAndSetListMemberships` |
| UC-07 | **Given** artist lists exist, **when** the ViewModel starts or a list is selected, **then** the current artist/list is restored and persisted. | `SongViewModelIntegrationTest.restoresArtistAndMasterListAndPersistsListChanges` |
| UC-08 | **Given** a current set list, **when** Practice or Perform derives its songs, **then** Practice sorts all songs and Perform filters before sorting. | `SongViewModelIntegrationTest.practiceAndPerformListsUseTheCurrentSetList` |
| UC-09 | **Given** selected songs for the current artist, **when** a custom list is created, **then** the new list contains those songs and becomes current. | `SongViewModelIntegrationTest.creatingAListAssociatesTheSelectedSongsAndMakesItCurrent` |

## Test boundaries

- Unit tests do not require Android or a database.
- Instrumentation tests use an in-memory `SongRoomDatabase`, the test APK's
  private context, and a test Application that does not start production workers.
  They do not use the production singleton, seed callback, Firebase, Spotify,
  BPM API, Analytics, or WorkManager.
- Tests must create all artists before songs and all lists before join rows to
  preserve the same foreign-key rules as production.

## Deferred verification

- Migration testing is blocked until an authoritative pre-v6 schema is recovered.
- Worker, Firebase, Analytics, and API behavior need injectable collaborators.
- UI and navigation smoke tests need a hermetic Activity/application setup.

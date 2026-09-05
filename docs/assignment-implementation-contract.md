# First assignment implementation contract

Status: proposed build contract, 2026-09-05. Parent document:
[Platform design](artist-fan-platform-design.md).

## Outcome and scope

A leader builds a private set list, sends it to linked musicians, and sees
each musician's readiness. Musicians can respond without adding songs to their
repertoire. Optional personal-song add/link follows as a separate milestone.
The same workflow supports a teacher assigning songs to students.

This contract specifies behavior and backend boundaries. It creates no backend
repository, Supabase project, migration, dependency, or Android feature.

Initial test scenario: one act, one controller, a three-song list, two linked
musicians playing bass, and an unrelated account for access-denial tests.
The second musician allows a real readiness comparison. Use synthetic data.

## Four-step workflow and screens

### 1. Leader builds a set list: Build/share screen

1. Choose the artist/act being managed. Verify controller access.
2. Open an existing list or create a named list under that artist.
3. Select songs from that artist's repertoire. For the first test, use seeded
   repertoire; a general catalog/editor is outside this slice.
4. Add selected songs and reorder them. Remove a list item without deleting
   the repertoire song.
5. Enter shared performance instructions, key/version, and item notes.
6. Save. Show saving, saved, or failed; retain entered values after failure.
7. Select "Assign to musicians."

An event is optional: rehearsals, proposed gigs, and teaching assignments must
work without a performance record. Saving a list does not publish it publicly.

### 2. Leader shares: Recipient selection and send

1. Select already linked recipients, optionally recording instrument/role.
2. Enter assignment title, overall instructions, and optional due date.
3. Review selected content and recipients; show that instructions and responses
   are shared with the controller, not the public.
4. Press "Send assignment."
5. Create one complete assignment snapshot, items, and recipient records
   atomically. Do not send partial assignments when a write fails.
6. Show a sent confirmation and a route to the readiness overview.

The musician's Assignments inbox shows title, sender/act, optional due date,
and response progress. Opening it shows only published assignment content.
Push/email delivery is deferred; the first slice supports inbox refresh.
Retrying Send with the same operation ID must not create duplicate assignments.

### 3. Musician responds: My assignment screen

1. Open the inbox item as the assigned artist.
2. See ordered songs, key/version, leader instructions, and current response.
3. Select a song's readiness. Proposed initial labels: Ready, Needs practice,
   Not prepared. No response displays Not assessed; it is not a low rating.
4. Optionally enter a response note labeled "Visible to leader/teacher."
5. Save the response. Display saving/saved/failed and retain unsaved input.
6. Update the response later without creating another current-response row.

The musician cannot edit leader instructions, reorder the assignment, or write
another recipient's response. No personal song or rating is created here.

Leader overview: rows are songs; columns are recipients with their roles.
Each cell displays readiness or Not assessed, its saved time, and a route to
the shared response note. Progress is assessed/active songs, not ready/total.
Refresh retrieves saved responses. A network failure is not displayed as
Not assessed or as an empty assignment.

### 4. Optional personal repertoire action: Add/link preview (milestone C)

1. From a song or selected group of songs, choose "Add to my repertoire."
   This action is independent of reporting readiness.
2. Show candidate personal-song matches with title, version, and arrangement.
3. For each item, choose an existing personal song, create a new one, or skip.
4. For an existing song, confirm the link; preserve all its fields and ratings.
5. For a new song, preview copied descriptive fields and add it to the
   recipient artist's master list through SongRepository.
6. Show linked/added status. Repeating the action must reuse its durable mapping.

Linking does not import readiness or grant the controller personal-song access.
The controller sees assignment readiness whether or not a personal link exists.
Recipient song mappings are private and must not be returned with the overview.

Rating import is a later explicit action: preview the chosen assessment, target
song, conversion to the existing rating scale, and original assessment time.
Do not infer a numeric rating from a readiness label until mapping is approved.
Imports require immutable source assessment IDs and deduplication. The first
slice's current-response record is insufficient for a full practice history.

## Minimal backend records

All cloud entity primary keys are UUIDs. Firebase UID is unique text associated
with an internal user profile, not a UUID cast or a client-selected identity.
Room Long IDs remain local. Timestamps are server-controlled where authoritative.

| Record | Minimum fields |
| --- | --- |
| user_profiles | id, firebase_uid (unique), display_name |
| artists | id, kind, controller_profile_id, display_name; linked individual artist records established by trusted provisioning for the first slice |
| artist_songs | id, artist_id, title, version/key as needed for selection |
| song_lists | id, artist_id, name |
| song_list_items | id, song_list_id, artist_song_id, position, shared list instructions |
| assignments | id, controller_profile_id, source_song_list_id, title, instructions, due_at nullable, status (draft/sent/archived), revision, creation operation ID, timestamps |
| assignment_items | id, assignment_id, source_item_id nullable, title/version/key snapshot, shared_notes, position, active, content_revision |
| assignment_recipients | id, assignment_id, recipient_artist_id, role, active |
| assignment_item_responses | id, assignment_id, item_id, recipient_id, readiness, shared_note, assessed_content_revision, version, updated_at |

Snapshots expose only deliberately shared song fields. Never copy personal song
ratings or personal practice notes from the source. Canonical song matching,
personal rating tables, payments, memberships, venues, and invitations are not
required to prove this first flow.

## Integrity and operation contract

- A list item and its song must belong to the same artist.
- One recipient row per assignment/artist; one current response per item/recipient.
- Response item and recipient must belong to the same assignment. Enforce this
  structurally with composite keys/constraints, not solely in client validation.
- The authenticated controller must be allowed to read/share the source list.
- The assignment creator cannot spoof another controller or claim another
  person's existing artist identity. First fixtures are provisioned securely.
- Only valid readiness values and positive supported revision numbers are accepted.
- Initial Send snapshots the current list version and creates the complete
  recipient set in a transaction; conflicting source edits return a conflict.
- Response writes include an expected version. A stale write returns the current
  response for review instead of silently overwriting a newer assessment.
- Archived/revoked assignments reject new responses. Published item snapshots
  and responses survive source-list removal; source deletion is restricted or
  the source reference becomes nullable, without cascading into responses.

Conceptual operations (names are not committed API routes):

| Operation | Authorization and result |
| --- | --- |
| Save source list | Artist controller; validate same-artist membership and commit list changes atomically. |
| Send assignment | Controller; validate source revision and linked recipients, snapshot content, return assignment ID; retry-safe. |
| Get my assignments | Current user's linked artist only; active published assignment summaries. |
| Get assignment content | Controller or active recipient; authorized snapshots only. |
| Save my response | Current linked recipient only; validate same assignment, active item, revision, and expected response version. |
| Get readiness overview | Assignment controller only; current response matrix and shared notes, no recipient repertoire. |

Choose direct database operations or transaction functions during backend
implementation. Every path must enforce the same authorization and atomicity.

## Access policy contract

| Data/action | Controller | Assigned musician | Unrelated/anonymous |
| --- | --- | --- | --- |
| Source artist songs/lists | Own managed artist | No additional access from assignment | Denied in this private slice |
| Assignment content | Manage own assignment | Read sent content while active | Denied |
| Recipient roster | Manage/view | Own recipient row only | Denied |
| Readiness responses | Read assignment responses | Read/write own response only | Denied |
| Controller/instructions/recipient identities | Controlled operations only | Cannot change | Denied |
| Personal song links and ratings | No access merely from being leader | Own personal data only | Denied |

Use explicit safe field projections for display names. Profile rows and full
identity data must not become publicly readable to support a name label.
The initial controller cannot overwrite a musician's self-assessment. Reporting
for an unlinked substitute requires an explicit attributed operation later.

## Backend repository handoff

Suggested separate repository name: EncoreHubBackend. Confirm its location and
hosting when starting that phase. The following is a proposed structure:

```text
README.md                         local setup, identity setup, test commands
docs/assignment-contract.md        approved contract and Android API boundary
supabase/config.toml              local project settings
supabase/migrations/              initial schema, constraints, policies, functions
supabase/tests/                   authorization and transactional behavior tests
supabase/seed.sql                 synthetic development fixtures only
supabase/functions/               only when a trusted server operation requires one
```

The backend repository becomes authoritative for executable schema and policies;
the Android repository keeps product flows and client integration documentation.
Record a backend release/contract version used by Android when integration starts.

Before the first auth integration, verify current official Supabase Firebase
third-party auth instructions, required token claims, Kotlin client compatibility,
and trusted provisioning of existing Firebase users. Use Firebase token subject
as text when resolving identity; do not assume the UUID identity conventions
used by native Supabase Auth. No service/admin credentials belong in Android or Git.

SQL policy tests with synthetic claims and a real Firebase-token integration
test serve different purposes. Run both: prove token verification/refresh and
sign-out behavior, plus rejection of another Firebase project's token. A local
claim-injection test alone does not prove the hosted identity boundary.

## Delivery milestones and acceptance checks

All checks below are planned, not executed tests.

| Milestone | Exit criteria |
| --- | --- |
| A: Backend identity and assignment foundation | Fresh local schema applies from migrations; synthetic fixtures load; controller/recipient/unrelated access tests pass; development Firebase identity flow verified. |
| B: Android readiness flow | Build/share, inbox, My assignment, and leader overview complete the three-song scenario; failed/retried operations behave correctly; readiness alone causes no Room writes. |
| C: Optional personal-song add/link | Artist-scoped matching and durable mappings work; repeated actions create no duplicates; original song data and ratings remain unchanged. |
| D: Rating import, later | History representation, numeric conversion, duplicate prevention, and rollback/undo behavior are specified before implementation. |

- AT-01: Sending once or retrying the same operation yields one complete assignment.
- AT-02: Source-song ratings/notes do not leak into the snapshot.
- AT-03: Two recipients report different readiness for the same item; leader
  sees both, while neither musician can read/write the other's response.
- AT-04: Forged controller IDs, artist IDs, cross-assignment item/recipient
  pairs, and unrelated/anonymous reads or writes are rejected by the backend.
- AT-05: Null response displays Not assessed. Offline/error states do not report
  a successful save; concurrent response edits return a conflict.
- AT-06: Removing access prevents subsequent reads/writes. Source deletion and
  assignment archival preserve saved assessment data under the retention rules.
- AT-07: Readiness-only use leaves Room songs, lists, ratings, and preferences unchanged.
- AT-08: Add/link selects only the current artist's songs, detects ambiguous
  versions, and survives repeat submission without duplicating repertoire entries.
- AT-09: Account or artist switching clears inaccessible cloud state and cannot
  apply one account's assignment import to another artist's local repertoire.
- AT-10: Teacher/student fixtures pass the same assignment tests without an act
  membership or performance record.

## Follow-up revision and history behavior

Detailed post-send editing is subsequent to the first immutable sent snapshot.
Proposed rules: publish changes explicitly; retain stable item IDs; new items
start unassessed; removals become inactive and retain responses; reorder-only
changes preserve readiness; key/arrangement changes mark earlier responses
"Needs reconfirmation" without erasing them. Ordinary instruction edits should
not blindly reset every response. Define a material-change rule before shipping.

Full response history requires immutable assessment events or versions before
rating import ships. Keep this extension separate from current-response display.

## Android integration and validation

SongRepository remains the data boundary. Fragments consume repository/ViewModel
state; they do not directly manage Supabase or credentials. Preserve Navigation,
Safe Args, artist/list preference keys, and the existing Room ownership rules.

Milestone B is initially online with explicit failure/retry behavior. Offline
queues and full sync need separate conflict and persistence design. Choose the
Supabase client and explain dependency/build implications when implementing.

For milestone C, define durable mappings scoped by Firebase account, cloud
recipient artist, local artist, assignment item, and local song. Room Long IDs
are device-local; never send one as a universal cloud identity. If mapping or
import deduplication requires Room additions, design an additive migration,
rollback approach, exported schema, and migration tests before those changes.

Backend validation covers migrations, constraints, permissions, and retries.
Android changes require narrow relevant tests, Kotlin compilation, lintDebug,
and assembleDebug; database changes also require migration preservation tests.
This documentation-only phase does not require a Gradle build.

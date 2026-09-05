# EncoreHub platform design

## Status and implementation boundary

Updated 2026-09-05. This document records the product direction for the artist,
fan, and teaching platform. It is a design proposal, not a deployed schema.

The agreed assignment direction supersedes the earlier proposals for copied
recipient lists, typed song plans, and granting teachers control over students'
personal lists. An assignment has shared content and individual responses.
A recipient can report readiness without adding any song to their repertoire.

The first deliverable is defined in [Assignment implementation contract](assignment-implementation-contract.md).
[Current Android use cases and tests](use-cases-and-test-plan.md) remain the
behavioral baseline; proposed use cases here are not implemented test coverage.

Firebase remains the identity provider and Analytics provider. Supabase is the
planned application backend. Room remains the Android personal workspace.
The future backend repository will own SQL migrations, database access policies,
server functions, and backend tests. This planning change creates none of those
resources and makes no Room migration.

## Agreed product rules

- Individuals and acts can each own a master song list and other song lists.
  The proposed cloud representation uses artist kind: individual or act.
- A song list is a song list. Performance and teaching context comes from
  related records, rather than requiring different list types.
- Each artist's songs own that artist's personal rating history independently.
- A teacher or leader controls an assignment's songs, order, and shared
  instructions. Each recipient controls their own readiness response.
- Recipients can report assignment readiness, optionally add/link songs to their
  personal repertoire, and separately choose to import eligible rating history.
- An act has a controlling account and payout recipient. Member distributions
  happen outside the app initially.
- Membership and an account are optional for a substitute musician. A person
  can participate in one performance without becoming a permanent band member.
- Individual and act profiles support optional links and up to three photos.
- Acts can publish master repertoire with a caveat that readiness for a
  particular event is not guaranteed.
- A performance can expose its selected set list and encore-ready songs from
  the act's master list. The controller chooses public availability.
- Fans can request an encore, suggest new repertoire, or request a dust-off.
  Every request flow offers an optional tip. General tips need no request.
- Teacher assignments use the same assignment workflow with private visibility.

## User types and use cases

These are contextual roles. One account can be a musician, teacher, leader,
fan, and client in different contexts; it need not choose one global role.

| User type | Use cases |
| --- | --- |
| Act controller / band leader | AC-01 Manage act profile, links, photos, and payout setup. AC-02 Add optional members and substitutes. AC-03 Maintain master list. AC-04 Schedule performances. AC-05 Build and publish set list. AC-06 Assign songs to musicians. AC-07 Publish assignment changes. AC-08 Review requests and linked tips. AC-09 Compare musicians' readiness, including prospective substitutes. |
| Individual musician | IA-01 Maintain personal repertoire and ratings, including solo work. IA-02 Optionally link an existing artist record to an account through a verified process. IA-03 Respond to assignments without repertoire import. IA-04 Optionally add/link assigned songs and import eligible ratings. |
| Unlinked substitute | SUB-01 Appear in an assignment or performance lineup without an account or permanent membership. SUB-02 Have controller-reported readiness recorded with attribution; self-service claiming and delegated reporting need a later access design. |
| Teacher | T-01 Establish a teacher/student relationship. T-02 Assign a song list with instructions and optional due date. T-03 Update shared assignment content. T-04 Review each student's responses. |
| Student | S-01 Open assignments and instructions. S-02 Practice and report readiness. S-03 Keep personal repertoire and history independent. S-04 Optionally add/link songs and import eligible ratings. |
| Fan | F-01 Browse public artists and repertoire. F-02 View performance information, published set list, and encore choices. F-03 Tip without requesting. F-04 Request an encore. F-05 Optionally tip when making any request. F-06 Suggest new or rusty songs for future consideration. |
| Potential client | C-01 Evaluate public repertoire with availability caveat. C-02 Request a future song and provide date/event details, optionally with a tip. |
| Venue, later | V-01 Maintain venue profile and associated performances. Profile ownership is outside the first implementation. |

Public browsing requires no account. Sign-in requirements for public requests
and tips remain open. The first private assignment slice uses signed-in,
already linked leader and musician accounts.

## Proposed conceptual model

Names describe cloud concepts; they do not rename existing Room entities.

| Entity | Responsibility |
| --- | --- |
| user_profiles | Internal ID, unique Firebase UID, minimal account profile. |
| artists | Individual or act; optional verified account association, controller, display details. Unlinked artists are supported. |
| artist_members | Act-to-individual membership, role and active status; no login requirement. |
| artist_photos | Up to three ordered images per individual or act. |
| song_definitions, optional later | Shared musical-work identity to assist matching; never owns personal ratings. |
| artist_songs | Artist-owned song, arrangement, and repertoire details. |
| song_ratings | Personal timestamped ratings belonging to an artist-owned song. |
| song_lists | Artist-owned master or custom lists. |
| song_list_items | Membership and order of that artist's songs in a list. |
| assignments | Controller, source list, title, instructions, optional due date and context, lifecycle/revision. |
| assignment_items | Stable assigned-song identity, snapshot of shared details, order, shared notes, active status. |
| assignment_recipients | One assigned artist and role/status per recipient; independent of band membership. |
| assignment_item_responses | Recipient's readiness and optional note shared with the controller for one assignment item. |
| assignment_song_links, later | Private recipient mapping from an assignment item to their own song; never grants repertoire access to the controller. |
| rating_imports, later | Provenance and deduplication of explicitly imported assignment work into personal history. |
| teacher_students, later | Authorized teaching relationship; membership alone must not expose a student's complete repertoire. |
| performances | Act, selected list, start time and timezone, location, public/all-ages/indoor flags, optional venue URL, publication and request/tip settings. |
| performance_members | Actual lineup, including one-time substitutes. Candidates can be assigned without being committed to the lineup. |
| requests, later | Act, requester, type, optional performance and selected-song reference, suggested title/version, notes, status, resulting repertoire link. |
| tips, later | Act, optional performance/request, sender, amount/currency, payment identifier, server-confirmed payment status and recipient attribution. |
| venues, later | Optional venue profile; performances can later link through a nullable venue ID. |

## Assignment behavior and ownership

A leader starts from an artist-owned song list. Sending it creates a shared
assignment snapshot plus recipients and responses. It does not create recipient
lists or recipient songs. Assignment access covers that snapshot, not the
source artist's private repertoire or personal song ratings.

The leader can revise the assignment without editing a recipient's repertoire.
Source-list edits do not silently rewrite a sent assignment. Proposed default:
the leader explicitly publishes changes into the assignment. Stable assignment
item IDs preserve responses across harmless reorderings; meaningful changes
such as key or arrangement are flagged for reconfirmation.

Recipients initially have no response, displayed as "Not assessed." Their prior
personal rating is never silently substituted as a current assignment response.
Each musician sees their own responses. The controller sees the recipient
comparison; peers and the public do not.

Shared leader instructions and recipient notes are separate fields. A recipient
note submitted with readiness is labeled "Visible to leader/teacher." Personal
practice notes remain private. Controller-reported substitute readiness must
remain distinguishable from a musician's own response.

The first implementation stores current assignment responses. Response history
and rating imports need an explicit follow-up design. Assignment readiness is
a separate assessment; personal ratings continue to belong to personal songs.

Adding or linking a song is optional and recipient-controlled. Linking does not
overwrite the song's title, arrangement, notes, or ratings. Matching considers
version, arrangement, and instrument; a matching title alone is insufficient.
Importing a rating is a separate action with a preview and deduplication, not a
side effect of linking.

## Public performances and requests

The act master list supplies a performance's selections. Set-list ordering and
encore availability are performance-specific. Whether a song can be both a
scheduled song and requestable is still open; do not force mutually exclusive
sections into the schema before resolving it.

Public list publication is separate from sending private assignments. Public
views expose selected titles, versions, event details, and requestability, not
musician responses or internal instructions.

Public repertoire copy must explain: these are songs the act plays or may
prepare; availability for a particular event must be confirmed with the act.
The "open to the public" event flag is distinct from whether its page is
published. Store event timezone alongside an unambiguous start instant.

Requests support encore, future repertoire, and dust-off contexts. A new-song
request can exist before a repertoire song does. At submission, ask "Would you
like to send a tip with your request?" A canceled payment leaves a valid request.
Standalone tips have no request link. Multiple later supporting tips are a
future extension, not needed for the first payment flow.

The trusted payment backend validates the act/request relationship and records
provider-confirmed payment state. Payment records preserve the recipient at
payment time if act control later changes. The public product does not promise
that tipping guarantees a song will be played. Refund behavior is still open.

## First implementation and subsequent phases

1. Establish a backend repository with reproducible Supabase migrations,
   minimal data, access rules, and tests using synthetic accounts.
2. Prove Firebase identity and assignment authorization with a controller,
   recipient, and unrelated test account.
3. Deliver the private four-screen flow in the linked contract: build/share,
   assignment inbox, musician response, leader overview. Readiness-only use
   has no Room migration.
4. Add optional personal-song add/link through SongRepository, with durable
   cloud-to-local mappings and a reviewed Room migration if required.
5. Define and implement rating-history import with duplicate prevention.
6. Extend the same assignment workflow to teaching; add invitation and
   unlinked-recipient processes.
7. Build public act/performance and fan request views, then payments and web UI.

## Existing Android data boundary

| Existing concept | Planned treatment |
| --- | --- |
| Artist | Preserve local ownership. Mapping to a cloud artist is explicit and account-scoped. |
| Song | Preserve the artist-owned song and its fields. Cloud song/assignment IDs never replace Room primary keys. |
| Rating | Preserve existing timestamps, integer values, notes, and song association. Assignment labels have no automatic numeric conversion. |
| SongList / SongListSongM2M | Preserve list ownership and local joins. Assignment receipt creates no local list. |
| Instrument | Keep existing behavior; account for instrument/arrangement when linking songs. |
| PreferencesManager | Preserve keys and selected artist/list behavior. |

Supabase migrations and Room migrations are distinct deliverables. Before a Room
change, review entities, DAOs, relations, migrations, and exported schemas
together; test preservation against a copy of existing data and define rollback.
Do not use destructive migration as a recovery strategy.

## Open decisions and proposed defaults

The contract identifies implementation defaults so they can be reviewed without
treating every earlier suggestion as an agreed product rule.

- Assignment scale: proposed labels are Ready, Needs practice, Not prepared,
  with Not assessed represented by no response.
- Assignment revisions: proposed explicit publication and reconfirmation for
  key/arrangement changes; detailed editing is a later slice.
- Personal import: matching/linking and adding songs are optional. History
  selection, mapping into existing numeric ratings, and undo remain open.
- Solo public presentation: artist kind supports individual and act records;
  whether solo musicians need another public record is undecided.
- Anonymous requests, tip amounts/refunds, public totals, and controller
  transfer processes are later public-platform decisions.
- Teaching onboarding, minors/guardian account needs, and substitute account
  claiming require design before those user flows ship.
- Backup, full Room/cloud synchronization, offline assignment editing,
  reusable templates, co-controllers, and public venue accounts are separate
  features; the first assignment contract does not specify them.

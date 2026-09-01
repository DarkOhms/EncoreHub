# Encore Hub

Android application written primarily in Kotlin.

## Working rules
- Preserve existing architecture unless there is a good reason to change it.
- Prefer small, reviewable changes.
- Do not rewrite unrelated code.
- Run relevant Gradle tests/build checks after changes.
- Explain architectural changes before making large refactors.
- Do not add dependencies without explaining why.
- Preserve Android Navigation behavior and existing database data.

## Architecture
- This is a single-module Kotlin Android app using XML layouts, Fragments, Navigation Component,
  View/Data Binding, LiveData/Flow, and a shared `SongViewModel`.
- Keep UI work in its relevant fragment/layout pair. Preserve navigation destinations, actions,
  Safe Args, and bottom-navigation behavior.
- `SongRepository` is the data boundary. Room database, entities, DAOs, migrations, and exported
  schemas are under `app/src/main/java/com/lukemartinrecords/encorehub/data/` and `app/schemas/`.
- Firebase provides Auth and Analytics, not the application data store. Do not expose or alter
  `app/google-services.json` without explicit user approval.

## Validation
- Run the narrowest relevant Gradle task, then run `lintDebug` and `assembleDebug` for UI or data work.
- Add or update tests for behavior changes. Use Room migration tests for database changes.
- Kotlin compilation is this project's type check. No standalone formatter or linter is configured.

## Data safety
- Treat Room entities, DAOs, relations, migrations, and `app/schemas/` as one atomic change.
- Do not rely on `fallbackToDestructiveMigration()` for a user-data-preserving change.
- Preserve existing database data and `PreferencesManager` keys unless an explicit migration and
  rollback plan are approved.

## Agent coordination
- The main Codex thread owns scope, file ownership, integration, and final validation.
- Research, planning, and review agents are read-only. Only one implementation agent may edit a
  feature in a shared working directory at a time.
- Before implementation, define an explicit file-ownership list. Use separate worktrees only for
  truly disjoint file sets; the main thread integrates their work.
- Do not modify unrelated existing changes in a dirty working tree.

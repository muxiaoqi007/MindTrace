# MindTrace Reliability Hardening Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Eliminate the known data-loss paths in WebDAV sync and backup/restore, restore Android 26 compatibility, make automatic sync functional, and add regression coverage for these behaviors.

**Architecture:** Separate pure merge/path-remapping logic from Android and network side effects so it can be covered with fast JVM tests. Treat remote restore as a staged operation: download and validate every required snapshot before entering one Room transaction that replaces local data. Treat sync success as an acknowledgement of specific uploaded records rather than a blanket timestamp update.

**Tech Stack:** Kotlin, JUnit 4, kotlinx-coroutines-test, Room transactions, WorkManager, Hilt, Gson, Jetpack Compose/AndroidX.

---

### Task 1: Add JVM test infrastructure

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/java/com/mindtrace/diary/core/sync/SyncMergePolicyTest.kt`
- Create: `app/src/test/java/com/mindtrace/diary/domain/usecase/backup/BackupImagePathMapperTest.kt`

**Step 1: Add test dependencies**

Add `kotlinx-coroutines-test` and keep the tests Android-free by testing pure Kotlin collaborators.

**Step 2: Write failing merge-policy tests**

Cover local-only, remote-only, newer-local, newer-remote, equal-timestamp, and tombstone records. The merge result must retain the union of record IDs and resolve conflicts deterministically by `updatedAt`.

**Step 3: Write failing image-remapping tests**

Verify that both the compatibility `images` list and every `ContentBlockData(type = "IMAGE")` path are remapped during export and import, while text blocks and unknown paths remain unchanged.

**Step 4: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest`

Expected: the new tests fail because the pure merge and path-mapping collaborators do not exist yet.

### Task 2: Make backup image path remapping complete

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/domain/usecase/backup/BackupImagePathMapper.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/backup/ExportDataWithImagesUseCase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/backup/ImportDataWithImagesUseCase.kt`

**Step 1: Implement a pure mapper**

The mapper must update `DiaryEntity.images`, `DiaryEntity.contentBlocks[*].path`, `DiaryEntity.entries[*].images`, and `FlashNoteEntity.images` through one supplied path mapping.

**Step 2: Use the mapper in ZIP export/import**

Collect image paths from all three diary representations, archive only existing local files, and restore all archived references to application-private paths.

**Step 3: Prevent malformed/oversized ZIP input**

Reject unexpected entry names, duplicate `data.json`, missing image references, individual entries above a bounded size, and unsupported backup versions. Delete partially extracted files when import fails.

**Step 4: Run focused tests**

Run: `./gradlew testDebugUnitTest --tests '*BackupImagePathMapperTest'`

Expected: PASS.

### Task 3: Replace lossy WebDAV upload behavior

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/core/sync/SyncMergePolicy.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/sync/SyncManager.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/sync/WebDavClient.kt`

**Step 1: Implement and test deterministic merging**

Merge complete local and remote snapshots by ID. Pick the greater `updatedAt`; on equal timestamps prefer a tombstone so deletion cannot be accidentally resurrected.

**Step 2: Download before upload**

For each entity type, download and parse the current remote snapshot, merge it with the complete local snapshot including tombstones, then upload the complete merged snapshot.

**Step 3: Make transport failures explicit**

Replace `null`/`false` error swallowing in the sync path with typed success, not-found, and failure results. Do not advance `lastSyncTime` or `syncedAt` when any required upload fails.

**Step 4: Persist merged remote changes transactionally**

Write merged records to Room and update acknowledgement timestamps only after every upload succeeds.

**Step 5: Run focused and full JVM tests**

Run: `./gradlew testDebugUnitTest --tests '*SyncMergePolicyTest'`

Expected: PASS.

### Task 4: Make destructive cloud restore atomic

**Files:**
- Modify: `app/src/main/java/com/mindtrace/diary/core/sync/SyncManager.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/database/AppDatabase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/di/DatabaseModule.kt`

**Step 1: Inject the Room database transaction boundary**

Expose a transaction helper or inject `AppDatabase` into `SyncManager`.

**Step 2: Stage all remote data**

Download, parse, and validate all required snapshots before deleting any local row. Treat a missing required snapshot differently from an empty valid snapshot.

**Step 3: Replace local data in one transaction**

Only after validation, delete and insert diaries, flash notes, and todos in a single `withTransaction` block. Roll back automatically on any failure.

**Step 4: Verify failure preserves local data**

Add a regression test at the repository/instrumented level when Room test infrastructure is available.

### Task 5: Make automatic sync functional

**Files:**
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/webdav/WebDAVSettingsViewModel.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/sync/SyncWorker.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/webdav/WebDAVSettingsScreen.kt`

**Step 1: Inject application context into the ViewModel**

Use the application context only for WorkManager scheduling.

**Step 2: Schedule or cancel work with the toggle**

After persisting the setting, call `schedulePeriodicSync` when enabled and `cancelSync` when disabled. Recreate scheduling when saved configuration changes while auto-sync is enabled.

**Step 3: Correct the UI description**

Describe the behavior as periodic network-constrained synchronization unless save operations also enqueue one-time sync work.

### Task 6: Fix platform and notification correctness

**Files:**
- Modify: `app/src/main/java/com/mindtrace/diary/data/mapper/AIMappers.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/notification/NotificationHelper.kt`

**Step 1: Replace API-34-only date conversion**

Use `Instant.ofEpochMilli(date).atZone(zone).toLocalDate()`, which works with the configured Java-time desugaring/API baseline.

**Step 2: Make notification calls permission-safe**

Perform the permission check immediately around `notify` and catch `SecurityException` to cover permission revocation races.

**Step 3: Run lint**

Run: `./gradlew lintDebug`

Expected: no `NewApi` or `MissingPermission` errors.

### Task 7: Verify the complete change

**Files:**
- Modify only if verification reveals regressions.

**Step 1: Run JVM tests**

Run: `./gradlew testDebugUnitTest`

Expected: PASS with executed tests, not `NO-SOURCE`.

**Step 2: Run static analysis**

Run: `./gradlew lintDebug`

Expected: PASS; remaining warnings are documented follow-up work.

**Step 3: Build debug and release variants**

Run: `./gradlew assembleDebug assembleRelease`

Expected: both variants compile; release signing remains a separate delivery concern.

**Step 4: Inspect the worktree**

Run: `git diff --check && git status --short`

Expected: no whitespace errors and no changes to the pre-existing `viking-storage/` directory.

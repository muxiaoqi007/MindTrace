# MindTrace Personalization Suite Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the complete personalization suite proposed for MindTrace: daily receipts, memory walks, a daily material basket, private/AI exclusions, customizable life facets, AI follow-up reflection, future-self capsules, weekly magazines, life storylines, a personal lexicon, one-second life, map footprints, and print-ready exports.

**Architecture:** Add each capability as an independent vertical slice with a pure domain policy, repository/use-case layer, Compose screen, and focused tests. Persist only features that require durable user state in Room, using additive migrations and backup/sync coverage; keep derived views such as receipts, magazines, lexicons, and storylines reproducible from source records whenever possible.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Room, Hilt, Coroutines Flow, WorkManager, Android Media APIs, Canvas/PDF, FileProvider, OpenAI-compatible provider, JUnit 4.

---

### Task 1: Daily receipt — implemented, pending device review

**Files:**
- Create: `domain/model/DailyReceipt.kt`
- Create: `domain/usecase/receipt/DailyReceiptBuilder.kt`
- Create: `domain/usecase/receipt/GetDailyReceiptUseCase.kt`
- Create: `ui/screens/receipt/*`
- Create: `core/export/DailyReceiptImageRenderer.kt`
- Test: `app/src/test/java/com/mindtrace/diary/domain/usecase/receipt/DailyReceiptBuilderTest.kt`

**Verification:** 28 JVM tests, Lint 0 errors, debug/release builds pass. Device-review the PNG and tune typography before release.

### Task 2: Random memory walk

**Files:**
- Create: `domain/model/MemoryWalk.kt`
- Create: `domain/usecase/memorywalk/MemoryWalkPlanner.kt`
- Create: `domain/usecase/memorywalk/GetMemoryWalkUseCase.kt`
- Create: `ui/screens/memorywalk/MemoryWalkViewModel.kt`
- Create: `ui/screens/memorywalk/MemoryWalkScreen.kt`
- Modify: `ui/navigation/Screen.kt`
- Modify: `ui/navigation/NavGraph.kt`
- Modify: `ui/screens/home/HomeScreen.kt`
- Test: `app/src/test/java/com/mindtrace/diary/domain/usecase/memorywalk/MemoryWalkPlannerTest.kt`

**Steps:** Write failing deterministic selection tests; implement surprise/keyword/mood modes, three diverse stops, exclusion of today/deleted items, and stable seeded selection; add route, setup screen, stop cards, skip/continue, source opening, and “此刻回应” saved as a flash note; run focused and full tests.

### Task 3: Daily material basket and per-entry privacy

**Files:**
- Modify: `core/database/entity/DiaryEntity.kt`
- Modify: `core/database/entity/FlashNoteEntity.kt`
- Modify: `core/database/AppDatabase.kt`
- Create: `domain/model/DailyMaterialBasket.kt`
- Create: `domain/usecase/material/GetDailyMaterialBasketUseCase.kt`
- Create: `ui/screens/material/DailyMaterialBasketScreen.kt`
- Modify: diary/flash editors and AI context builders
- Test: migration, basket grouping, and AI exclusion tests

**Steps:** Add `excludeFromAI` and `excludeFromResurfacing` flags with a migration; include toggles in editors; enforce flags in every AI and resurfacing query; group today's fragments into an editable draft; allow local deterministic stitching and opt-in AI stitching; save as a diary only after preview.

### Task 4: Custom life facets and evidence-backed correlations

**Files:**
- Create: life-facet definitions and daily check-in Room entities/DAOs/repositories
- Create: `domain/usecase/facets/FacetCorrelationCalculator.kt`
- Create: facet setup, quick check-in, and insight screens
- Test: migration, custom option, and correlation calculations

**Steps:** Let users define icon/color/name/options; record facets in seconds; calculate counts and mood deltas locally with minimum-sample disclosure; show evidence dates and avoid causal wording.

### Task 5: AI follow-up reflection

**Files:**
- Create: `domain/usecase/ai/GenerateFollowUpQuestionUseCase.kt`
- Create: follow-up UI state/components in diary detail
- Modify: AI settings and context privacy controls
- Test: JSON parsing, grounding, disabled/private behavior

**Steps:** Generate at most one grounded question after save; display nothing unless requested; provide answer/another/stop actions; save answers as linked diary entries; never analyze excluded entries.

### Task 6: Future-self time capsules

**Files:**
- Add time-capsule entity/DAO/repository and Room migration
- Add WorkManager unlock notification
- Create capsule composer, sealed list, and reveal screen
- Test: unlock policy, timezone behavior, migration, and worker scheduling

**Steps:** Support text/photo/voice, unlock date, prediction and question; encrypt/keep local through existing app storage; prevent normal viewing before unlock; exclude sealed content from AI and search; allow deletion without early reveal.

### Task 7: Weekly life magazine

**Files:**
- Create: weekly magazine model/builder/use case
- Create: magazine Compose preview and Canvas/PDF exporter
- Test: week boundaries, highlights, empty weeks, deterministic layout inputs

**Steps:** Build cover, seven-day mood strip, top moments, flash excerpts, completed goals, unresolved thread, and next-week carryover; use AI editor note only when explicitly enabled; export image/PDF.

### Task 8: Life storylines

**Files:**
- Add confirmed storyline and source-link entities with migration
- Create candidate detector, approval flow, storyline timeline screen
- Test: candidate grounding, merge/split, rejected candidate suppression

**Steps:** Detect recurring projects/relationships/skills as candidates; require user confirmation; show evidence-linked chronology; allow rename, merge, split, archive, and source corrections.

### Task 9: Personal lexicon

**Files:**
- Create lexicon extractor/model/use case and evidence browser
- Reuse candidate confirmation patterns from AI memory
- Test: normalization, evidence links, correction persistence, exclusions

**Steps:** Derive people, places, phrases, wishes, and changing meanings; every definition links to source records; corrections override generated meanings and feed future context.

### Task 10: One-second life

**Files:**
- Add daily media-pick entity/DAO/migration
- Create media picker/timeline screen and WorkManager reminder
- Create Media3-compatible monthly montage exporter
- Test: one-item-per-day policy, missing-day behavior, ordering

**Steps:** Pick one existing image/video or capture a clip; trim video to one second; preserve local URI safely; render monthly montage with date captions; allow gaps rather than auto-inventing media.

### Task 11: Map footprints

**Files:**
- Add optional structured coordinates to diary records with migration
- Add privacy-aware location picker and map screen
- Test: opt-in storage, coordinate migration, exclusion/export behavior

**Steps:** Never collect background location; store location only on explicit entry action; browse clusters by month/place; open source entry; support hiding/removing locations independently of diary text.

### Task 12: Print-ready archive

**Files:**
- Extend receipt/magazine exporters
- Create print configuration and Android Print Framework adapter
- Test: page selection, date/tag filters, pagination input

**Steps:** Export receipts, weekly magazines, or selected journal ranges to paginated PDF; include preview, date/tag filtering, font scale, photos, metadata, and Android system print; physical fulfillment remains an external optional integration, not a privacy-default dependency.

### Task 13: Completion audit and release

**Steps:** Verify every named feature via source, migration tests, domain tests, UI/runtime evidence, Lint, debug/release builds, backup/sync coverage, privacy exclusions, and exported artifacts. Only then bump version, commit, push, and publish an APK after explicit user approval.

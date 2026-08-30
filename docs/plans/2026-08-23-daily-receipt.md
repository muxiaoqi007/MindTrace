# Daily Receipt Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a daily receipt that summarizes a selected day's diaries, flash notes, todos, mood, keywords, and representative quote, with an in-app preview and shareable PNG export.

**Architecture:** Keep aggregation deterministic and local-first in a domain use case fed by the existing repositories. Render the same `DailyReceipt` model in Compose for preview and in an Android Canvas renderer for PNG sharing; no AI call is required for v1, so the receipt works offline and does not expose diary content externally.

**Tech Stack:** Kotlin, Coroutines Flow, Hilt, Jetpack Compose Material 3, Android Canvas/Bitmap, FileProvider, JUnit 4.

---

### Task 1: Define deterministic receipt aggregation

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/domain/model/DailyReceipt.kt`
- Create: `app/src/main/java/com/mindtrace/diary/domain/usecase/receipt/DailyReceiptBuilder.kt`
- Test: `app/src/test/java/com/mindtrace/diary/domain/usecase/receipt/DailyReceiptBuilderTest.kt`

**Step 1: Write the failing tests**

Cover diary-date fallback, completed-todo date filtering, mood selection, representative quote cleanup, keyword frequency, and empty days.

**Step 2: Run the focused test and verify failure**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest --tests 'com.mindtrace.diary.domain.usecase.receipt.*'`

Expected: compilation fails because `DailyReceiptBuilder` does not exist.

**Step 3: Implement the model and pure builder**

`DailyReceipt` contains date, mood, diary/flash/completed/pending counts, total diary word count, quote, keywords, and display lines. `DailyReceiptBuilder.build(date, diaries, flashNotes, todos)` performs all date filtering and truncation without Android dependencies.

**Step 4: Run the focused tests**

Expected: all receipt builder tests pass.

### Task 2: Load receipt data from repositories

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/domain/usecase/receipt/GetDailyReceiptUseCase.kt`
- Create: `app/src/main/java/com/mindtrace/diary/ui/screens/receipt/DailyReceiptUiState.kt`
- Create: `app/src/main/java/com/mindtrace/diary/ui/screens/receipt/DailyReceiptViewModel.kt`

**Step 1: Implement repository aggregation**

Combine `getDiariesByDate(date)`, `getFlashNotesByDate(date)`, and `getAllTodos()`, then pass their latest values to the pure builder. Keep the stream reactive so the receipt updates if the underlying day changes.

**Step 2: Implement date navigation state**

The ViewModel exposes the selected date, loading/error state, and receipt. Add previous day, next day (not beyond today), and return-to-today actions.

**Step 3: Compile**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew compileDebugKotlin`

Expected: build succeeds.

### Task 3: Build the receipt preview screen

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/ui/screens/receipt/DailyReceiptScreen.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/navigation/Screen.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/home/HomeScreen.kt`

**Step 1: Add the route and home entry point**

Add a `receipt` route and a receipt icon to the home top bar.

**Step 2: Create the thermal-paper-style preview**

Show date navigation, perforated receipt card, daily counts, mood, quote, keywords, and the closing line `TOTAL 认真生活了 1 天`. Handle empty days honestly instead of inventing content.

**Step 3: Add accessibility labels and loading/error states**

All icon buttons receive Chinese content descriptions; share is disabled while loading.

### Task 4: Export and share a PNG

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/core/export/DailyReceiptImageRenderer.kt`
- Create: `app/src/main/java/com/mindtrace/diary/core/export/DailyReceiptShareManager.kt`
- Modify: `app/src/main/res/xml/file_paths.xml`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/receipt/DailyReceiptScreen.kt`

**Step 1: Render a fixed-width bitmap**

Use Android Canvas to create a readable 1080px PNG with wrapped quote/keyword lines, stable spacing, and the same facts as the Compose preview.

**Step 2: Save into the app cache**

Write `cacheDir/shared_receipts/MindTrace-receipt-YYYY-MM-DD.png`, replacing only the same day's cached export.

**Step 3: Share through FileProvider**

Use `Intent.ACTION_SEND`, MIME `image/png`, `FLAG_GRANT_READ_URI_PERMISSION`, and an Android chooser. Never write into public storage without explicit user interaction.

### Task 5: Verify the feature

**Files:**
- Modify if needed: receipt files above

**Step 1: Run all unit tests**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest`

Expected: all tests pass.

**Step 2: Run Android Lint**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew lintDebug`

Expected: 0 errors.

**Step 3: Build both variants**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug assembleRelease`

Expected: both APK builds succeed.

**Step 4: Inspect the final diff**

Run: `git diff --check && git status --short`

Expected: no whitespace errors; `viking-storage/` remains untouched and untracked.

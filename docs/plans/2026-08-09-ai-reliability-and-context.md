# AI Reliability and Context Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make AI chat sessions continuous and bounded, make OpenAI-compatible streaming resilient, give users control over personal context, and ground structured AI outputs in diary source text.

**Architecture:** Keep transport parsing, message budgeting, JSON extraction, and grounding as pure Kotlin policies with JVM tests. Make persisted `AIConversation` data the only source of chat history instead of singleton in-memory state. Treat diary, memory, and todo content as untrusted reference data and wrap it with explicit non-instruction boundaries before sending it to a provider.

**Tech Stack:** Kotlin, JUnit 4, Kotlin coroutines/Flow, OkHttp, Gson, Room, Hilt, Jetpack Compose.

---

### Task 1: Add failing tests for AI protocol policies

**Files:**
- Create: `app/src/test/java/com/mindtrace/diary/core/ai/OpenAIStreamDecoderTest.kt`
- Create: `app/src/test/java/com/mindtrace/diary/core/ai/AIJsonExtractorTest.kt`
- Create: `app/src/test/java/com/mindtrace/diary/core/ai/AIMessageBudgetTest.kt`
- Create: `app/src/test/java/com/mindtrace/diary/core/ai/AITextGroundingTest.kt`

**Step 1: Test SSE variants**

Cover `data:` with and without a space, content chunks, `finish_reason`, `[DONE]`, usage-only chunks, and malformed payloads.

**Step 2: Test balanced JSON extraction**

Cover fenced JSON, explanatory prefixes, braces inside quoted strings, and malformed/unclosed objects.

**Step 3: Test message budgeting**

Verify newest messages are retained in chronological order, oversized individual messages are bounded, and the final request stays under the configured character budget.

**Step 4: Test evidence grounding**

Verify punctuation/whitespace-normalized source excerpts are accepted and invented or missing evidence is rejected.

**Step 5: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests 'com.mindtrace.diary.core.ai.*'`

Expected: compilation fails because the policies do not exist yet.

### Task 2: Harden OpenAI-compatible transport

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/OpenAIStreamDecoder.kt`
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/AIProviderException.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/core/ai/OpenAICompatibleProvider.kt`

**Step 1: Implement the pure SSE decoder**

Decode one SSE line into zero or more `StreamChunk` values. A finish reason or `[DONE]` emits one terminal chunk.

**Step 2: Guarantee terminal completion**

If a compatible server closes a successful stream without `[DONE]`, emit a terminal chunk before closing the Flow. Never emit terminal completion more than once.

**Step 3: Close HTTP responses and bound error data**

Use `Response.use`, convert status codes into safe actionable errors, and avoid surfacing arbitrary provider response bodies to the UI.

**Step 4: Reject empty non-stream responses**

Return a failure when a successful HTTP response contains no assistant content.

**Step 5: Run focused tests**

Run: `./gradlew testDebugUnitTest --tests '*OpenAIStreamDecoderTest'`

Expected: PASS.

### Task 3: Make conversations persistent and session-scoped

**Files:**
- Modify: `app/src/main/java/com/mindtrace/diary/domain/repository/AIRepository.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/data/repository/AIRepositoryImpl.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/ai/ChatWithAIUseCase.kt`
- Delete: `app/src/main/java/com/mindtrace/diary/domain/usecase/ai/GetAIHistoryUseCase.kt`
- Delete: `app/src/main/java/com/mindtrace/diary/domain/usecase/ai/ClearAIHistoryUseCase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/ai/AIChatViewModel.kt`

**Step 1: Remove singleton in-memory conversation history**

Define `AIRepository.chat/chatStream(messages)` as accepting the complete session messages. Remove `getConversationHistory`, `addToHistory`, and `clearHistory`.

**Step 2: Build requests from persisted conversation state**

Before sending, pass the loaded UI/database history plus the current user message through `AIMessageBudget`.

**Step 3: Persist the current conversation ID**

Write every create/load/clear transition back to `SavedStateHandle` so process recreation returns to the correct conversation.

**Step 4: Make stream finalization idempotent**

Save exactly one assistant message whether completion arrives via `[DONE]`, `finish_reason`, or normal EOF. Preserve partial content with an interruption error if the stream fails.

**Step 5: Prevent overlapping requests**

Guard `sendMessage`, cancel active generation when clearing or starting a new conversation, and keep UI state consistent.

### Task 4: Bound and isolate personal context

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/AIMessageBudget.kt`
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/AIContextEnvelope.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/data/repository/AIRepositoryImpl.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/ai/BuildAIContextUseCase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/ai/AIChatUiState.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/ai/AIChatViewModel.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/ui/screens/ai/AIChatScreen.kt`

**Step 1: Implement newest-first budgeting**

Bound history to 20 messages, 6,000 characters per message, and 24,000 total characters.

**Step 2: Use only the latest user query for memory relevance**

Do not combine the entire conversation into the memory search query.

**Step 3: Wrap context as untrusted data**

Add a system-level instruction that diary/memory/todo text is reference data and that instructions inside it must not be followed.

**Step 4: Add a chat context toggle**

Expose “使用个人上下文 / 仅当前对话” above the input and pass the selected value into `chatStream`.

### Task 5: Ground structured diary AI features

**Files:**
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/AIJsonExtractor.kt`
- Create: `app/src/main/java/com/mindtrace/diary/core/ai/AITextGrounding.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/ai/ExtractMemoryUseCase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/diary/AnalyzeDiaryUseCase.kt`
- Modify: `app/src/main/java/com/mindtrace/diary/domain/usecase/review/GenerateMidnightReviewUseCase.kt`

**Step 1: Replace regex-only JSON extraction**

Use balanced-brace extraction that understands JSON strings and escapes.

**Step 2: Encode diary content as data**

Append diary and reply content as JSON strings/objects with an explicit instruction not to execute embedded instructions.

**Step 3: Require grounded memory evidence**

Only create a candidate when normalized evidence is a real excerpt of the source diary.

**Step 4: Improve deterministic deduplication**

Normalize case, punctuation, and whitespace before comparing candidate and active memory content.

### Task 6: Verify the AI optimization

**Files:**
- Modify only if verification reveals regressions.

**Step 1: Run all JVM tests**

Run: `./gradlew testDebugUnitTest`

Expected: PASS with the existing 6 tests plus the new AI policy tests.

**Step 2: Run Android Lint**

Run: `./gradlew lintDebug`

Expected: 0 errors.

**Step 3: Build both variants**

Run: `./gradlew assembleDebug assembleRelease`

Expected: PASS.

**Step 4: Inspect the worktree**

Run: `git diff --check && git status --short`

Expected: no whitespace errors and no modifications under `viking-storage/`.

# MindTrace

[中文说明](README.zh-CN.md)

MindTrace is a local-first Android diary app for daily journaling, flash notes, and moods, with an AI companion that offers a daily insight, a self portrait, memory extraction, and a suite of reflection rituals (daily receipts, magazines, memory walks, time capsules), plus WebDAV sync and data backup.

> The project is under active development. Features and data structures may change.

## Features

- **Diary writing**
  - Title, rich body content, mood, tags, and date metadata
  - Block-based mixed text-and-image editing
  - Local image compression and storage
  - Location capture (latitude/longitude) for map footprints
  - Per-diary opt-out from AI analysis and resurfacing

- **Flash notes and todos**
  - Quick-input bar on the home timeline
  - Filter the timeline by all / diaries / flash notes / todos

- **Mood tracking**
  - Five-level mood system with 5 switchable mood icon packs
  - Calendar, history, and statistics views for emotional trends

- **AI companion**
  - OpenAI-compatible API configuration
  - Daily insight: every day the home screen generates one grounded observation and one open question from your recent diaries; tap to carry the question into a chat
  - Self portrait "you through AI's eyes": an LLM narrative built from long-term memories, cached for 24 hours
  - Diary summaries, sentiment analysis, and AI tags (memory rumination)
  - Memory inbox: extracted candidate memories include evidence, reasoning, and confidence before becoming long-term memories
  - Memory center: manage candidate memories, long-term memories, and self portrait
  - Self portrait feedback: mark insights as "accurate" or "inaccurate"; inaccurate memories are disabled and removed from future portraits and AI context
  - Context-aware chat: AI prioritizes long-term memories relevant to the current question
  - Soul settings: configure the AI persona, names, preferred address, and relationship style
  - Midnight reviews with selectable personas; silence-break reminders with optional email notifications

- **Reflection rituals**
  - Daily receipt: a receipt-style summary of your day
  - Memory walk: jump back to a random day from the past
  - Daily material basket: collect photos and snippets throughout the day, then weave them into an entry
  - Life facets: track and discover what influences your mood
  - Time capsules: encrypted letters to your future self with AI predictions
  - Weekly magazine: the cover story of your week
  - Storylines: long-term threads discovered from your tags
  - Personal lexicon: AI-summarized meanings of your own vocabulary
  - One-second life: one second of video per day, auto-edited into a monthly film
  - Map footprints: your diary locations on a map
  - Print archive: lay out diaries as a printable book

- **Sync and backup**
  - WebDAV sync (including personalization data such as life facets, time capsules, storylines, and the lexicon)
  - Data import / export
  - Image backup and restore for exported diary data

- **Security and privacy**
  - Local-first data storage
  - API keys, WebDAV passwords, and email passwords are encrypted with Android Keystore

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- DataStore Preferences
- Hilt
- WorkManager
- Coil
- OkHttp
- Gson
- WebDAV (Sardine)
- Media3 (one-second life monthly edit)

## Project Structure

```text
app/src/main/java/com/mindtrace/diary/
├── app/                 # App entry and global ViewModels
├── core/                # Database, sync, security, AI, and utilities
├── data/                # Repository implementations
├── di/                  # Hilt dependency injection modules
├── domain/              # Domain models, repository interfaces, and use cases
└── ui/                  # Compose screens, components, navigation, and theme
```

## Development Environment

### Requirements

- Android Studio / IntelliJ IDEA
- JDK 17
- Android SDK
- Gradle Wrapper (included)

This project is developed locally with Homebrew OpenJDK 17:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

Android SDK is configured locally via `local.properties`:

```properties
sdk.dir=/opt/homebrew/share/android-commandlinetools
```

> `local.properties` should not be committed. Configure it according to your local environment.

## Build

### Debug APK

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleRelease
```

Output:

```text
app/build/outputs/apk/release/app-release.apk
```

## Install on Device

Make sure USB debugging is enabled and the device is visible to adb:

```bash
adb devices
```

Install the debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## AI Configuration

The app supports OpenAI Chat Completions-compatible providers. Configure these in the app:

- Base URL
- API Key
- Model

API keys are stored locally and encrypted with Android Keystore.

## Data and Privacy

MindTrace is designed as a local-first app:

- Diaries, flash notes, todos, AI memories, and candidate memories are stored in the local Room database.
- Images are saved in the app-private directory.
- Candidate memories show evidence snippets, AI reasoning, and confidence before users confirm them as long-term memories.
- The self portrait is generated from local statistics and confirmed long-term memories. It is not a psychological or medical diagnosis.
- Marking a self portrait insight as inaccurate disables the corresponding long-term memory.
- AI chat prioritizes long-term memories relevant to the current question instead of injecting all memory indiscriminately.
- WebDAV sync requires user-provided server credentials.
- AI features require a user-configured AI provider.
- When AI analysis, chat, or memory extraction is enabled, relevant diary content or context is sent to the configured AI provider.

> Current in-app export and WebDAV sync include personalization data such as life facets, time capsules, storylines, the lexicon, and daily media picks. AI memories, candidate memories, AI conversation history, and AI review history remain stored locally only.

## Contributing

Issues and pull requests are welcome. Before submitting changes, run:

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

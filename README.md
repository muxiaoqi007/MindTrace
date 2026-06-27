# MindTrace

[中文说明](README.zh-CN.md)

MindTrace is a local-first Android diary app for daily journaling, flash notes, moods, todos, AI reflection, memory extraction, WebDAV sync, and data backup.

> The project is under active development. Features and data structures may change.

## Features

- **Diary writing**
  - Title, rich body content, mood, tags, and date metadata
  - Block-based mixed text-and-image editing
  - Local image compression and storage

- **Flash notes and todos**
  - Quickly capture thoughts, ideas, and tasks
  - Keep lightweight notes connected with daily diary records

- **Mood tracking**
  - Five-level mood system
  - Calendar, history, and statistics views for emotional trends

- **AI capabilities**
  - OpenAI-compatible API configuration
  - Diary summaries, sentiment analysis, and AI tags
  - Memory inbox: extracted candidate memories include evidence, reasoning, and confidence before becoming long-term memories
  - Memory center: manage candidate memories, long-term memories, and self portrait
  - Self portrait feedback: mark insights as "accurate" or "inaccurate"; inaccurate memories are disabled and removed from future portraits and AI context
  - Context-aware chat: AI prioritizes long-term memories relevant to the current question
  - Soul settings: configure the AI persona, names, preferred address, and relationship style
  - Midnight reviews and silence-break reminders

- **Sync and backup**
  - WebDAV sync
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

> Current in-app export and WebDAV sync do not include AI memories, candidate memories, AI conversation history, or AI review history.

## Contributing

Issues and pull requests are welcome. Before submitting changes, run:

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

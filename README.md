# MindTrace

MindTrace 是一款本地优先的 Android 日记应用，专注于记录日常、闪念、心情和待办，并提供 AI 回顾、记忆提取、WebDAV 同步与数据备份能力。

> 当前项目处于持续开发阶段，功能和数据结构仍可能调整。

## 功能特性

- **日记记录**
  - 支持标题、正文、心情、标签、日期等基础信息
  - 支持块级图文混排编辑，文字和图片可以按内容顺序展示
  - 支持图片本地压缩与保存

- **闪念与待办**
  - 快速记录灵感、临时想法和待办事项
  - 可与日记内容形成连续的生活记录

- **心情追踪**
  - 支持多档心情状态
  - 可结合日历和历史记录查看情绪变化

- **AI 能力**
  - 支持 OpenAI Compatible 接口配置
  - 日记摘要、情感分析、AI 标签
  - 候选记忆收件箱：AI 提取的新理解需确认后才进入长期记忆
  - 记忆中心：集中管理候选记忆、长期记忆和自我画像
  - Soul 设置：配置 AI 人格、称呼、名称和相处方式
  - 自我画像：基于本地日记统计和已确认长期记忆生成初版画像
  - 午夜回顾与沉默唤醒

- **同步与备份**
  - WebDAV 同步
  - 数据导入 / 导出
  - 图片随数据一起备份与恢复

- **安全与隐私**
  - 数据默认存储在本地
  - API Key、WebDAV 密码、邮箱密码等通过 Android Keystore 加密后存储

## 技术栈

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
- WebDAV（Sardine）

## 项目结构

```text
app/src/main/java/com/mindtrace/diary/
├── app/                 # 应用入口与全局 ViewModel
├── core/                # 数据库、同步、安全、AI、工具类
├── data/                # Repository 实现
├── di/                  # Hilt 依赖注入模块
├── domain/              # 领域模型、Repository 接口、UseCase
└── ui/                  # Compose 页面、组件、导航与主题
```

## 开发环境

### 基础要求

- Android Studio / IntelliJ IDEA
- JDK 17
- Android SDK
- Gradle Wrapper（项目已包含）

本项目本地开发使用 Homebrew 安装的 OpenJDK 17：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

Android SDK 默认配置见 `local.properties`：

```properties
sdk.dir=/opt/homebrew/share/android-commandlinetools
```

> `local.properties` 不应提交到仓库，请按自己的本地环境配置。

## 构建

### Debug APK

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
```

构建产物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleRelease
```

构建产物：

```text
app/build/outputs/apk/release/app-release.apk
```

## 安装到设备

确保设备已开启 USB 调试，并能被 adb 识别：

```bash
adb devices
```

安装 Debug 包：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## AI 配置说明

应用内可配置兼容 OpenAI Chat Completions 风格的接口，包括：

- Base URL
- API Key
- Model

密钥只存储在本机，并通过 Android Keystore 加密。

## 数据与隐私

MindTrace 采用本地优先设计：

- 日记、闪念、待办等数据存储在本地 Room 数据库中
- 图片保存到应用私有目录
- AI 候选记忆和长期记忆默认保存在本地，候选记忆需用户确认后才会成为长期记忆
- 自我画像初版基于本地统计和已确认长期记忆生成，不是心理或医学诊断
- WebDAV 同步需要用户自行配置服务地址和账号
- AI 功能需要用户自行配置 API 服务
- 开启 AI 分析、聊天或记忆提取时，相关日记内容/上下文会发送给用户配置的 AI 服务商

> 当前 App 内导出和 WebDAV 同步暂不包含 AI 记忆、候选记忆、AI 会话历史和 AI 回信历史。

## 贡献

欢迎提交 Issue 和 Pull Request。建议在提交前先运行：

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

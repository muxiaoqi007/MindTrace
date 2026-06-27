# MindTrace 开发文档

## 版本历史

### v1.5.0
**AI 记忆与 Soul 配置 MVP**

#### 核心功能
- **候选记忆收件箱** - AI 从日记中提取的新理解先进入候选箱，用户确认后才写入长期记忆
- **记忆中心** - 聚合展示待确认候选、长期记忆数量、分类分布和自我画像入口
- **Soul 设置** - 独立配置 AI 人格、用户称呼、AI 名称、相处偏好和自定义提示词
- **自我画像初版** - 基于本地日记统计和已确认长期记忆生成记录概览、偏好、目标、关系和高频标签
- **聊天上下文修复** - 修复当前用户消息可能重复注入 AI prompt 的问题

#### 数据库迁移
- 版本 7 → 8：新增 `ai_memory_candidates` 表
- `ExtractMemoryUseCase` 从“直接写长期记忆”改为“写入候选记忆”

#### 新增路由
- `ai/memory/center` - 记忆中心
- `ai/memory/inbox` - 候选记忆
- `ai/soul` - Soul 设置
- `ai/self-portrait` - 自我画像

#### 已知限制
- 候选记忆仅做完全文本去重，暂不支持语义去重/向量检索
- 自我画像初版仅做本地聚合，不调用 LLM 生成总结
- AI 记忆、候选记忆、AI 会话和 AI 回信暂不包含在 App 内导出与 WebDAV 同步中
- 保存日记后的 AI 分析仍使用现有异步协程，暂未升级为 WorkManager 可靠队列
- 仍基于 OpenAI-compatible provider，暂未实现多 Provider 原生适配和多 Agent 编排

---
### v1.4.0 (2026-01-27)
**AI 伙伴功能**

#### 核心功能
- **AI 聊天** - 与 AI 进行对话，AI 会阅读用户日记提供个性化回复
- **会话管理** - 支持多会话、历史记录、新建/删除会话
- **长期记忆** - AI 记住用户的偏好、性格、重要事件等
- **流式响应** - 实时显示 AI 回复内容

#### 新增文件
| 目录 | 文件 | 描述 |
|------|------|------|
| `core/ai/` | `ChatModels.kt` | 聊天消息模型 |
| | `LLMProvider.kt` | LLM 提供者接口 |
| | `OpenAICompatibleProvider.kt` | OpenAI 兼容 API 实现 |
| `core/database/dao/` | `AIConversationDao.kt` | 会话数据访问 |
| | `AIMemoryDao.kt` | 记忆数据访问 |
| `core/database/entity/` | `AIConversationEntity.kt` | 会话实体 |
| | `AIMemoryEntity.kt` | 记忆实体 |
| `domain/model/` | `AIConversation.kt` | 会话领域模型 |
| | `AIMemory.kt` | 记忆领域模型 |
| `domain/repository/` | `AIConversationRepository.kt` | 会话仓库接口 |
| | `AIMemoryRepository.kt` | 记忆仓库接口 |
| `domain/usecase/ai/` | `ChatWithAIUseCase.kt` | AI 聊天用例 |
| | `TestAIConnectionUseCase.kt` | 测试连接用例 |
| | `GetAIHistoryUseCase.kt` | 获取历史用例 |
| | `ClearAIHistoryUseCase.kt` | 清除历史用例 |
| `data/repository/` | `AIConversationRepositoryImpl.kt` | 会话仓库实现 |
| | `AIMemoryRepositoryImpl.kt` | 记忆仓库实现 |
| | `AIRepositoryImpl.kt` | AI 配置仓库实现 |
| | `LLMProviderFactoryImpl.kt` | LLM 提供者工厂 |
| `data/mapper/` | `AIMappers.kt` | AI 相关映射器 |
| `ui/screens/ai/` | `AIChatScreen.kt` | AI 聊天页面 |
| | `AIChatViewModel.kt` | 聊天 ViewModel |
| | `AIChatUiState.kt` | 聊天 UI 状态 |
| | `AISettingsScreen.kt` | AI 设置页面 |
| | `AISettingsViewModel.kt` | 设置 ViewModel |
| | `AISettingsUiState.kt` | 设置 UI 状态 |
| | `ConversationHistoryScreen.kt` | 会话历史页面 |
| | `ConversationHistoryViewModel.kt` | 历史 ViewModel |
| | `MemoryManagementScreen.kt` | 记忆管理页面 |
| | `MemoryManagementViewModel.kt` | 记忆 ViewModel |

#### 数据库迁移
- 版本 2 → 3：新增 `ai_conversations` 和 `ai_memories` 表

#### 导航路由
- `ai/chat` - AI 聊天页面
- `ai/settings` - AI 设置页面
- `ai/history` - 会话历史页面
- `ai/memory` - 记忆管理页面

---

### v1.3.0 (2026-01-26)
**功能增强：代码清理 + 数据导入导出 + 标签系统 + 搜索功能**

#### Phase 1: 代码清理
- 删除 `GetActivityHeatMapUseCase.kt` - 活跃热力图功能未被使用
- 清理 `StatisticsChartNew.kt` 中的 ActivityHeatMap、HeatMapCell、getActivityColor 函数
- 清理 `StatisticsViewModel.kt` 中的 getActivityHeatMapUseCase 注入和相关方法

#### Phase 2: 数据导入/导出功能
- 新增 `ExportData.kt` - 导出数据模型
- 新增 `ExportDataUseCase.kt` - 导出数据用例
- 新增 `ImportDataUseCase.kt` - 导入数据用例
- DAO 添加 `getAllDiariesOnce()`, `getAllFlashNotesOnce()`, `getAllTodosOnce()` 方法
- SettingsScreen 添加数据管理区块（导出/导入 JSON）

#### Phase 3: 标签系统
- DiaryDao 添加 `getAllTagsRaw()`, `getDiariesByTag()` 方法
- DiaryRepository 添加标签相关方法
- 新增 `GetAllTagsUseCase.kt`, `GetDiariesByTagUseCase.kt`
- 新增 `TagChip.kt`, `TagSelector.kt` 组件
- 新增 `TagsScreen.kt`, `TagDiariesScreen.kt`, `TagsViewModel.kt`
- DiaryEditScreen 集成标签选择器
- 添加 Tags, TagDiaries 导航路由

#### Phase 4: 搜索功能
- 新增 `SearchScreen.kt`, `SearchViewModel.kt`, `SearchUiState.kt`
- HomeScreen 添加搜索图标入口
- 添加 Search 导航路由
- 支持搜索日记内容和闪念

---

### v1.2.0 (2026-01-26)
**统计页面重新设计**

#### 新增功能
- **总览卡片**
  - 心情指数仪表盘（0-100分，带动画效果）
  - 连续写作天数徽章（带火焰/睡眠 emoji）
  - 日记数、闪念数、已完成待办数统计

- **~~活跃热力图~~** (已在 v1.3.0 删除)
  - ~~类似 GitHub 贡献图的12周活跃记录~~
  - ~~功能实现但未被 UI 使用，已清理~~

- **7天心情趋势图**
  - 折线图展示最近7天心情变化
  - 显示每日心情 emoji
  - 带动画效果的图表绘制

- **月度统计**
  - 月份选择器（前/后月切换）
  - 记录数、字数、写作天数统计
  - 心情分布条形图

#### Bug 修复
- 修复心情指数卡片和连续天数卡片大小不一致问题（统一为 80dp）
- 修复活跃热力图布局（改为横排周一到周日，竖排显示周数）

#### 新增文件
| 文件 | 描述 |
|------|------|
| `GetMoodTrendUseCase.kt` | 获取心情趋势数据 |
| `GetOverviewStatisticsUseCase.kt` | 获取总览统计数据 |
| `StatisticsChartNew.kt` | 新的统计图表组件 |

#### 删除文件 (v1.3.0)
| 文件 | 原因 |
|------|------|
| `GetActivityHeatMapUseCase.kt` | 活跃热力图功能未被 UI 使用，已清理 |

#### 修改文件
| 文件 | 修改内容 |
|------|---------|
| `StatisticsScreen.kt` | 完全重构 UI 布局 |
| `StatisticsViewModel.kt` | 集成新的 UseCase，更新 UiState |

---

### v1.1.0 (2026-01-26)
**闪念功能重新设计 + Todo 修复**

#### Bug 修复
- 修复 Todo 点击完成无响应问题
- 修复闪念在编辑日记后消失的问题
- 修复 TodoDao 查询缺少 `isDeleted = 0` 条件

#### 新增功能
- **闪念系统重构**
  - 闪念自动归入当天日记的 entries
  - 底部常驻输入框快速记录
  - 支持 Todo 模式切换（底部输入框）

- **滑动删除**
  - 闪念卡片支持右滑删除
  - 待办卡片支持右滑删除

- **简化待办**
  - 移除待办优先级属性
  - 简化待办创建流程

#### 数据模型变更
- 新增 `DiaryEntry` 模型
- 新增 `DiaryEntryData` 数据库实体
- `DiaryEntity` 添加 `entries` 和 `date` 字段
- 数据库迁移 v1 → v2

---

## 架构说明

### 项目结构
```
app/src/main/java/com/mindtrace/diary/
├── core/
│   ├── ai/                # AI 核心模块
│   │   ├── ChatModels.kt      # 聊天消息模型
│   │   ├── LLMProvider.kt     # LLM 提供者接口
│   │   └── OpenAICompatibleProvider.kt  # OpenAI 兼容实现
│   ├── database/          # Room 数据库
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # 数据库实体
│   │   └── converter/     # 类型转换器
│   ├── datastore/         # DataStore 配置存储
│   └── util/              # 工具类
├── data/
│   ├── mapper/            # 实体映射器
│   └── repository/        # Repository 实现
├── domain/
│   ├── model/             # 领域模型
│   ├── repository/        # Repository 接口
│   └── usecase/           # 用例
│       ├── ai/            # AI 相关用例
│       ├── backup/        # 导入导出用例
│       ├── diary/         # 日记用例
│       ├── flashnote/     # 闪念用例
│       ├── statistics/    # 统计用例
│       ├── tag/           # 标签用例
│       └── todo/          # 待办用例
├── ui/
│   ├── components/        # 可复用 UI 组件
│   ├── navigation/        # 导航配置
│   ├── screens/           # 页面
│   │   ├── ai/            # AI 相关页面
│   │   ├── calendar/      # 日历页面
│   │   ├── diary/         # 日记页面
│   │   ├── flashnote/     # 闪念页面
│   │   ├── history/       # 历史页面
│   │   ├── home/          # 首页
│   │   ├── search/        # 搜索页面
│   │   ├── settings/      # 设置页面
│   │   ├── statistics/    # 统计页面
│   │   ├── tags/          # 标签页面
│   │   └── todo/          # 待办页面
│   └── theme/             # 主题
└── di/                    # Hilt 依赖注入模块
```

### 技术栈
- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **数据库**: Room
- **配置存储**: DataStore
- **网络**: OkHttp + Kotlin Serialization
- **异步**: Kotlin Coroutines + Flow

### 数据流
```
UI (Compose)
    ↓ 用户操作
ViewModel
    ↓ 调用 UseCase
UseCase (业务逻辑)
    ↓ 调用 Repository
Repository
    ↓ 访问数据源
Room Database / Network
```

---

## 开发指南

### 构建项目
```bash
# 设置 Java 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# 构建 Debug APK
./gradlew assembleDebug

# APK 输出位置
app/build/outputs/apk/debug/app-debug.apk
```

### 数据库迁移
当需要修改数据库结构时：
1. 更新 `AppDatabase` 的 version
2. 创建 `Migration` 对象
3. 在 `DatabaseModule` 中注册迁移

示例：
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ...")
    }
}
```

### 添加新页面
1. 创建 `XxxScreen.kt` (Composable)
2. 创建 `XxxViewModel.kt` (ViewModel)
3. 创建 `XxxUiState.kt` (UI 状态)
4. 在 `NavGraph.kt` 中添加路由
5. 创建相关 UseCase

---

## 已知问题

- [x] ~~热力图在数据量大时可能卡顿~~ (已改为固定12周显示，后删除)
- [ ] 心情趋势图在无数据时显示为空

## 待办功能

- [ ] 导出统计报告
- [ ] 自定义统计周期
- [ ] 标签统计
- [ ] AI 自动提取记忆（从日记中）
- [ ] AI 写作建议
- [ ] 多语言支持

---

## 功能模块说明

### AI 伙伴模块

#### 架构设计
```
AIChatScreen
    ↓
AIChatViewModel
    ↓
ChatWithAIUseCase
    ↓
┌─────────────────────────────────────┐
│ AIConversationRepository (会话管理)  │
│ AIMemoryRepository (长期记忆)        │
│ DiaryRepository (日记上下文)         │
│ LLMProviderFactory (API 调用)       │
└─────────────────────────────────────┘
```

#### API 兼容性
支持任何 OpenAI 兼容格式的 API：
- OpenAI
- Azure OpenAI
- Claude API (通过兼容层)
- 本地 Ollama
- 其他兼容服务商

#### 配置存储
AI 配置通过 DataStore 存储：
- `ai_enabled` - 是否启用
- `ai_base_url` - API 地址
- `ai_api_key` - API 密钥
- `ai_model` - 模型名称
- `ai_system_prompt` - 系统提示词

### 数据导入导出模块

#### 导出格式
```json
{
  "version": 1,
  "exportTime": 1706313600000,
  "diaries": [...],
  "flashNotes": [...],
  "todos": [...]
}
```

#### 使用方式
1. 设置 → 数据管理 → 导出数据
2. 选择保存位置
3. 生成 JSON 文件

### 标签系统

#### 数据结构
标签存储在 `DiaryEntity.tags` 字段中，以逗号分隔的字符串形式。

#### 功能
- 编辑日记时添加/删除标签
- 标签管理页面查看所有标签
- 点击标签查看相关日记
- 搜索支持按标签筛选

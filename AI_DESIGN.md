# MindTrace AI 功能设计方案

## 一、市场调研

### 1.1 竞品分析

| 产品 | 核心特点 | 亮点 | 不足 |
|------|---------|------|------|
| **Rosebud** | 对话式日记，AI 引导反思 | 长期记忆、周报洞察、情感分析 | 纯云端，隐私担忧 |
| **Reflectr** | 多 AI 人格（哲学家、乐观者等） | 心情日历、个性化对话风格 | 缺乏深度个性化 |
| **Mindsera** | 认知教练，心智模型 | 框架建议、思维模式分析 | 偏向效率优化，缺乏情感 |
| **Diarly** | 传统日记 + AI 助手 | 语音转文字、图片识别 | AI 功能相对基础 |

### 1.2 差异化定位

MindTrace 的 AI 应该是：
- **精神伙伴** - 不是冷冰冰的分析工具，而是懂你的朋友
- **记忆守护者** - 记录越多越懂你，形成个人知识图谱
- **隐私优先** - 本地优先，用户数据自己掌控

---

## 二、核心功能设计

### 2.1 功能矩阵

```
┌─────────────────────────────────────────────────────────────┐
│                    MindTrace AI 功能                        │
├──────────────┬──────────────┬──────────────┬───────────────┤
│   对话陪伴    │   智能洞察    │   创作辅助    │   记忆整理    │
├──────────────┼──────────────┼──────────────┼───────────────┤
│ • 情绪倾听    │ • 情绪趋势    │ • 写作建议    │ • 自动标签    │
│ • 引导反思    │ • 行为模式    │ • 标题生成    │ • 摘要生成    │
│ • 问题探索    │ • 人格画像    │ • 续写补充    │ • 主题聚类    │
│ • 鼓励支持    │ • 周/月报告   │ • 风格润色    │ • 时间线回顾  │
└──────────────┴──────────────┴──────────────┴───────────────┘
```

### 2.2 用户场景

#### 场景 1：日常对话
```
用户写完日记后...
AI: "看起来今天工作压力不小。上周三你也提到过类似的感受，
    那次你通过散步缓解了。要不要聊聊发生了什么？"
```

#### 场景 2：周期洞察
```
每周日晚上...
AI: "这周你记录了 5 篇日记，我注意到几个有趣的模式：
    📈 周三开始情绪明显好转
    🔄 运动后的日记通常更积极
    💡 你提到了 3 次'想学画画'这个念头

    要不要一起制定个小计划？"
```

#### 场景 3：记忆唤醒
```
用户: "我上次去西湖是什么时候？"
AI: "根据记录，你上次去西湖是 2025 年 4 月 15 日，
    那天你写道'湖边的柳树刚发新芽，心情很平静'。
    那次旅行似乎对你很有意义，要回顾一下那段时间的日记吗？"
```

---

## 三、架构设计

### 3.1 整体架构

```
┌────────────────────────────────────────────────────────────────┐
│                        MindTrace App                           │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    AI Layer (AIManager)                  │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │   │
│  │  │ ChatEngine  │ │InsightEngine│ │MemoryRetriever  │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌───────────────────────────┼─────────────────────────────┐   │
│  │                    Memory Layer                          │   │
│  │  ┌─────────────┐ ┌───────┴───────┐ ┌─────────────────┐  │   │
│  │  │ ShortTerm   │ │  LongTerm     │ │  Semantic       │  │   │
│  │  │ Memory      │ │  Memory       │ │  Index          │  │   │
│  │  │ (对话上下文) │ │  (用户画像)    │ │  (向量检索)     │  │   │
│  │  └─────────────┘ └───────────────┘ └─────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌───────────────────────────┼─────────────────────────────┐   │
│  │                    LLM Provider Layer                    │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────────┐   │   │
│  │  │ Gemini  │ │ Claude  │ │ OpenAI  │ │ Local LLM    │   │   │
│  │  │ (默认)  │ │ (可选)  │ │ (可选)  │ │ (离线备选)   │   │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └──────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
├────────────────────────────────────────────────────────────────┤
│                     Data Layer (Room DB)                       │
│  Diaries │ FlashNotes │ Todos │ AIMemory │ Embeddings          │
└────────────────────────────────────────────────────────────────┘
```

### 3.2 记忆系统设计

#### 三层记忆架构

```kotlin
// 1. 短期记忆 - 当前对话上下文
data class ShortTermMemory(
    val conversationHistory: List<Message>,  // 当前对话历史
    val currentContext: String,               // 当前情境摘要
    val recentEntries: List<String>           // 最近的日记摘要
)

// 2. 长期记忆 - 用户画像
data class LongTermMemory(
    val userProfile: UserProfile,             // 基本信息、偏好
    val emotionalPatterns: List<Pattern>,     // 情绪模式
    val importantEvents: List<Event>,         // 重要事件
    val recurringThemes: List<Theme>,         // 反复出现的主题
    val relationships: List<Person>           // 提到的人物关系
)

// 3. 语义索引 - 用于检索
data class SemanticIndex(
    val entryEmbeddings: Map<String, FloatArray>,  // 日记向量
    val summaryEmbeddings: Map<String, FloatArray> // 摘要向量
)
```

#### 记忆更新机制

```
日记保存
    │
    ├── 实时处理 ─────────────────┐
    │   • 提取关键实体            │
    │   • 识别情绪标签            │
    │   • 生成向量嵌入            │
    │                             ▼
    │                      SemanticIndex
    │
    └── 延迟处理 (后台/夜间) ─────┐
        • 更新用户画像            │
        • 识别新模式              │
        • 生成周期摘要            │
                                  ▼
                           LongTermMemory
```

### 3.3 AI Agent 设计

```kotlin
// AI Agent 接口
interface AIAgent {
    // 核心能力
    suspend fun chat(message: String, context: AIContext): AIResponse
    suspend fun generateInsight(entries: List<Diary>): Insight
    suspend fun summarize(content: String): String

    // 记忆相关
    suspend fun recallRelevant(query: String): List<Memory>
    suspend fun updateMemory(entry: Diary)
}

// AI 上下文 - 每次对话携带
data class AIContext(
    val shortTermMemory: ShortTermMemory,
    val relevantLongTermMemory: List<Memory>,  // 检索得到的相关记忆
    val currentMood: MoodType?,
    val timeContext: TimeContext  // 时间上下文（早/晚/周末等）
)

// AI 响应
data class AIResponse(
    val message: String,
    val suggestedActions: List<Action>?,  // 建议操作
    val relatedMemories: List<Memory>?,   // 关联的过往记忆
    val moodDetected: MoodType?
)
```

---

## 四、技术选型

### 4.1 LLM Provider 策略

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| **Gemini API** | 免费额度大，Android 集成好 | 需联网 | 默认首选 |
| **Claude API** | 理解能力强，温暖 | 付费 | 高级用户 |
| **OpenAI API** | 生态成熟 | 付费 | 可选 |
| **本地 LLM** | 完全隐私，离线可用 | 性能有限 | 隐私敏感场景 |

**推荐策略**：Gemini 为默认，支持用户自带 API Key 切换

### 4.2 向量检索方案

| 方案 | 描述 |
|------|------|
| **SQLite + 自定义** | 简单场景，存储向量在 BLOB 字段 |
| **Qdrant (本地)** | 专业向量数据库，支持 Android |
| **Gemini Embedding API** | 云端生成向量，本地存储检索 |

**推荐**：初期用 SQLite BLOB + 简单余弦相似度，后期可升级

### 4.3 隐私保护设计

```
┌──────────────────────────────────────────────────────────┐
│                    隐私保护层级                          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Level 1: 仅本地                                         │
│  ├── 日记数据永不上传原文                                │
│  ├── AI 调用只传递匿名化摘要                             │
│  └── 向量索引存储在本地                                  │
│                                                          │
│  Level 2: 可选云端                                       │
│  ├── 用户主动选择是否启用云端 AI                         │
│  ├── 数据端到端加密                                      │
│  └── 定期自动清理云端缓存                                │
│                                                          │
│  Level 3: 完全离线                                       │
│  └── 使用本地 LLM (如 Gemma 2B)                          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 五、数据模型扩展

### 5.1 新增数据库表

```kotlin
// AI 记忆表
@Entity(tableName = "ai_memories")
data class AIMemoryEntity(
    @PrimaryKey val id: String,
    val type: String,          // "insight", "pattern", "summary"
    val content: String,
    val relatedEntryIds: List<String>,
    val importance: Float,     // 0-1 重要性评分
    val createdAt: Long,
    val expiresAt: Long?       // 可选过期时间
)

// 向量嵌入表
@Entity(tableName = "embeddings")
data class EmbeddingEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val entryType: String,     // "diary", "flashnote", "summary"
    val vector: ByteArray,     // 向量数据
    val model: String,         // 生成模型
    val createdAt: Long
)

// AI 对话历史表
@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey val id: String,
    val messages: List<AIMessage>,
    val relatedEntryId: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 5.2 用户画像模型

```kotlin
data class UserProfile(
    // 基础信息
    val writingStyle: WritingStyle,        // 写作风格偏好
    val activeTimeSlots: List<TimeSlot>,   // 活跃时间段
    val preferredTopics: List<String>,     // 常写主题

    // 情感模式
    val emotionalBaseline: Float,          // 情绪基线
    val moodTriggers: Map<String, MoodType>, // 情绪触发因素
    val copingStrategies: List<String>,    // 应对策略

    // 人际关系图谱
    val mentionedPeople: List<Person>,
    val relationshipDynamics: Map<String, String>,

    // 目标与愿望
    val recurringGoals: List<String>,
    val unfullfilledWishes: List<String>
)
```

---

## 六、实施计划

### Phase 1: 基础 AI 对话 (MVP)

**目标**：实现基本的 AI 对话功能

**功能**：
- [ ] 集成 Gemini API
- [ ] 日记写完后的 AI 对话入口
- [ ] 简单的上下文记忆（当前日记 + 最近 3 篇）
- [ ] 情绪识别和回应

**文件**：
```
domain/
  └── usecase/ai/
      ├── ChatWithAIUseCase.kt
      └── AnalyzeMoodUseCase.kt
data/
  └── ai/
      ├── GeminiProvider.kt
      └── AIRepository.kt
ui/
  └── screens/ai/
      ├── AIChatScreen.kt
      └── AIChatViewModel.kt
```

### Phase 2: 智能洞察

**目标**：提供周期性洞察和模式识别

**功能**：
- [ ] 周报/月报生成
- [ ] 情绪趋势分析
- [ ] 行为模式识别
- [ ] 重要事件时间线

### Phase 3: 长期记忆

**目标**：建立用户画像和语义检索

**功能**：
- [ ] 向量嵌入存储
- [ ] 语义搜索（"我上次去杭州是什么时候"）
- [ ] 用户画像持续更新
- [ ] 跨时间关联提醒

### Phase 4: 多模型支持

**目标**：支持多 LLM 和本地模型

**功能**：
- [ ] Claude/OpenAI 可选
- [ ] 本地 LLM 备选 (Gemma)
- [ ] 自定义 AI 人格
- [ ] API Key 管理

---

## 七、UI/UX 设计

### 7.1 AI 入口位置

```
┌─────────────────────────────────────┐
│  日记详情页                         │
│  ─────────────────────────────────  │
│                                     │
│  [日记内容...]                      │
│                                     │
│  ─────────────────────────────────  │
│                                     │
│  💬 和 AI 聊聊这篇日记              │  ← 主入口
│                                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  首页 TopAppBar                     │
│  ─────────────────────────────────  │
│  时间线            🔍  🤖           │  ← AI 快捷入口
└─────────────────────────────────────┘
```

### 7.2 对话界面

```
┌─────────────────────────────────────┐
│  ← AI 伙伴                    •••   │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🤖 我注意到你今天写了不少   │   │
│  │    关于工作压力的内容。     │   │
│  │    要聊聊发生了什么吗？     │   │
│  └─────────────────────────────┘   │
│                                     │
│          ┌─────────────────────┐   │
│          │ 是的，今天开会时... │   │
│          └─────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🤖 我理解这种感受。上个月  │   │
│  │    15 号你也提到过类似的   │   │
│  │    情况，那次你选择了...   │   │
│  │                             │   │
│  │  📎 查看相关日记            │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │ 输入消息...           📤   │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 八、风险与应对

| 风险 | 影响 | 应对策略 |
|------|------|---------|
| API 成本过高 | 用户流失 | 免费额度 + 本地模型降级 |
| 隐私泄露担忧 | 信任危机 | 默认本地，可选云端，透明说明 |
| AI 回应不当 | 用户不适 | 温和的 system prompt + 内容过滤 |
| 响应延迟 | 体验差 | 流式输出 + 加载动画 + 预取 |

---

## 九、参考资源

### 研究论文
- [MIRIX: Multi-Agent Memory System](https://arxiv.org/html/2507.07957v1)
- [Evaluating Long-Term Conversational Memory](https://snap-research.github.io/locomo/)
- [Towards Ethical Personal AI](https://arxiv.org/html/2409.11192v1)

### 产品参考
- [Rosebud](https://www.rosebud.app/)
- [Reflectr](https://play.google.com/store/apps/details?id=net.dailylabs.daily_app)
- [Mindsera](https://www.mindsera.com/)

### 技术文档
- [Gemini API for Android](https://ai.google.dev/)
- [Firebase AI Logic](https://firebase.google.com/docs/ai-logic/chat)
- [LangGraph Long Memory](https://github.com/FareedKhan-dev/langgraph-long-memory)

---

## 十、下一步行动

1. **确认 MVP 范围** - Phase 1 的具体功能列表
2. **设计 AI Prompt** - 定义 AI 伙伴的人格和对话风格
3. **申请 API Key** - Gemini API 密钥
4. **开始实现** - 从 ChatWithAIUseCase 开始

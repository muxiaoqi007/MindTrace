package com.mindtrace.diary.domain.usecase.ai

import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.Priority
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class BuildAIContextUseCase @Inject constructor(
    private val memoryRepository: AIMemoryRepository,
    private val diaryRepository: DiaryRepository,
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(query: String): String {
        val parts = mutableListOf<String>()

        val memorySummary = buildRelevantMemorySummary(query)
        if (memorySummary.isNotBlank()) parts.add(memorySummary)

        val diarySummary = buildDiaryContextSummary()
        if (diarySummary.isNotBlank()) parts.add(diarySummary)

        val todoSummary = buildTodoContextSummary()
        if (todoSummary.isNotBlank()) parts.add(todoSummary)

        return parts.joinToString("\n\n")
    }

    private suspend fun buildRelevantMemorySummary(query: String): String {
        val allMemories = memoryRepository.getAllActiveMemories().first()
        if (allMemories.isEmpty()) return ""

        val keywords = extractKeywords(query)
        val relevant = if (keywords.isEmpty()) {
            emptyList()
        } else {
            allMemories.filter { memory ->
                keywords.any { keyword -> memory.content.contains(keyword, ignoreCase = true) }
            }
        }

        val selected = (relevant + allMemories.sortedByDescending { it.importance })
            .distinctBy { it.id }
            .groupBy { it.category }
            .flatMap { (_, memories) -> memories.take(3) }
            .sortedByDescending { it.importance }
            .take(12)

        if (selected.isEmpty()) return ""

        val grouped = selected.groupBy { it.category }
        return buildString {
            append("关于用户的相关长期记忆：\n")
            MemoryCategory.entries.forEach { category ->
                val memories = grouped[category].orEmpty()
                if (memories.isNotEmpty()) {
                    append("【${category.displayName}】")
                    append(memories.joinToString("；") { it.content })
                    append('\n')
                }
            }
        }.trim()
    }

    private suspend fun buildDiaryContextSummary(): String {
        return try {
            val recentDiaries = diaryRepository.getDiariesPaged(5, 0).first()
            if (recentDiaries.isEmpty()) return ""

            val today = LocalDate.now()
            val summaries = recentDiaries.map { diary ->
                val diaryDate = diary.date ?: diary.createdAt.toLocalDate()
                val dateLabel = formatRelativeDate(diaryDate, today)
                val mood = diary.mood?.let { "心情: ${it.name}" }.orEmpty()
                val tags = if (diary.tags.isNotEmpty()) "标签: ${diary.tags.joinToString(", ")}" else ""
                val preview = diary.content.take(100).replace("\n", " ")

                buildString {
                    append("[$dateLabel] ")
                    if (mood.isNotBlank()) append("$mood ")
                    if (tags.isNotBlank()) append("$tags ")
                    append("- $preview")
                    if (diary.content.length > 100) append("...")
                }
            }
            "最近日记摘要：\n${summaries.joinToString("\n")}"
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun buildTodoContextSummary(): String {
        return try {
            val pendingTodos = todoRepository.getPendingTodos().first()
            if (pendingTodos.isEmpty()) return ""

            val today = LocalDate.now()
            val summaries = pendingTodos.take(10).map { todo ->
                val dueInfo = todo.dueDate?.let { dueDateTime ->
                    val dueDate = dueDateTime.toLocalDate()
                    val dateLabel = formatRelativeDate(dueDate, today)
                    val isOverdue = dueDate.isBefore(today)
                    if (isOverdue) "（已过期: $dateLabel）" else "（截止: $dateLabel）"
                }.orEmpty()
                val priorityLabel = when (todo.priority) {
                    Priority.HIGH -> "【高优先级】"
                    Priority.MEDIUM -> ""
                    Priority.LOW -> "【低优先级】"
                }
                "$priorityLabel${todo.content}$dueInfo"
            }
            "用户的待办事项（未完成）：\n${summaries.joinToString("\n• ", prefix = "• ")}"
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractKeywords(query: String): List<String> {
        return query
            .split(Regex("[\\s，。！？、；：,.!?;:()（）《》\"'「」]+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
            .take(12)
    }

    private fun formatRelativeDate(date: LocalDate, today: LocalDate): String {
        val daysDiff = ChronoUnit.DAYS.between(date, today)
        return when {
            daysDiff == 0L -> "今天"
            daysDiff == 1L -> "昨天"
            daysDiff == 2L -> "前天"
            daysDiff in 3..6 -> "${daysDiff}天前"
            else -> date.format(DateTimeFormatter.ofPattern("MM月dd日"))
        }
    }
}

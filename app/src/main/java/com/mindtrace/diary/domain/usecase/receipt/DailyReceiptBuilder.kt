package com.mindtrace.diary.domain.usecase.receipt

import com.mindtrace.diary.domain.model.DailyReceipt
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.Todo
import java.time.LocalDate
import java.util.Locale

object DailyReceiptBuilder {
    fun build(
        date: LocalDate,
        diaries: List<Diary>,
        flashNotes: List<FlashNote>,
        todos: List<Todo>
    ): DailyReceipt {
        val dailyDiaries = diaries
            .filterNot(Diary::isDeleted)
            .filterNot(Diary::excludeFromResurfacing)
            .filter { diary -> (diary.date ?: diary.createdAt.toLocalDate()) == date }
            .sortedBy(Diary::createdAt)
        val dailyFlashNotes = flashNotes
            .filterNot(FlashNote::isDeleted)
            .filterNot(FlashNote::excludeFromResurfacing)
            .filter { note -> note.createdAt.toLocalDate() == date }
            .sortedBy(FlashNote::createdAt)
        val activeTodos = todos.filterNot(Todo::isDeleted)
        val completedTodoCount = activeTodos.count { todo ->
            todo.isCompleted && todo.completedAt?.toLocalDate() == date
        }
        val pendingTodoCount = activeTodos.count { todo ->
            !todo.isCompleted && (
                todo.dueDate?.toLocalDate() == date ||
                    (todo.dueDate == null && todo.createdAt.toLocalDate() == date)
                )
        }

        return DailyReceipt(
            date = date,
            mood = dailyDiaries.lastOrNull { it.mood != null }?.mood,
            diaryCount = dailyDiaries.size,
            flashNoteCount = dailyFlashNotes.size,
            completedTodoCount = completedTodoCount,
            pendingTodoCount = pendingTodoCount,
            wordCount = dailyDiaries.sumOf { diary -> diary.content.count { !it.isWhitespace() } },
            highlight = selectHighlight(dailyDiaries, dailyFlashNotes),
            keywords = rankKeywords(dailyDiaries)
        )
    }

    private fun selectHighlight(
        diaries: List<Diary>,
        flashNotes: List<FlashNote>
    ): String? {
        val raw = flashNotes.asReversed()
            .firstNotNullOfOrNull { note -> note.content.takeIf(String::isNotBlank) }
            ?: diaries.asReversed().firstNotNullOfOrNull { diary ->
                diary.summary?.takeIf(String::isNotBlank)
                    ?: diary.content.takeIf(String::isNotBlank)
            }
        return raw
            ?.replace(WHITESPACE, " ")
            ?.trim()
            ?.take(MAX_HIGHLIGHT_LENGTH)
            ?.takeIf(String::isNotEmpty)
    }

    private fun rankKeywords(diaries: List<Diary>): List<String> {
        data class KeywordScore(val display: String, var count: Int, val order: Int)

        val scores = linkedMapOf<String, KeywordScore>()
        var order = 0
        diaries.forEach { diary ->
            (diary.tags + diary.aiTags).forEach tagLoop@{ rawTag ->
                val display = rawTag.trim()
                if (display.isEmpty()) return@tagLoop
                val key = display.lowercase(Locale.ROOT)
                val existing = scores[key]
                if (existing == null) {
                    scores[key] = KeywordScore(display = display, count = 1, order = order++)
                } else {
                    existing.count++
                }
            }
        }
        return scores.values
            .sortedWith(compareByDescending<KeywordScore> { it.count }.thenBy { it.order })
            .take(MAX_KEYWORDS)
            .map(KeywordScore::display)
    }

    private val WHITESPACE = Regex("\\s+")
    private const val MAX_HIGHLIGHT_LENGTH = 80
    private const val MAX_KEYWORDS = 5
}

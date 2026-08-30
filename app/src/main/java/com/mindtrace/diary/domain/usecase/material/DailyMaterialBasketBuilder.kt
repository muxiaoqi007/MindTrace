package com.mindtrace.diary.domain.usecase.material

import com.mindtrace.diary.domain.model.DailyMaterial
import com.mindtrace.diary.domain.model.DailyMaterialBasket
import com.mindtrace.diary.domain.model.DailyMaterialType
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.Todo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DailyMaterialBasketBuilder {
    fun build(
        date: LocalDate,
        diaries: List<Diary>,
        flashNotes: List<FlashNote>,
        todos: List<Todo>
    ): DailyMaterialBasket {
        val items = buildList {
            diaries.asSequence()
                .filterNot { it.isDeleted || it.excludeFromResurfacing }
                .filter { (it.date ?: it.createdAt.toLocalDate()) == date }
                .filter { it.content.isNotBlank() }
                .mapTo(this) { diary ->
                    DailyMaterial(
                        sourceId = diary.id,
                        type = DailyMaterialType.DIARY,
                        title = diary.title.trim().takeIf(String::isNotEmpty),
                        text = diary.content.clean(),
                        timestamp = diary.createdAt
                    )
                }

            flashNotes.asSequence()
                .filterNot { it.isDeleted || it.excludeFromResurfacing }
                .filter { it.createdAt.toLocalDate() == date && it.content.isNotBlank() }
                .mapTo(this) { note ->
                    DailyMaterial(
                        sourceId = note.id,
                        type = DailyMaterialType.FLASH_NOTE,
                        text = note.content.clean(),
                        timestamp = note.createdAt
                    )
                }

            todos.asSequence()
                .filterNot(Todo::isDeleted)
                .filter { todo ->
                    if (todo.isCompleted) {
                        todo.completedAt?.toLocalDate() == date
                    } else {
                        todo.dueDate?.toLocalDate() == date ||
                            (todo.dueDate == null && todo.createdAt.toLocalDate() == date)
                    }
                }
                .mapTo(this) { todo ->
                    DailyMaterial(
                        sourceId = todo.id,
                        type = if (todo.isCompleted) {
                            DailyMaterialType.COMPLETED_TODO
                        } else {
                            DailyMaterialType.PENDING_TODO
                        },
                        text = todo.content.clean(),
                        timestamp = todo.completedAt ?: todo.dueDate ?: todo.createdAt
                    )
                }
        }.sortedWith(compareBy(DailyMaterial::timestamp).thenBy(DailyMaterial::key))

        return DailyMaterialBasket(date = date, items = items)
    }

    fun defaultSelection(items: List<DailyMaterial>): Set<String> {
        val fragments = items.filterNot { it.type == DailyMaterialType.DIARY }
        return (fragments.ifEmpty { items }).mapTo(linkedSetOf(), DailyMaterial::key)
    }

    fun composeDraft(date: LocalDate, selected: List<DailyMaterial>): String {
        if (selected.isEmpty()) return ""
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
        return buildString {
            append(date.format(DateTimeFormatter.ofPattern("M月d日的生活素材", Locale.CHINA)))
            append("\n\n")
            selected.sortedBy(DailyMaterial::timestamp).forEach { item ->
                append("[")
                append(item.timestamp.format(formatter))
                append(" ")
                append(item.type.label)
                append("] ")
                item.title?.let { append(it).append("\n") }
                append(item.text)
                append("\n\n")
            }
        }.trim()
    }

    private fun String.clean(): String = replace(WHITESPACE, " ").trim()
    private val WHITESPACE = Regex("\\s+")
}

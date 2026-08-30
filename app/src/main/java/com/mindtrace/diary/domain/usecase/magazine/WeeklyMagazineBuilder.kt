package com.mindtrace.diary.domain.usecase.magazine

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.Todo
import com.mindtrace.diary.domain.model.WeeklyMagazine
import com.mindtrace.diary.domain.model.WeeklyMoodDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object WeeklyMagazineBuilder {
    fun build(anchor: LocalDate, diaries: List<Diary>, flashNotes: List<FlashNote>, todos: List<Todo>): WeeklyMagazine {
        val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = start.plusDays(6)
        val weekDiaries = diaries.filterNot { it.isDeleted || it.excludeFromResurfacing }
            .filter { (it.date ?: it.createdAt.toLocalDate()) in start..end }
            .sortedBy(Diary::createdAt)
        val weekNotes = flashNotes.filterNot { it.isDeleted || it.excludeFromResurfacing }
            .filter { it.createdAt.toLocalDate() in start..end }
            .sortedBy(FlashNote::createdAt)
        val activeTodos = todos.filterNot(Todo::isDeleted)

        val moodStrip = (0L..6L).map { offset ->
            val date = start.plusDays(offset)
            val scores = weekDiaries.filter { (it.date ?: it.createdAt.toLocalDate()) == date }.mapNotNull { it.mood?.score }
            WeeklyMoodDay(date, scores.takeIf { it.isNotEmpty() }?.average()?.toFloat())
        }
        val topMoments = weekDiaries.asReversed().mapNotNull { diary ->
            (diary.summary?.takeIf(String::isNotBlank) ?: diary.content.takeIf(String::isNotBlank))?.clean()?.take(120)
        }.distinct().take(5)
        val flashes = weekNotes.asReversed().map { it.content.clean().take(90) }.filter(String::isNotEmpty).distinct().take(6)
        val completed = activeTodos.filter { todo ->
            todo.isCompleted && todo.completedAt?.toLocalDate()?.let { it in start..end } == true
        }
            .map(Todo::content).filter(String::isNotBlank).distinct().take(8)
        val unresolved = activeTodos.filter { !it.isCompleted && (it.dueDate?.toLocalDate()?.let { date -> !date.isAfter(end) } == true) }
            .map(Todo::content).filter(String::isNotBlank).distinct().take(8)

        return WeeklyMagazine(start, end, moodStrip, topMoments, flashes, completed, unresolved, weekDiaries.size, weekNotes.size)
    }

    private fun String.clean() = replace(Regex("\\s+"), " ").trim()
}

package com.mindtrace.diary.domain.usecase.material

import com.mindtrace.diary.domain.model.DailyMaterialType
import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.FlashNote
import com.mindtrace.diary.domain.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyMaterialBasketBuilderTest {
    private val date = LocalDate.of(2026, 8, 23)

    @Test
    fun collectsAndChronologicallySortsDailySources() {
        val basket = DailyMaterialBasketBuilder.build(
            date = date,
            diaries = listOf(diary("d", "晚上的日记", 20)),
            flashNotes = listOf(flash("f", "早上的想法", 8)),
            todos = listOf(
                Todo(
                    id = "t",
                    content = "跑步",
                    isCompleted = true,
                    createdAt = date.minusDays(1).atStartOfDay(),
                    completedAt = date.atTime(18, 0)
                )
            )
        )

        assertEquals(
            listOf(DailyMaterialType.FLASH_NOTE, DailyMaterialType.COMPLETED_TODO, DailyMaterialType.DIARY),
            basket.items.map { it.type }
        )
    }

    @Test
    fun keepsPrivateSourcesOutOfBasket() {
        val basket = DailyMaterialBasketBuilder.build(
            date = date,
            diaries = listOf(diary("d", "不应出现", 9, excluded = true)),
            flashNotes = listOf(flash("f", "不应出现", 10, excluded = true)),
            todos = emptyList()
        )

        assertTrue(basket.items.isEmpty())
    }

    @Test
    fun defaultsToFragmentsInsteadOfDuplicatingExistingDiary() {
        val basket = DailyMaterialBasketBuilder.build(
            date = date,
            diaries = listOf(diary("d", "日记", 9)),
            flashNotes = listOf(flash("f", "闪念", 10)),
            todos = emptyList()
        )

        val selection = DailyMaterialBasketBuilder.defaultSelection(basket.items)

        assertFalse("DIARY:d" in selection)
        assertTrue("FLASH_NOTE:f" in selection)
    }

    @Test
    fun composesOnlySelectedMaterialsWithoutInventingText() {
        val basket = DailyMaterialBasketBuilder.build(
            date = date,
            diaries = emptyList(),
            flashNotes = listOf(flash("one", "真实文本", 10), flash("two", "不选这条", 11)),
            todos = emptyList()
        )

        val draft = DailyMaterialBasketBuilder.composeDraft(date, listOf(basket.items.first()))

        assertTrue(draft.contains("真实文本"))
        assertFalse(draft.contains("不选这条"))
    }

    private fun diary(id: String, content: String, hour: Int, excluded: Boolean = false) = Diary(
        id = id,
        title = "",
        content = content,
        date = date,
        createdAt = date.atTime(hour, 0),
        updatedAt = date.atTime(hour, 0),
        excludeFromResurfacing = excluded
    )

    private fun flash(id: String, content: String, hour: Int, excluded: Boolean = false) = FlashNote(
        id = id,
        content = content,
        createdAt = date.atTime(hour, 0),
        updatedAt = date.atTime(hour, 0),
        excludeFromResurfacing = excluded
    )
}

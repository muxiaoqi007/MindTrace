package com.mindtrace.diary.domain.usecase.lexicon

import com.mindtrace.diary.domain.model.Diary
import com.mindtrace.diary.domain.model.LexiconType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PersonalLexiconExtractorTest {
    private val date = LocalDate.of(2026, 8, 1)

    @Test fun derivesExplicitPeoplePlacesPhrasesAndWishesWithEvidence() {
        val diary = diary(tags = listOf("人物:阿青", "词语:慢一点"), location = "西湖", content = "我希望明年去看海。")
        val values = PersonalLexiconExtractor.extract(listOf(diary))
        assertEquals(setOf(LexiconType.PERSON, LexiconType.PLACE, LexiconType.PHRASE, LexiconType.WISH), values.map { it.type }.toSet())
        assertTrue(values.all { it.evidence.single().diaryId == "d" })
    }

    @Test fun excludesPrivateAndSuppressedEntries() {
        val private = diary(tags = listOf("人物:阿青"), hidden = true)
        assertTrue(PersonalLexiconExtractor.extract(listOf(private)).isEmpty())
        val public = diary(tags = listOf("人物:阿青"))
        assertTrue(PersonalLexiconExtractor.extract(listOf(public), setOf("PERSON:阿青")).isEmpty())
    }

    private fun diary(tags: List<String> = emptyList(), location: String? = null, content: String = "记录", hidden: Boolean = false) = Diary(
        id = "d", title = "", content = content, tags = tags, location = location, date = date,
        createdAt = date.atStartOfDay(), updatedAt = date.atStartOfDay(), excludeFromResurfacing = hidden
    )
}

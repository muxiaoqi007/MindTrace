package com.mindtrace.diary.core.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AITextGroundingTest {

    @Test
    fun acceptsEvidenceDespiteWhitespaceAndPunctuationDifferences() {
        val source = "我最近决定：长期学习画画，也会每周练习。"

        assertTrue(AITextGrounding.isEvidenceSupported("长期学习画画", source))
        assertTrue(AITextGrounding.isEvidenceSupported("每周 练习", source))
    }

    @Test
    fun rejectsMissingOrInventedEvidence() {
        val source = "今天去公园散步。"

        assertFalse(AITextGrounding.isEvidenceSupported(null, source))
        assertFalse(AITextGrounding.isEvidenceSupported("用户计划明年创业", source))
    }

    @Test
    fun detectsNormalizedAndContainedDuplicates() {
        assertTrue(AITextGrounding.isLikelyDuplicate("喜欢喝咖啡。", "用户喜欢喝咖啡"))
        assertTrue(AITextGrounding.isLikelyDuplicate("长期学习画画", "学习画画"))
        assertFalse(AITextGrounding.isLikelyDuplicate("喜欢咖啡", "喜欢跑步"))
    }
}

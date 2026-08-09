package com.mindtrace.diary.core.ai

import java.util.Locale

/** Deterministic normalization used for evidence checks and basic deduplication. */
object AITextGrounding {
    fun normalizeForComparison(value: String): String = value
        .lowercase(Locale.ROOT)
        .filter { char -> char.isLetterOrDigit() }

    fun isEvidenceSupported(evidence: String?, source: String): Boolean {
        val normalizedEvidence = evidence?.let(::normalizeForComparison).orEmpty()
        if (normalizedEvidence.length < MIN_EVIDENCE_LENGTH) return false
        return normalizeForComparison(source).contains(normalizedEvidence)
    }

    fun isLikelyDuplicate(candidate: String, existing: String): Boolean {
        val left = normalizeForComparison(candidate)
        val right = normalizeForComparison(existing)
        if (left.isEmpty() || right.isEmpty()) return false
        if (left == right) return true

        val shorter = if (left.length <= right.length) left else right
        val longer = if (left.length > right.length) left else right
        return shorter.length >= MIN_DUPLICATE_SUBSTRING_LENGTH && longer.contains(shorter)
    }

    private const val MIN_EVIDENCE_LENGTH = 2
    private const val MIN_DUPLICATE_SUBSTRING_LENGTH = 4
}

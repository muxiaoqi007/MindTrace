package com.mindtrace.diary.core.ai

/** Extracts the first balanced JSON object while respecting quoted strings. */
object AIJsonExtractor {
    fun extractFirstObject(raw: String): String? {
        val start = raw.indexOf('{')
        if (start < 0) return null

        var depth = 0
        var inString = false
        var escaped = false

        for (index in start until raw.length) {
            val char = raw[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }

            when (char) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return raw.substring(start, index + 1)
                    if (depth < 0) return null
                }
            }
        }
        return null
    }
}

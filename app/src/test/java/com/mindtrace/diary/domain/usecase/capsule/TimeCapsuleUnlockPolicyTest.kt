package com.mindtrace.diary.domain.usecase.capsule

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TimeCapsuleUnlockPolicyTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val now = Instant.parse("2026-08-23T12:00:00Z")
    private val clock = Clock.fixed(now, zone)

    @Test
    fun remainsSealedBeforeExactInstant() {
        assertFalse(TimeCapsuleUnlockPolicy.isUnlocked(now.plusSeconds(1).toEpochMilli(), clock))
    }

    @Test
    fun unlocksAtExactInstantRegardlessOfTimezonePresentation() {
        assertTrue(TimeCapsuleUnlockPolicy.isUnlocked(now.toEpochMilli(), clock))
    }
}

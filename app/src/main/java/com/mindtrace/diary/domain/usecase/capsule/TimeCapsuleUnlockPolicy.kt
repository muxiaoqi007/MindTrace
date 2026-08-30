package com.mindtrace.diary.domain.usecase.capsule

import java.time.Clock
import java.time.Instant

object TimeCapsuleUnlockPolicy {
    fun isUnlocked(unlockAtMillis: Long, clock: Clock = Clock.systemDefaultZone()): Boolean =
        !Instant.ofEpochMilli(clock.millis()).isBefore(Instant.ofEpochMilli(unlockAtMillis))
}

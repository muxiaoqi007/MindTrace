package com.mindtrace.diary.core.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncMergePolicyTest {

    @Test
    fun mergeRetainsUnionAndChoosesNewestRecord() {
        val local = listOf(
            Record("local-only", updatedAt = 1),
            Record("local-newer", updatedAt = 3),
            Record("remote-newer", updatedAt = 1)
        )
        val remote = listOf(
            Record("remote-only", updatedAt = 1),
            Record("local-newer", updatedAt = 2),
            Record("remote-newer", updatedAt = 4)
        )

        val merged = SyncMergePolicy.merge(
            local = local,
            remote = remote,
            id = Record::id,
            updatedAt = Record::updatedAt,
            isDeleted = Record::isDeleted
        ).associateBy(Record::id)

        assertEquals(setOf("local-only", "remote-only", "local-newer", "remote-newer"), merged.keys)
        assertEquals(3, merged.getValue("local-newer").updatedAt)
        assertEquals(4, merged.getValue("remote-newer").updatedAt)
    }

    @Test
    fun equalTimestampPrefersTombstone() {
        val active = Record("same", updatedAt = 5, isDeleted = false)
        val tombstone = active.copy(isDeleted = true)

        val merged = SyncMergePolicy.merge(
            local = listOf(active),
            remote = listOf(tombstone),
            id = Record::id,
            updatedAt = Record::updatedAt,
            isDeleted = Record::isDeleted
        )

        assertEquals(listOf(tombstone), merged)
    }

    @Test
    fun equalActiveRecordsPreferLocalValueDeterministically() {
        val local = Record("same", updatedAt = 5, value = "local")
        val remote = Record("same", updatedAt = 5, value = "remote")

        val merged = SyncMergePolicy.merge(
            local = listOf(local),
            remote = listOf(remote),
            id = Record::id,
            updatedAt = Record::updatedAt,
            isDeleted = Record::isDeleted
        )

        assertEquals(listOf(local), merged)
    }

    private data class Record(
        val id: String,
        val updatedAt: Long,
        val isDeleted: Boolean = false,
        val value: String = ""
    )
}

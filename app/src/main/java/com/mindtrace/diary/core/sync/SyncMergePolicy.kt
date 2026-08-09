package com.mindtrace.diary.core.sync

/**
 * Pure, deterministic conflict resolution for sync snapshots.
 *
 * A snapshot always contains the union of local and remote IDs. The newest
 * record wins; if timestamps are equal, a tombstone wins to avoid resurrecting
 * a deletion. Other ties prefer the local value so repeated merges are stable.
 */
object SyncMergePolicy {
    fun <T> merge(
        local: List<T>,
        remote: List<T>,
        id: (T) -> String,
        updatedAt: (T) -> Long,
        isDeleted: (T) -> Boolean
    ): List<T> {
        val remoteById = remote.associateBy(id)
        val localById = local.associateBy(id)
        val orderedIds = LinkedHashSet<String>().apply {
            local.forEach { add(id(it)) }
            remote.forEach { add(id(it)) }
        }

        return orderedIds.map { recordId ->
            val localRecord = localById[recordId]
            val remoteRecord = remoteById[recordId]
            when {
                localRecord == null -> requireNotNull(remoteRecord)
                remoteRecord == null -> localRecord
                updatedAt(localRecord) > updatedAt(remoteRecord) -> localRecord
                updatedAt(remoteRecord) > updatedAt(localRecord) -> remoteRecord
                isDeleted(remoteRecord) && !isDeleted(localRecord) -> remoteRecord
                else -> localRecord
            }
        }
    }
}

package com.mindtrace.diary.data.repository

import com.mindtrace.diary.core.database.dao.TimeCapsuleDao
import com.mindtrace.diary.core.database.entity.TimeCapsuleEntity
import com.mindtrace.diary.core.security.CryptoManager
import com.mindtrace.diary.domain.model.TimeCapsule
import com.mindtrace.diary.domain.model.TimeCapsuleDraft
import com.mindtrace.diary.domain.model.TimeCapsuleSummary
import com.mindtrace.diary.domain.repository.TimeCapsuleRepository
import com.mindtrace.diary.domain.usecase.capsule.TimeCapsuleUnlockPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeCapsuleRepositoryImpl @Inject constructor(
    private val dao: TimeCapsuleDao,
    private val cryptoManager: CryptoManager
) : TimeCapsuleRepository {
    override fun observeSummaries(): Flow<List<TimeCapsuleSummary>> = dao.observeAll().map { entities ->
        entities.map { entity ->
            TimeCapsuleSummary(
                id = entity.id,
                title = entity.title,
                mediaCount = entity.mediaUris.size,
                unlockAt = entity.unlockAt.toDateTime(),
                createdAt = entity.createdAt.toDateTime(),
                openedAt = entity.openedAt?.toDateTime()
            )
        }
    }

    override suspend fun seal(draft: TimeCapsuleDraft): String {
        require(draft.message.isNotBlank()) { "胶囊内容不能为空" }
        val now = LocalDateTime.now()
        require(draft.unlockAt.isAfter(now)) { "开启时间必须在未来" }
        val id = UUID.randomUUID().toString()
        dao.insert(
            TimeCapsuleEntity(
                id = id,
                title = draft.title.trim().ifEmpty { "给未来的自己" },
                encryptedMessage = cryptoManager.encrypt(draft.message),
                encryptedPrediction = cryptoManager.encrypt(draft.prediction),
                encryptedQuestion = cryptoManager.encrypt(draft.question),
                mediaUris = draft.mediaUris.distinct(),
                unlockAt = draft.unlockAt.toMillis(),
                createdAt = now.toMillis(),
                openedAt = null
            )
        )
        return id
    }

    override suspend fun openIfUnlocked(id: String, clock: Clock): TimeCapsule? {
        val entity = dao.getById(id) ?: return null
        if (!TimeCapsuleUnlockPolicy.isUnlocked(entity.unlockAt, clock)) return null
        if (entity.openedAt == null) dao.markOpened(id, clock.millis())
        return TimeCapsule(
            id = entity.id,
            title = entity.title,
            message = cryptoManager.decrypt(entity.encryptedMessage),
            prediction = cryptoManager.decrypt(entity.encryptedPrediction),
            question = cryptoManager.decrypt(entity.encryptedQuestion),
            mediaUris = entity.mediaUris,
            unlockAt = entity.unlockAt.toDateTime(),
            createdAt = entity.createdAt.toDateTime(),
            openedAt = (entity.openedAt ?: clock.millis()).toDateTime()
        )
    }

    override suspend fun delete(id: String) = dao.delete(id)

    private fun LocalDateTime.toMillis(): Long = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun Long.toDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

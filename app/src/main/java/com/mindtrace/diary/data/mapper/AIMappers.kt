package com.mindtrace.diary.data.mapper

import com.mindtrace.diary.core.database.entity.AIConversationEntity
import com.mindtrace.diary.core.database.entity.AIMemoryCandidateEntity
import com.mindtrace.diary.core.database.entity.AIMemoryEntity
import com.mindtrace.diary.core.database.entity.AIMessageData
import com.mindtrace.diary.domain.model.AIConversation
import com.mindtrace.diary.domain.model.AIMemory
import com.mindtrace.diary.domain.model.AIMemoryCandidate
import com.mindtrace.diary.domain.model.AIMessage
import com.mindtrace.diary.domain.model.MemoryCandidateStatus
import com.mindtrace.diary.domain.model.MemoryCategory
import com.mindtrace.diary.domain.model.MemoryType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ========== AIConversation Mappers ==========

fun AIConversationEntity.toDomain(): AIConversation {
    return AIConversation(
        id = id,
        title = title,
        messages = messages.map { it.toDomain() },
        relatedDiaryId = relatedDiaryId,
        messageCount = messageCount,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault())
    )
}

fun AIConversation.toEntity(): AIConversationEntity {
    return AIConversationEntity(
        id = id,
        title = title,
        messages = messages.map { it.toData() },
        relatedDiaryId = relatedDiaryId,
        messageCount = messageCount,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

fun AIMessageData.toDomain(): AIMessage {
    return AIMessage(
        role = AIMessage.Role.fromString(role),
        content = content,
        timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    )
}

fun AIMessage.toData(): AIMessageData {
    return AIMessageData(
        role = role.name.lowercase(),
        content = content,
        timestamp = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

// ========== AIMemory Mappers ==========

fun AIMemoryEntity.toDomain(): AIMemory {
    return AIMemory(
        id = id,
        type = MemoryType.fromString(type),
        category = MemoryCategory.fromString(category),
        content = content,
        source = source,
        importance = importance,
        isActive = isActive,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault())
    )
}

fun AIMemory.toEntity(): AIMemoryEntity {
    return AIMemoryEntity(
        id = id,
        type = type.name.lowercase(),
        category = category.name.lowercase(),
        content = content,
        source = source,
        importance = importance,
        isActive = isActive,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

// ========== AIMemoryCandidate Mappers ==========

fun AIMemoryCandidateEntity.toDomain(): AIMemoryCandidate {
    return AIMemoryCandidate(
        id = id,
        category = MemoryCategory.fromString(category),
        content = content,
        source = source,
        importance = importance,
        status = MemoryCandidateStatus.fromString(status),
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault()),
        reviewedAt = reviewedAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
    )
}

fun AIMemoryCandidate.toEntity(): AIMemoryCandidateEntity {
    return AIMemoryCandidateEntity(
        id = id,
        category = category.name.lowercase(),
        content = content,
        source = source,
        importance = importance,
        status = status.name.lowercase(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        reviewedAt = reviewedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
}

// ========== AiReview Mappers ==========

fun com.mindtrace.diary.core.database.entity.AiReviewEntity.toDomain(): com.mindtrace.diary.domain.model.AiReview {
    return com.mindtrace.diary.domain.model.AiReview(
        id = id,
        date = java.time.LocalDate.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
        content = content,
        diaryIds = try {
            com.google.gson.Gson().fromJson(diaryIds, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        },
        persona = com.mindtrace.diary.domain.model.MidnightReviewPersona.fromId(persona),
        isRead = isRead,
        createdAt = createdAt,
        type = try {
            com.mindtrace.diary.domain.model.ReviewType.valueOf(type)
        } catch (e: Exception) {
            com.mindtrace.diary.domain.model.ReviewType.MIDNIGHT_REVIEW
        },
        userReply = userReply,
        userReplyAt = userReplyAt
    )
}

fun com.mindtrace.diary.domain.model.AiReview.toEntity(): com.mindtrace.diary.core.database.entity.AiReviewEntity {
    return com.mindtrace.diary.core.database.entity.AiReviewEntity(
        id = id,
        date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        content = content,
        diaryIds = com.google.gson.Gson().toJson(diaryIds),
        persona = persona.id,
        isRead = isRead,
        createdAt = createdAt,
        type = type.name,
        userReply = userReply,
        userReplyAt = userReplyAt
    )
}

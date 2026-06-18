package com.mindtrace.diary.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindtrace.diary.core.database.converter.Converters
import com.mindtrace.diary.core.database.dao.AIConversationDao
import com.mindtrace.diary.core.database.dao.AIMemoryDao
import com.mindtrace.diary.core.database.dao.AiReviewDao
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.MoodDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.core.database.entity.AIConversationEntity
import com.mindtrace.diary.core.database.entity.AIMemoryEntity
import com.mindtrace.diary.core.database.entity.AiReviewEntity
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.MoodEntity
import com.mindtrace.diary.core.database.entity.TodoEntity

@Database(
    entities = [
        DiaryEntity::class,
        FlashNoteEntity::class,
        TodoEntity::class,
        MoodEntity::class,
        AIConversationEntity::class,
        AIMemoryEntity::class,
        AiReviewEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun flashNoteDao(): FlashNoteDao
    abstract fun todoDao(): TodoDao
    abstract fun moodDao(): MoodDao
    abstract fun aiConversationDao(): AIConversationDao
    abstract fun aiMemoryDao(): AIMemoryDao
    abstract fun aiReviewDao(): AiReviewDao

    companion object {
        const val DATABASE_NAME = "mindtrace_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add entries column with default empty JSON array
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN entries TEXT NOT NULL DEFAULT '[]'"
                )
                // Add date column with default 0
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN date INTEGER NOT NULL DEFAULT 0"
                )
                // Update existing diaries to set date from createdAt (start of day)
                database.execSQL(
                    "UPDATE diaries SET date = (createdAt / 86400000) * 86400000"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create AI conversations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_conversations (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        messages TEXT NOT NULL,
                        relatedDiaryId TEXT,
                        messageCount INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)

                // Create AI memories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_memories (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        category TEXT NOT NULL,
                        content TEXT NOT NULL,
                        source TEXT,
                        importance REAL NOT NULL DEFAULT 0.5,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create AI reviews table for midnight review feature
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_reviews (
                        id TEXT NOT NULL PRIMARY KEY,
                        date INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        diaryIds TEXT NOT NULL,
                        persona TEXT NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add AI analysis fields to diaries table
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN summary TEXT DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN sentimentScore REAL DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN aiTags TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add type, userReply, userReplyAt columns to ai_reviews table
                database.execSQL(
                    "ALTER TABLE ai_reviews ADD COLUMN type TEXT NOT NULL DEFAULT 'MIDNIGHT_REVIEW'"
                )
                database.execSQL(
                    "ALTER TABLE ai_reviews ADD COLUMN userReply TEXT DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ai_reviews ADD COLUMN userReplyAt INTEGER DEFAULT NULL"
                )
            }
        }
    }
}

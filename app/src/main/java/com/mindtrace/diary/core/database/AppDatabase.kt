package com.mindtrace.diary.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindtrace.diary.core.database.converter.Converters
import com.mindtrace.diary.core.database.dao.AIConversationDao
import com.mindtrace.diary.core.database.dao.AIMemoryCandidateDao
import com.mindtrace.diary.core.database.dao.AIMemoryDao
import com.mindtrace.diary.core.database.dao.AiReviewDao
import com.mindtrace.diary.core.database.dao.DiaryDao
import com.mindtrace.diary.core.database.dao.FlashNoteDao
import com.mindtrace.diary.core.database.dao.MoodDao
import com.mindtrace.diary.core.database.dao.TodoDao
import com.mindtrace.diary.core.database.dao.LifeFacetDao
import com.mindtrace.diary.core.database.dao.TimeCapsuleDao
import com.mindtrace.diary.core.database.dao.StorylineDao
import com.mindtrace.diary.core.database.dao.LexiconDao
import com.mindtrace.diary.core.database.dao.DailyMediaPickDao
import com.mindtrace.diary.core.database.entity.AIConversationEntity
import com.mindtrace.diary.core.database.entity.AIMemoryCandidateEntity
import com.mindtrace.diary.core.database.entity.AIMemoryEntity
import com.mindtrace.diary.core.database.entity.AiReviewEntity
import com.mindtrace.diary.core.database.entity.DiaryEntity
import com.mindtrace.diary.core.database.entity.FlashNoteEntity
import com.mindtrace.diary.core.database.entity.MoodEntity
import com.mindtrace.diary.core.database.entity.TodoEntity
import com.mindtrace.diary.core.database.entity.LifeFacetEntity
import com.mindtrace.diary.core.database.entity.FacetCheckInEntity
import com.mindtrace.diary.core.database.entity.TimeCapsuleEntity
import com.mindtrace.diary.core.database.entity.StorylineEntity
import com.mindtrace.diary.core.database.entity.StorylineSourceEntity
import com.mindtrace.diary.core.database.entity.LexiconEntryEntity
import com.mindtrace.diary.core.database.entity.LexiconEvidenceEntity
import com.mindtrace.diary.core.database.entity.DailyMediaPickEntity

@Database(
    entities = [
        DiaryEntity::class,
        FlashNoteEntity::class,
        TodoEntity::class,
        MoodEntity::class,
        AIConversationEntity::class,
        AIMemoryEntity::class,
        AIMemoryCandidateEntity::class,
        AiReviewEntity::class,
        LifeFacetEntity::class,
        FacetCheckInEntity::class,
        TimeCapsuleEntity::class,
        StorylineEntity::class,
        StorylineSourceEntity::class,
        LexiconEntryEntity::class,
        LexiconEvidenceEntity::class,
        DailyMediaPickEntity::class
    ],
    version = 16,
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
    abstract fun aiMemoryCandidateDao(): AIMemoryCandidateDao
    abstract fun aiReviewDao(): AiReviewDao
    abstract fun lifeFacetDao(): LifeFacetDao
    abstract fun timeCapsuleDao(): TimeCapsuleDao
    abstract fun storylineDao(): StorylineDao
    abstract fun lexiconDao(): LexiconDao
    abstract fun dailyMediaPickDao(): DailyMediaPickDao

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add contentBlocks column to diaries table
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN contentBlocks TEXT NOT NULL DEFAULT '[]'"
                )
                // Migrate existing content + images into contentBlocks
                // For each diary, create blocks: [Text(content)] + [Image(path) for each image]
                val cursor = database.query("SELECT id, content, images FROM diaries")
                val gson = com.google.gson.Gson()
                try {
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0)
                        val content = cursor.getString(1) ?: ""
                        val imagesJson = cursor.getString(2) ?: "[]"
                        val images = try {
                            gson.fromJson(imagesJson, Array<String>::class.java)?.toList() ?: emptyList()
                        } catch (e: Exception) {
                            emptyList<String>()
                        }
                        val blocks = mutableListOf<Map<String, Any?>>()
                        if (content.isNotEmpty()) {
                            blocks.add(mapOf(
                                "id" to java.util.UUID.randomUUID().toString(),
                                "type" to "TEXT",
                                "text" to content
                            ))
                        }
                        for (imagePath in images) {
                            blocks.add(mapOf(
                                "id" to java.util.UUID.randomUUID().toString(),
                                "type" to "IMAGE",
                                "path" to imagePath
                            ))
                        }
                        val blocksJson = gson.toJson(blocks)
                        database.execSQL(
                            "UPDATE diaries SET contentBlocks = ? WHERE id = ?",
                            arrayOf(blocksJson, id)
                        )
                    }
                } finally {
                    cursor.close()
                }
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_memory_candidates (
                        id TEXT NOT NULL PRIMARY KEY,
                        category TEXT NOT NULL,
                        content TEXT NOT NULL,
                        source TEXT,
                        importance REAL NOT NULL DEFAULT 0.5,
                        status TEXT NOT NULL DEFAULT 'pending',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        reviewedAt INTEGER
                    )
                """)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ai_memory_candidates ADD COLUMN evidence TEXT DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ai_memory_candidates ADD COLUMN reason TEXT DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE ai_memory_candidates ADD COLUMN confidence REAL NOT NULL DEFAULT 0.5"
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN excludeFromAI INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE diaries ADD COLUMN excludeFromResurfacing INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE flash_notes ADD COLUMN excludeFromAI INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE flash_notes ADD COLUMN excludeFromResurfacing INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS life_facets (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        options TEXT NOT NULL,
                        isArchived INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS facet_check_ins (
                        id TEXT NOT NULL PRIMARY KEY,
                        facetId TEXT NOT NULL,
                        option TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )"""
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_facet_check_ins_facetId_date ON facet_check_ins (facetId, date)"
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS time_capsules (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        encryptedMessage TEXT NOT NULL,
                        encryptedPrediction TEXT NOT NULL,
                        encryptedQuestion TEXT NOT NULL,
                        mediaUris TEXT NOT NULL,
                        unlockAt INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        openedAt INTEGER
                    )"""
                )
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS storylines (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    normalizedName TEXT NOT NULL,
                    type TEXT NOT NULL,
                    status TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )""")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_storylines_normalizedName ON storylines (normalizedName)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS storyline_sources (
                    id TEXT NOT NULL PRIMARY KEY,
                    storylineId TEXT NOT NULL,
                    diaryId TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    excerpt TEXT NOT NULL
                )""")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_storyline_sources_storylineId_diaryId ON storyline_sources (storylineId, diaryId)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS lexicon_entries (
                    id TEXT NOT NULL PRIMARY KEY, term TEXT NOT NULL, normalizedTerm TEXT NOT NULL,
                    type TEXT NOT NULL, generatedMeaning TEXT NOT NULL, correctedMeaning TEXT,
                    status TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL
                )""")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_lexicon_entries_type_normalizedTerm ON lexicon_entries (type, normalizedTerm)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS lexicon_evidence (
                    id TEXT NOT NULL PRIMARY KEY, entryId TEXT NOT NULL, diaryId TEXT NOT NULL,
                    date INTEGER NOT NULL, excerpt TEXT NOT NULL
                )""")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_lexicon_evidence_entryId_diaryId ON lexicon_evidence (entryId, diaryId)")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS daily_media_picks (
                    id TEXT NOT NULL PRIMARY KEY, date INTEGER NOT NULL, uri TEXT NOT NULL,
                    type TEXT NOT NULL, createdAt INTEGER NOT NULL
                )""")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_media_picks_date ON daily_media_picks (date)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diaries ADD COLUMN latitude REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE diaries ADD COLUMN longitude REAL DEFAULT NULL")
            }
        }
    }
}

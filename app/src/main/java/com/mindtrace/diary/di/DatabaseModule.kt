package com.mindtrace.diary.di

import android.content.Context
import androidx.room.Room
import com.mindtrace.diary.core.database.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16
            )
            .build()
    }

    @Provides
    fun provideDiaryDao(database: AppDatabase): DiaryDao {
        return database.diaryDao()
    }

    @Provides
    fun provideFlashNoteDao(database: AppDatabase): FlashNoteDao {
        return database.flashNoteDao()
    }

    @Provides
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    fun provideMoodDao(database: AppDatabase): MoodDao {
        return database.moodDao()
    }

    @Provides
    fun provideAIConversationDao(database: AppDatabase): AIConversationDao {
        return database.aiConversationDao()
    }

    @Provides
    fun provideAIMemoryDao(database: AppDatabase): AIMemoryDao {
        return database.aiMemoryDao()
    }

    @Provides
    fun provideAIMemoryCandidateDao(database: AppDatabase): AIMemoryCandidateDao {
        return database.aiMemoryCandidateDao()
    }

    @Provides
    fun provideAiReviewDao(database: AppDatabase): AiReviewDao {
        return database.aiReviewDao()
    }

    @Provides
    fun provideLifeFacetDao(database: AppDatabase): LifeFacetDao = database.lifeFacetDao()

    @Provides
    fun provideTimeCapsuleDao(database: AppDatabase): TimeCapsuleDao = database.timeCapsuleDao()

    @Provides
    fun provideStorylineDao(database: AppDatabase): StorylineDao = database.storylineDao()

    @Provides
    fun provideLexiconDao(database: AppDatabase): LexiconDao = database.lexiconDao()

    @Provides
    fun provideDailyMediaPickDao(database: AppDatabase): DailyMediaPickDao = database.dailyMediaPickDao()
}

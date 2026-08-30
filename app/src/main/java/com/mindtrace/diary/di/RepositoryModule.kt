package com.mindtrace.diary.di

import com.mindtrace.diary.data.repository.AIConversationRepositoryImpl
import com.mindtrace.diary.data.repository.AIMemoryCandidateRepositoryImpl
import com.mindtrace.diary.data.repository.AIMemoryRepositoryImpl
import com.mindtrace.diary.data.repository.AIRepositoryImpl
import com.mindtrace.diary.data.repository.AiReviewRepositoryImpl
import com.mindtrace.diary.data.repository.DiaryRepositoryImpl
import com.mindtrace.diary.data.repository.FlashNoteRepositoryImpl
import com.mindtrace.diary.data.repository.LLMProviderFactory
import com.mindtrace.diary.data.repository.LLMProviderFactoryImpl
import com.mindtrace.diary.data.repository.TodoRepositoryImpl
import com.mindtrace.diary.data.repository.LifeFacetRepositoryImpl
import com.mindtrace.diary.data.repository.TimeCapsuleRepositoryImpl
import com.mindtrace.diary.data.repository.StorylineRepositoryImpl
import com.mindtrace.diary.data.repository.LexiconRepositoryImpl
import com.mindtrace.diary.data.repository.DailyMediaRepositoryImpl
import com.mindtrace.diary.domain.repository.AIConversationRepository
import com.mindtrace.diary.domain.repository.AIMemoryCandidateRepository
import com.mindtrace.diary.domain.repository.AIMemoryRepository
import com.mindtrace.diary.domain.repository.AIRepository
import com.mindtrace.diary.domain.repository.AiReviewRepository
import com.mindtrace.diary.domain.repository.DiaryRepository
import com.mindtrace.diary.domain.repository.FlashNoteRepository
import com.mindtrace.diary.domain.repository.TodoRepository
import com.mindtrace.diary.domain.repository.LifeFacetRepository
import com.mindtrace.diary.domain.repository.TimeCapsuleRepository
import com.mindtrace.diary.domain.repository.StorylineRepository
import com.mindtrace.diary.domain.repository.LexiconRepository
import com.mindtrace.diary.domain.repository.DailyMediaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(
        diaryRepositoryImpl: DiaryRepositoryImpl
    ): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindFlashNoteRepository(
        flashNoteRepositoryImpl: FlashNoteRepositoryImpl
    ): FlashNoteRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(
        todoRepositoryImpl: TodoRepositoryImpl
    ): TodoRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        aiRepositoryImpl: AIRepositoryImpl
    ): AIRepository

    @Binds
    @Singleton
    abstract fun bindLLMProviderFactory(
        llmProviderFactoryImpl: LLMProviderFactoryImpl
    ): LLMProviderFactory

    @Binds
    @Singleton
    abstract fun bindAIConversationRepository(
        aiConversationRepositoryImpl: AIConversationRepositoryImpl
    ): AIConversationRepository

    @Binds
    @Singleton
    abstract fun bindAIMemoryRepository(
        aiMemoryRepositoryImpl: AIMemoryRepositoryImpl
    ): AIMemoryRepository

    @Binds
    @Singleton
    abstract fun bindAIMemoryCandidateRepository(
        aiMemoryCandidateRepositoryImpl: AIMemoryCandidateRepositoryImpl
    ): AIMemoryCandidateRepository

    @Binds
    @Singleton
    abstract fun bindAiReviewRepository(
        aiReviewRepositoryImpl: AiReviewRepositoryImpl
    ): AiReviewRepository

    @Binds
    @Singleton
    abstract fun bindLifeFacetRepository(
        lifeFacetRepositoryImpl: LifeFacetRepositoryImpl
    ): LifeFacetRepository

    @Binds
    @Singleton
    abstract fun bindTimeCapsuleRepository(
        timeCapsuleRepositoryImpl: TimeCapsuleRepositoryImpl
    ): TimeCapsuleRepository

    @Binds
    @Singleton
    abstract fun bindStorylineRepository(
        storylineRepositoryImpl: StorylineRepositoryImpl
    ): StorylineRepository

    @Binds
    @Singleton
    abstract fun bindLexiconRepository(
        lexiconRepositoryImpl: LexiconRepositoryImpl
    ): LexiconRepository

    @Binds
    @Singleton
    abstract fun bindDailyMediaRepository(
        dailyMediaRepositoryImpl: DailyMediaRepositoryImpl
    ): DailyMediaRepository
}

package com.mindtrace.diary.core.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindtrace.diary.core.database.entity.AIMessageData
import com.mindtrace.diary.core.database.entity.DiaryEntryData

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDiaryEntryDataList(value: List<DiaryEntryData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDiaryEntryDataList(value: String): List<DiaryEntryData> {
        val listType = object : TypeToken<List<DiaryEntryData>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAIMessageDataList(value: List<AIMessageData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAIMessageDataList(value: String): List<AIMessageData> {
        val listType = object : TypeToken<List<AIMessageData>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

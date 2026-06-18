package com.mindtrace.diary.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.datastore.ThemeMode
import com.mindtrace.diary.core.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val moodIconPackId: StateFlow<String> = settingsDataStore.moodIconPackId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "minimalist"
        )

    init {
        setupAutoSync()
    }

    private fun setupAutoSync() {
        viewModelScope.launch {
            settingsDataStore.autoSyncEnabled.collect { enabled ->
                // Auto sync will be handled by SyncWorker if enabled
                // This is initialized in the Application class
            }
        }
    }
}

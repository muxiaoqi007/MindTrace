package com.mindtrace.diary.ui.screens.webdav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.datastore.WebDavConfig
import com.mindtrace.diary.core.sync.RestoreResult
import com.mindtrace.diary.core.sync.SyncManager
import com.mindtrace.diary.core.sync.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WebDAVSettingsUiState(
    val config: WebDavConfig = WebDavConfig("", "", "", "/MindTrace"),
    val autoSyncEnabled: Boolean = false,
    val lastSyncTime: Long? = null,
    val isTesting: Boolean = false,
    val isSyncing: Boolean = false,
    val isRestoring: Boolean = false,
    val testResult: Boolean? = null,
    val syncResult: SyncResult? = null,
    val restoreResult: RestoreResult? = null,
    val error: String? = null,
    val showRestoreConfirmDialog: Boolean = false
)

@HiltViewModel
class WebDAVSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebDAVSettingsUiState())
    val uiState: StateFlow<WebDAVSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.webDavConfig,
                settingsDataStore.autoSyncEnabled,
                settingsDataStore.lastSyncTime
            ) { config, autoSync, lastSync ->
                Triple(config, autoSync, lastSync)
            }.collect { (config, autoSync, lastSync) ->
                _uiState.update {
                    it.copy(
                        config = config,
                        autoSyncEnabled = autoSync,
                        lastSyncTime = lastSync
                    )
                }
            }
        }
    }

    fun updateWebDavUrl(url: String) {
        _uiState.update {
            it.copy(config = it.config.copy(url = url), testResult = null)
        }
    }

    fun updateWebDavUsername(username: String) {
        _uiState.update {
            it.copy(config = it.config.copy(username = username), testResult = null)
        }
    }

    fun updateWebDavPassword(password: String) {
        _uiState.update {
            it.copy(config = it.config.copy(password = password), testResult = null)
        }
    }

    fun updateWebDavPath(path: String) {
        _uiState.update {
            it.copy(config = it.config.copy(path = path), testResult = null)
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            settingsDataStore.setWebDavConfig(_uiState.value.config)
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoSyncEnabled(enabled)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            settingsDataStore.setWebDavConfig(_uiState.value.config)
            val result = syncManager.testConnection()
            _uiState.update { it.copy(isTesting = false, testResult = result) }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncResult = null) }
            val result = syncManager.sync()
            _uiState.update { it.copy(isSyncing = false, syncResult = result) }
        }
    }

    fun showRestoreConfirmDialog() {
        _uiState.update { it.copy(showRestoreConfirmDialog = true) }
    }

    fun dismissRestoreConfirmDialog() {
        _uiState.update { it.copy(showRestoreConfirmDialog = false) }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(showRestoreConfirmDialog = false, isRestoring = true) }
            val result = syncManager.restoreFromCloud()
            _uiState.update { it.copy(isRestoring = false, restoreResult = result) }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    fun clearSyncResult() {
        _uiState.update { it.copy(syncResult = null) }
    }

    fun clearRestoreResult() {
        _uiState.update { it.copy(restoreResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

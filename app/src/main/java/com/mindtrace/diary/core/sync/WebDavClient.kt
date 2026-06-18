package com.mindtrace.diary.core.sync

import com.mindtrace.diary.core.datastore.SettingsDataStore
import com.mindtrace.diary.core.datastore.WebDavConfig
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavClient @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private var sardine: OkHttpSardine? = null
    private var currentConfig: WebDavConfig? = null

    private suspend fun getSardine(): OkHttpSardine? {
        val config = settingsDataStore.webDavConfig.first()
        if (!config.isConfigured) return null

        if (sardine == null || currentConfig != config) {
            sardine = OkHttpSardine().apply {
                setCredentials(config.username, config.password)
            }
            currentConfig = config
        }
        return sardine
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            if (!config.isConfigured) return@withContext false

            val client = getSardine() ?: return@withContext false
            val baseUrl = config.url.trimEnd('/')

            // Try to list the root directory
            client.exists(baseUrl)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun ensureDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext false
            val fullPath = "${config.url.trimEnd('/')}${path}"

            if (!client.exists(fullPath)) {
                client.createDirectory(fullPath)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadFile(remotePath: String, data: ByteArray, contentType: String = "application/json"): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val config = settingsDataStore.webDavConfig.first()
                val client = getSardine() ?: return@withContext false
                val fullPath = "${config.url.trimEnd('/')}${config.path}$remotePath"

                client.put(fullPath, data, contentType)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun downloadFile(remotePath: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext null
            val fullPath = "${config.url.trimEnd('/')}${config.path}$remotePath"

            if (!client.exists(fullPath)) return@withContext null

            client.get(fullPath).use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun listFiles(remotePath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext emptyList()
            val fullPath = "${config.url.trimEnd('/')}${config.path}$remotePath"

            if (!client.exists(fullPath)) return@withContext emptyList()

            client.list(fullPath)
                .filter { !it.isDirectory }
                .map { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext false
            val fullPath = "${config.url.trimEnd('/')}${config.path}$remotePath"

            if (client.exists(fullPath)) {
                client.delete(fullPath)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fileExists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext false
            val fullPath = "${config.url.trimEnd('/')}${config.path}$remotePath"

            client.exists(fullPath)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

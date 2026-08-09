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

    suspend fun ensureDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine()
                ?: return@withContext Result.failure(IllegalStateException("WebDAV 未配置"))
            val fullPath = buildRemoteUrl(config, path)

            if (!client.exists(fullPath)) {
                client.createDirectory(fullPath)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(remotePath: String, data: ByteArray, contentType: String = "application/json"): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val config = settingsDataStore.webDavConfig.first()
                val client = getSardine()
                    ?: return@withContext Result.failure(IllegalStateException("WebDAV 未配置"))
                val fullPath = buildRemoteUrl(config, config.path, remotePath)

                client.put(fullPath, data, contentType)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun downloadFile(remotePath: String): RemoteFileResult = withContext(Dispatchers.IO) {
        try {
            val config = settingsDataStore.webDavConfig.first()
            val client = getSardine() ?: return@withContext RemoteFileResult.Failure(
                "WebDAV 未配置",
                IllegalStateException("WebDAV 未配置")
            )
            val fullPath = buildRemoteUrl(config, config.path, remotePath)

            if (!client.exists(fullPath)) return@withContext RemoteFileResult.NotFound

            client.get(fullPath).use { inputStream ->
                RemoteFileResult.Found(inputStream.readBytesLimited(MAX_REMOTE_FILE_BYTES))
            }
        } catch (e: Exception) {
            RemoteFileResult.Failure(e.message ?: "下载失败", e)
        }
    }

    private fun buildRemoteUrl(config: WebDavConfig, vararg paths: String): String {
        val suffix = paths
            .flatMap { path -> path.split('/') }
            .filter { segment -> segment.isNotBlank() }
            .joinToString("/")
        return if (suffix.isBlank()) config.url.trimEnd('/') else "${config.url.trimEnd('/')}/$suffix"
    }

    private fun InputStream.readBytesLimited(maxBytes: Long): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = read(buffer)
            if (read < 0) break
            total += read
            require(total <= maxBytes) { "远端同步文件过大" }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private companion object {
        const val MAX_REMOTE_FILE_BYTES = 50L * 1024 * 1024
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

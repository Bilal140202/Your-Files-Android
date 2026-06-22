package com.yourfiles.manager.utils

import android.content.Context
import android.content.SharedPreferences
import com.yourfiles.manager.app.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject

/**
 * Holds metadata about the current trash contents.
 */
data class TrashInfo(val fileCount: Int, val totalSize: Long)

/**
 * Singleton utility for safely moving deleted files to a trash folder instead of
 * permanently deleting them. Files are stored under the app's external files directory
 * so they survive across app restarts but are cleaned up when the app is uninstalled.
 *
 * Trash location: /Android/data/com.yourfiles.manager/files/Trash/
 *
 * Original paths are persisted in SharedPreferences so restore works across app restarts.
 */
object TrashManager {

    private const val TRASH_DIR_NAME = "Trash"
    private const val TRASH_RETENTION_DAYS = 30L
    private const val PREFS_NAME = "trash_records"
    private const val KEY_RECORDS = "records"

    /** In-memory mapping of originalPath -> trashPath for the most recent trash operation. */
    private val lastTrashedEntries = ConcurrentHashMap<String, String>()

    private val _trashInfo = MutableStateFlow(TrashInfo(fileCount = 0, totalSize = 0L))

    /** Flow that emits the current trash state (count + size) whenever it changes. */
    val trashInfo: StateFlow<TrashInfo> = _trashInfo.asStateFlow()

    // ────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────────────────────

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Persist the full trash record map to SharedPreferences as JSON.
     * Format: {"records": [{"originalPath":"...","trashPath":"...","timestamp":123}, ...]}
     */
    private fun saveRecords(context: Context, records: Map<String, TrashRecord>) {
        val arr = JSONArray()
        for ((trashPath, record) in records) {
            val obj = JSONObject()
            obj.put("originalPath", record.originalPath)
            obj.put("trashPath", record.trashPath)
            obj.put("timestamp", record.timestamp)
            arr.put(obj)
        }
        getPrefs(context).edit().putString(KEY_RECORDS, arr.toString()).apply()
    }

    /**
     * Load all persisted trash records from SharedPreferences.
     * Returns a map keyed by trashPath for easy lookup.
     */
    private fun loadRecords(context: Context): Map<String, TrashRecord> {
        val json = getPrefs(context).getString(KEY_RECORDS, null) ?: return emptyMap()
        val result = mutableMapOf<String, TrashRecord>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val record = TrashRecord(
                    originalPath = obj.getString("originalPath"),
                    trashPath = obj.getString("trashPath"),
                    timestamp = obj.optLong("timestamp", 0L),
                )
                result[record.trashPath] = record
            }
        } catch (_: Exception) {}
        return result
    }

    /**
     * Returns the trash directory, creating it if it does not yet exist.
     */
    private fun getTrashDir(context: Context): File? {
        val baseDir = context.getExternalFilesDir(null) ?: return null
        val trashDir = File(baseDir, TRASH_DIR_NAME)
        if (!trashDir.exists()) {
            val created = trashDir.mkdirs()
            if (!created) return null
        }
        return trashDir
    }

    /**
     * Generates a unique filename inside trash to avoid collisions.
     * Format: `<timestamp_ms>_<originalFileName>`
     */
    private fun uniqueTrashName(file: File): String {
        return "${System.currentTimeMillis()}_${file.name}"
    }

    /**
     * Refreshes the [_trashInfo] state flow with current trash contents.
     */
    private fun refreshTrashInfo() {
        val context = App.instance
        val files = getTrashFiles(context)
        val count = files.size
        val size = files.sumOf { it.length() }
        _trashInfo.value = TrashInfo(fileCount = count, totalSize = size)
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Move files to trash. Original paths are persisted so restore works across restarts.
     *
     * @param filePaths Set of absolute file paths to move to trash.
     * @return Map of original path -> trash path for successfully moved entries.
     */
    suspend fun moveToTrash(filePaths: Set<String>): Map<String, String> {
        val context = App.instance
        val trashDir = getTrashDir(context) ?: return emptyMap()

        // Load existing records and merge new ones
        val existingRecords = loadRecords(context).toMutableMap()
        val result = mutableMapOf<String, String>()
        val now = System.currentTimeMillis()

        for (filePath in filePaths) {
            val source = File(filePath)
            if (!source.exists()) continue

            val trashFile = File(trashDir, uniqueTrashName(source))
            val moved = source.renameTo(trashFile)

            if (moved) {
                result[filePath] = trashFile.absolutePath
                // Persist the record
                existingRecords[trashFile.absolutePath] = TrashRecord(
                    originalPath = filePath,
                    trashPath = trashFile.absolutePath,
                    timestamp = now,
                )
            }
        }

        // Save updated records
        saveRecords(context, existingRecords)

        // Update in-memory undo map
        lastTrashedEntries.clear()
        lastTrashedEntries.putAll(result)

        refreshTrashInfo()
        return result
    }

    /**
     * Restore a single trashed file to its original location using persisted records.
     *
     * @param trashFile the File object in the trash directory
     * @return true if restored successfully
     */
    suspend fun restoreFile(trashFile: File): Boolean {
        val context = App.instance
        val records = loadRecords(context)
        val record = records[trashFile.absolutePath]

        val originalPath = record?.originalPath ?: reconstructOriginalPath(trashFile)
        val originalFile = File(originalPath)

        if (!trashFile.exists()) return false

        // Make sure parent directory exists
        val parentDir = originalFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val restored = trashFile.renameTo(originalFile)
        if (restored) {
            // Remove from persisted records
            val updatedRecords = loadRecords(context).toMutableMap()
            updatedRecords.remove(trashFile.absolutePath)
            saveRecords(context, updatedRecords)
            lastTrashedEntries.remove(originalPath)
            refreshTrashInfo()
        }
        return restored
    }

    /**
     * Undo the last trash operation — move files back to their original locations.
     *
     * @param trashEntries Map of original path -> trash path.
     * @return the number of files that were successfully restored.
     */
    suspend fun undoTrash(trashEntries: Map<String, String>): Int {
        var restoredCount = 0

        for ((originalPath, trashPath) in trashEntries) {
            val trashFile = File(trashPath)
            val originalFile = File(originalPath)

            if (!trashFile.exists()) continue

            val parentDir = originalFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                val dirsCreated = parentDir.mkdirs()
                if (!dirsCreated) continue
            }

            val restored = trashFile.renameTo(originalFile)
            if (restored) {
                restoredCount++
                lastTrashedEntries.remove(originalPath)

                // Also remove from persisted records
                val context = App.instance
                val updatedRecords = loadRecords(context).toMutableMap()
                updatedRecords.remove(trashPath)
                saveRecords(context, updatedRecords)
            }
        }

        refreshTrashInfo()
        return restoredCount
    }

    /**
     * Delete files that have been in the trash for longer than [TRASH_RETENTION_DAYS].
     */
    suspend fun cleanupOldTrash(context: Context): Int {
        val trashDir = getTrashDir(context) ?: return 0
        val cutoffMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(TRASH_RETENTION_DAYS)
        val trashFiles = trashDir.listFiles()
        var deletedCount = 0
        val records = loadRecords(context).toMutableMap()

        if (trashFiles != null) {
            for (file in trashFiles) {
                if (file.lastModified() < cutoffMillis) {
                    val deleted = file.delete()
                    if (deleted) {
                        deletedCount++
                        records.remove(file.absolutePath)
                        lastTrashedEntries.values.remove(file.absolutePath)
                    }
                }
            }
        }

        saveRecords(context, records)
        refreshTrashInfo()
        return deletedCount
    }

    /**
     * List all files currently in the trash directory.
     */
    fun getTrashFiles(context: Context): List<File> {
        val trashDir = getTrashDir(context) ?: return emptyList()
        val files = trashDir.listFiles()
        return files?.toList() ?: emptyList()
    }

    /**
     * Get the persisted original path for a trashed file.
     * Falls back to reconstructing from filename if no record exists.
     */
    fun getOriginalPath(context: Context, trashFile: File): String {
        val records = loadRecords(context)
        return records[trashFile.absolutePath]?.originalPath
            ?: reconstructOriginalPath(trashFile)
    }

    /**
     * Calculate the total size of all files in trash.
     */
    fun getTrashSize(context: Context): Long {
        return getTrashFiles(context).sumOf { it.length() }
    }

    /**
     * Permanently delete every file in the trash directory.
     */
    suspend fun emptyTrash(context: Context): Int {
        val trashDir = getTrashDir(context) ?: return 0
        val trashFiles = trashDir.listFiles()
        var deletedCount = 0

        if (trashFiles != null) {
            for (file in trashFiles) {
                val deleted = file.delete()
                if (deleted) {
                    deletedCount++
                    lastTrashedEntries.values.remove(file.absolutePath)
                }
            }
        }

        lastTrashedEntries.clear()
        // Clear all persisted records
        saveRecords(context, emptyMap())
        refreshTrashInfo()
        return deletedCount
    }

    /**
     * Reconstruct the original path from a trash file name.
     * Trash files are named: `<timestamp_ms>_<originalFileName>`
     * We can only recover the filename, not the full original path.
     */
    private fun reconstructOriginalPath(trashFile: File): String {
        val originalName = trashFile.name.substringAfter("_", trashFile.name)
        // Best-effort: restore to Download folder
        val downloadDir = File(
            android.os.Environment.getExternalStorageDirectory(),
            "Download"
        )
        if (downloadDir.exists()) {
            return File(downloadDir, originalName).absolutePath
        }
        return File(trashFile.parentFile ?: trashFile, originalName).absolutePath
    }
}

/** Persistent record for a trashed file. */
data class TrashRecord(
    val originalPath: String,
    val trashPath: String,
    val timestamp: Long = System.currentTimeMillis(),
)
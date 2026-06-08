package com.yourfiles.manager.domain.model

import java.io.File

/**
 * Represents a file or folder item in the file browser.
 */
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val childCount: Int = 0,
    val mimeType: String? = null,
) {
    val file: File get() = File(path)

    companion object {
        fun fromFile(file: File): FileItem {
            val childCount = if (file.isDirectory) {
                file.listFiles()?.count() ?: 0
            } else 0
            return FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = file.length(),
                lastModified = file.lastModified(),
                childCount = childCount,
            )
        }
    }
}

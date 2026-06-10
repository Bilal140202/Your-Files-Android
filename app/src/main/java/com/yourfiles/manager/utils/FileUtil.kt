package com.yourfiles.manager.utils

import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import com.yourfiles.manager.utils.ImageOptimizer
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest

fun File.readIsOptimised(): Boolean {
    val ext = extension.lowercase()
    if (ext != "jpg" && ext != "jpeg") return false
    return try {
        ExifInterface(absolutePath).getAttribute(ExifInterface.TAG_USER_COMMENT) == ImageOptimizer.EXIF_MARKER
    } catch (_: Exception) {
        false
    }
}

fun getMimeType(path: String): String? {
    // MimeTypeMap.getFileExtensionFromUrl fails on filenames with spaces/parentheses
    // Use File.extension as reliable fallback
    val ext = MimeTypeMap.getFileExtensionFromUrl(path).lowercase().ifEmpty {
        File(path).extension.lowercase()
    }
    if (ext.isEmpty()) return null
    // Android's MimeTypeMap misses many common types — handle explicitly
    return when (ext) {
        "yaml", "yml" -> "text/yaml"
        "md" -> "text/markdown"
        "json" -> "application/json"
        "xml" -> "text/xml"
        "csv" -> "text/csv"
        "log" -> "text/plain"
        "sh", "bash" -> "text/x-shellscript"
        "py" -> "text/x-python"
        "kt", "kts" -> "text/x-kotlin"
        "java" -> "text/x-java"
        "js", "mjs", "cjs" -> "text/javascript"
        "ts" -> "text/typescript"
        "html", "htm" -> "text/html"
        "css", "scss", "less" -> "text/css"
        "sql" -> "text/x-sql"
        "gradle" -> "text/plain"
        "properties" -> "text/plain"
        "toml" -> "text/plain"
        "ini", "cfg" -> "text/plain"
        "rs" -> "text/x-rust"
        "c", "h" -> "text/x-c"
        "cpp", "cc", "cxx", "hpp" -> "text/x-c++"
        "rb" -> "text/x-ruby"
        "go" -> "text/x-go"
        "swift" -> "text/x-swift"
        "dart" -> "text/x-dart"
        "lua" -> "text/x-lua"
        "r" -> "text/x-r"
        "tex" -> "text/x-tex"
        "rtf" -> "application/rtf"
        "apk" -> "application/vnd.android.package-archive"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        "tar", "gz", "bz2", "xz" -> "application/x-tar"
        else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }
}

// return file size in mb
fun File.size(): Long {
    return length() / 1024
}

fun File.md5(): String? {
    if (!exists()) return null
    return try {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(this).use { fis ->
            val buffer = ByteArray(65536)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        val sb = StringBuilder(32)
        for (hashByte in md.digest()) {
            sb.append("%02x".format(hashByte.toInt() and 0xff))
        }
        sb.toString()
    } catch (e: IOException) {
        Log.w("YourFiles", "Could not compute md5 for $absolutePath", e)
        null
    }
}

/**
 * Computes MD5 over a partial read of the file and appends the file size as
 * "<hash>_<sizeInBytes>", so two files with identical partial content but
 * different sizes always produce different checksum strings.
 *
 * Strategy:
 *  - Files ≤ 2 × [bytesFromEachEnd]: entire file is hashed (no edge cases).
 *  - Larger files: first [bytesFromEachEnd] bytes + last [bytesFromEachEnd] bytes.
 *    The tail is reached via FileChannel.position() which is an exact seek,
 *    unlike InputStream.skip() which is not guaranteed to advance the full amount.
 *
 * Reading both head and tail catches files that share a common header but differ
 * at the end (e.g. a PDF with extra pages appended).
 */
fun File.partialMd5(bytesFromEachEnd: Int = 4096): String? {
    if (!exists()) return null
    val fileSize = length()
    return try {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(this).use { fis ->
            if (fileSize <= bytesFromEachEnd * 2L) {
                // Small file: hash everything in one pass
                val buffer = ByteArray(fileSize.toInt())
                val read = fis.read(buffer)
                if (read > 0) md.update(buffer, 0, read)
            } else {
                // Large file: hash head
                val headBuffer = ByteArray(bytesFromEachEnd)
                val headRead = fis.read(headBuffer)
                if (headRead > 0) md.update(headBuffer, 0, headRead)

                // Seek to tail via channel (exact, unlike skip())
                fis.channel.position(fileSize - bytesFromEachEnd)
                val tailBuffer = ByteArray(bytesFromEachEnd)
                val tailRead = fis.read(tailBuffer)
                if (tailRead > 0) md.update(tailBuffer, 0, tailRead)
            }
        }
        val sb = StringBuilder(32)
        for (hashByte in md.digest()) {
            sb.append("%02x".format(hashByte.toInt() and 0xff))
        }
        "${sb}_$fileSize"
    } catch (e: IOException) {
        Log.w("YourFiles", "Could not compute partial md5 for $absolutePath", e)
        null
    }
}

fun isFileImage(mimeType: String?) = mimeType?.contains("image", ignoreCase = true) == true
fun isFileVideo(mimeType: String?) = mimeType?.contains("video", ignoreCase = true) == true
fun isFileText(mimeType: String?) = mimeType?.startsWith("text") == true
fun isFileAudio(mimeType: String?) = mimeType?.startsWith("audio") == true
fun isFilePdf(mimeType: String?) = mimeType == "application/pdf"
fun isFileArchive(mimeType: String?) = mimeType in listOf(
    "application/zip", "application/x-rar-compressed",
    "application/x-7z-compressed", "application/x-tar",
)
fun isFileApk(mimeType: String?) = mimeType == "application/vnd.android.package-archive"
fun isFileOffice(mimeType: String?) = mimeType?.startsWith("application/vnd.openxmlformats-officedocument") == true
    || mimeType in listOf("application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint")
fun isFileCode(path: String): Boolean {
    val ext = path.substringAfterLast('.', "").lowercase()
    return ext in listOf("kt","java","py","js","ts","html","css","sh","bash","sql","go","rs","dart",
        "swift","lua","r","rb","c","cpp","cc","cxx","h","hpp","gradle","toml","yaml","yml","json",
        "xml","md","txt","log","ini","cfg","properties","htm")
}

fun isVideo(path: String): Boolean {
    val extension = path.substringAfterLast('.', "").lowercase()
    return extension in listOf("mp4", "mkv", "webm", "avi", "mov")
}

fun isImage(path: String): Boolean {
    val extension = path.substringAfterLast('.', "").lowercase()
    return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
}
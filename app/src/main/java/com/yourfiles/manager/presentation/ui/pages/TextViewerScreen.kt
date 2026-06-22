package com.yourfiles.manager.presentation.ui.pages

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "TextViewer"
private const val MAX_INITIAL_BYTES = 500_000L // ~500KB shown initially

/**
 * Text/code file viewer with monospace font and optional horizontal scroll.
 * Loads file on IO thread, shows up to 500KB initially with "Show more" for larger files.
 */
@Composable
fun TextViewerScreen(
    filePath: String,
) {
    var fileContent by remember(filePath) { mutableStateOf<String?>(null) }
    var isLoading by remember(filePath) { mutableStateOf(true) }
    var error by remember(filePath) { mutableStateOf<String?>(null) }
    var showFull by remember(filePath) { mutableStateOf(false) }
    var isFullLoaded by remember(filePath) { mutableStateOf(false) }
    var fullContent by remember(filePath) { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val loadFullJob = remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose { loadFullJob.value?.cancel() }
    }

    val file = File(filePath)
    val fileSize = remember(filePath) { if (file.exists()) file.length() else 0L }
    val isLargeFile = fileSize > MAX_INITIAL_BYTES

    // Load initial content (up to 500KB)
    LaunchedEffect(filePath) {
        try {
            val text = withContext(Dispatchers.IO) {
                if (isLargeFile) {
                    file.inputStream().use { ins ->
                        val buf = ByteArray(MAX_INITIAL_BYTES.toInt())
                        val read = ins.read(buf)
                        String(buf, 0, read, charset("UTF-8"))
                    } + "\n\n--- File truncated (%,d bytes total) ---".format(fileSize)
                } else {
                    file.readText(charset("UTF-8"))
                }
            }
            fileContent = text
        } catch (e: Exception) {
            Log.e(TAG, "Error reading $filePath", e)
            error = e.message
        }
        isLoading = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = error ?: "Error loading file",
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = { /* retry not needed for local */ }) {
                        Text("OK")
                    }
                }
            }
            fileContent != null -> {
                val displayText = if (isLargeFile && showFull && fullContent != null) {
                    fullContent!!
                } else {
                    fileContent!!
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                    ) {
                        Text(
                            text = displayText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                        )
                    }

                    // Show more button for large files
                    if (isLargeFile && !showFull && !isFullLoaded) {
                        TextButton(
                            onClick = {
                                showFull = true
                                loadFullJob.value = scope.launch(Dispatchers.IO) {
                                    val text = try { file.readText(charset("UTF-8")) }
                                    catch (e: Exception) { fileContent!! }
                                    fullContent = text
                                    isFullLoaded = true
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text("Load full file (%,d bytes)".format(fileSize))
                        }
                    }
                }
            }
        }
    }
}

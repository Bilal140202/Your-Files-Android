package com.yourfiles.manager.presentation.ui.pages

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

/**
 * Native in-app PDF viewer using Android's PdfRenderer.
 * Pure Jetpack Compose — no Activities, no XML.
 *
 * Features:
 *  - Renders pages via PdfRenderer on background threads
 *  - HorizontalPager for swipe navigation between pages
 *  - Pinch-to-zoom and pan support (built-in detectTransformGestures)
 *  - Double-tap to reset zoom
 *  - Bottom bar: prev/next buttons + page indicator
 *  - Page cache (prev/current/next) for smooth swiping
 *  - Large PDF loading indicator (>10 MB)
 *  - White background, ES-style minimal UI
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfViewerScreen(
    filePath: String,
) {
    val file = remember(filePath) { File(filePath) }
    val fileExists = remember(filePath) { file.exists() }
    val fileSizeMB = remember(filePath) { file.length() / (1024.0 * 1024.0) }
    val fileName = remember(filePath) { file.name }

    // PDF renderer state
    var pageCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Page bitmap cache: map of page index -> Bitmap
    val pageCache = remember { mutableMapOf<Int, Bitmap>() }
    // Track rendering jobs so we can cancel them
    val renderJobs = remember { mutableMapOf<Int, Job>() }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { max(1, pageCount) }
    )

    val coroutineScope = rememberCoroutineScope()

    // Open PdfRenderer and get page count
    DisposableEffect(filePath) {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        if (!fileExists) {
            loading = false
            loadError = "File not found"
        } else {
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                loading = false
            } catch (e: Exception) {
                loading = false
                loadError = "Failed to open PDF: ${e.localizedMessage}"
            }
        }

        onDispose {
            // Cancel all pending render jobs
            renderJobs.values.forEach { it.cancel() }
            renderJobs.clear()
            // Close all cached bitmaps
            pageCache.values.forEach { if (!it.isRecycled) it.recycle() }
            pageCache.clear()
            // Close renderer
            renderer?.close()
            pfd?.close()
        }
    }

    // Prefetch adjacent pages whenever the current page changes
    LaunchedEffect(pagerState.currentPage, pageCount) {
        if (pageCount <= 0) return@LaunchedEffect
        val current = pagerState.currentPage
        // Cache window: current ± 1
        val pagesToRender = listOf(current - 1, current, current + 1)
            .filter { it in 0 until pageCount }
            .filter { it !in pageCache }

        for (pageIdx in pagesToRender) {
            // Skip if already rendering
            if (pageIdx in renderJobs) continue
            renderJobs[pageIdx] = coroutineScope.launch(Dispatchers.IO) {
                try {
                    renderPage(file, pageIdx)?.let { bitmap ->
                        // Double-check still valid (not navigated away)
                        if (pageIdx !in pageCache) {
                            pageCache[pageIdx] = bitmap
                        } else {
                            bitmap.recycle()
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    renderJobs.remove(pageIdx)
                }
            }
        }

        // Evict pages outside the window to save memory
        val keysToEvict = pageCache.keys.filter {
            it < current - 1 || it > current + 1
        }
        keysToEvict.forEach { key ->
            pageCache.remove(key)?.recycle()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        when {
            loadError != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(loadError!!, color = Color.Red, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("File: $fileName", color = Color.Gray, fontSize = 12.sp)
                }
            }
            loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (fileSizeMB > 10) {
                        Text(
                            "Loading PDF (${"%.1f".format(fileSizeMB)} MB)...",
                            color = Color.Gray,
                            fontSize = 14.sp,
                        )
                    } else {
                        Text("Loading PDF...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            pageCount == 0 && !loading -> {
                Text("Empty PDF", color = Color.Gray, fontSize = 14.sp)
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // PDF page pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        pageSpacing = 0.dp,
                    ) { pageIndex ->
                        PdfPageView(
                            pageIndex = pageIndex,
                            bitmap = pageCache[pageIndex],
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    // Bottom navigation bar
                    PdfBottomNavBar(
                        currentPage = pagerState.currentPage + 1,
                        totalPages = pageCount,
                        onPrev = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        onNext = {
                            if (pagerState.currentPage < pageCount - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Renders a single PDF page to a Bitmap on IO thread.
 * Uses a reasonable max dimension to avoid OOM on large pages.
 * Each call opens/closes its own PdfRenderer — safe for parallel invocation.
 */
private fun renderPage(file: File, pageIndex: Int): Bitmap? {
    return try {
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val page = renderer.openPage(pageIndex)

        // Cap bitmap size to avoid OOM — max 2048px on longest side
        val maxWidth = 2048
        val scale = if (page.width > maxWidth) maxWidth.toFloat() / page.width else 1f
        val bitmap = Bitmap.createBitmap(
            (page.width * scale).toInt().coerceAtLeast(1),
            (page.height * scale).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        renderer.close()
        pfd.close()

        bitmap
    } catch (_: Exception) {
        null
    }
}

/**
 * Displays a single PDF page bitmap with zoom & pan support.
 * Uses Compose's built-in detectTransformGestures for pinch-to-zoom.
 * Double-tap resets zoom to 1x.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PdfPageView(
    pageIndex: Int,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
) {
    if (bitmap == null) {
        // Page still loading or failed
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Page ${pageIndex + 1}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    // Zoom & pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double-tap: reset zoom
                        scale = 1f
                        offset = Offset.Zero
                    },
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                    // Calculate pan bounds based on current zoom
                    val maxX = (size.width * (newScale - 1)) / 2f
                    val maxY = (size.height * (newScale - 1)) / 2f
                    offset = Offset(
                        x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                        y = (offset.y + panChange.y).coerceIn(-maxY, maxY),
                    )
                    scale = newScale
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Image(
            bitmap = imageBitmap,
            contentDescription = "Page ${pageIndex + 1}",
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )
    }
}

/**
 * Bottom navigation bar for PDF pages: prev/next buttons + page indicator.
 * ES-style: minimal, clean, functional. Light gray background.
 */
@Composable
private fun PdfBottomNavBar(
    currentPage: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Prev button
        IconButton(
            onClick = onPrev,
            enabled = currentPage > 1,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous page",
                tint = if (currentPage > 1) MaterialTheme.colorScheme.primary else Color.LightGray,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Page indicator
        Text(
            text = "$currentPage / $totalPages",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Next button
        IconButton(
            onClick = onNext,
            enabled = currentPage < totalPages,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next page",
                tint = if (currentPage < totalPages) MaterialTheme.colorScheme.primary else Color.LightGray,
            )
        }
    }
}

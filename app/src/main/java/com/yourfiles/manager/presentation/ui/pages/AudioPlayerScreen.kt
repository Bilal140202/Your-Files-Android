package com.yourfiles.manager.presentation.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * In-app audio player using ExoPlayer 2.x.
 * Pure Jetpack Compose — no Activities, no XML.
 *
 * ES-style minimal: play/pause + seek + time display.
 * No playlist, no equalizer, no visualizations.
 *
 * Features:
 *  - Album art placeholder with music note icon
 *  - Song title + file size info
 *  - Seek slider with current/total time
 *  - Play/Pause toggle button
 *  - Auto-pause when composable leaves composition
 *  - Background rendering for seek bar updates
 */
@Composable
fun AudioPlayerScreen(
    filePath: String,
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }
    val fileName = remember(filePath) { file.nameWithoutExtension.ifEmpty { "Unknown" } }
    val fileSize = remember(filePath) { formatFileSize(file.length()) }

    // Player state
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableFloatStateOf(0f) }
    var totalDurationMs by remember { mutableFloatStateOf(0f) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // Create ExoPlayer — use remember + DisposableEffect for lifecycle
    val player = remember(filePath) {
        ExoPlayer.Builder(context).build()
    }

    // Initialize player with the audio file
    LaunchedEffect(filePath) {
        try {
            val uri = file.toUri()
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            playbackError = "Failed to load audio: ${e.localizedMessage}"
        }
    }

    // Observe player state changes
    LaunchedEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    totalDurationMs = player.duration.toFloat()
                }
            }

            override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                playbackError = "Playback error: ${error.localizedMessage}"
            }
        }
        player.addListener(listener)

        // Poll position for seek bar updates (every 250ms)
        while (true) {
            try {
                if (player.playbackState == Player.STATE_READY) {
                    currentPositionMs = player.currentPosition.toFloat()
                }
            } catch (_: Exception) {
                break
            }
            kotlinx.coroutines.delay(250)
        }
    }

    // Release player on dispose
    DisposableEffect(filePath) {
        onDispose {
            player.run {
                stop()
                release()
            }
        }
    }

    // Dark background matching the file viewer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Error state
            if (playbackError != null) {
                Text(
                    text = playbackError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            // Album art placeholder — music note icon in a circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Music",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song title
            Text(
                text = fileName,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // File size info
            Text(
                text = fileSize,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Seek slider
            if (totalDurationMs > 0f) {
                Slider(
                    value = if (totalDurationMs > 0f) {
                        (currentPositionMs / totalDurationMs).coerceIn(0f, 1f)
                    } else {
                        0f
                    },
                    onValueChange = { fraction ->
                        val seekMs = fraction * totalDurationMs
                        player.seekTo(seekMs.toLong())
                        currentPositionMs = seekMs
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time row: current / total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatTime(currentPositionMs.toLong()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatTime(totalDurationMs.toLong()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (isBuffering) {
                // Show buffering state while duration is not yet known
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading audio...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play / Pause button — large, centered
            IconButton(
                onClick = {
                    if (isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rewind 10s / Forward 10s buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Rewind 10s
                IconButton(onClick = {
                    val seekTo = max(0L, player.currentPosition - 10_000)
                    player.seekTo(seekTo)
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Rewind 10s",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Forward 10s
                IconButton(onClick = {
                    val seekTo = minOf(player.duration, player.currentPosition + 10_000)
                    player.seekTo(seekTo)
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Forward 10s",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

/** Format milliseconds to mm:ss or h:mm:ss */
private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

/** Format file size to human-readable string */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

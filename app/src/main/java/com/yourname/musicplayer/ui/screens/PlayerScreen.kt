package com.yourname.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourname.musicplayer.ui.components.SongThumbnail
import com.yourname.musicplayer.ui.viewmodels.SharedMusicViewModel
import java.util.concurrent.TimeUnit

@Composable
fun PlayerScreen(
    viewModel: SharedMusicViewModel,
    onMinimizeClick: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isShuffleOn by viewModel.isShuffleModeOn.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    // Temporary tracking helper state to prevent track skip sliding jumps
    var sliderScrubPosition by remember { mutableStateOf<Long?>(null) }

    currentSong?.let { song ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Header Top Bar Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onMinimizeClick) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Minimize")
                }
                Text("Now Playing", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterVertically))
                IconButton(onClick = { /* Handle playlist adding later */ }) {
                    Icon(Icons.Rounded.PlaylistAdd, contentDescription = "Add to playlist")
                }
            }

            // Big Centered Layout Cover Art Canvas
            SongThumbnail(
                song = song,
                cornerRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )

            // Tracks String Details Typography Labels Block
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Scrubbing Progress Slider Logic Component Row
            Column {
                Slider(
                    value = (sliderScrubPosition ?: currentPosition).toFloat(),
                    onValueChange = { sliderScrubPosition = it.toLong() },
                    onValueChangeFinished = {
                        sliderScrubPosition?.let { viewModel.seekTo(it) }
                        sliderScrubPosition = null
                    },
                    valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        thumbColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(sliderScrubPosition ?: currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Hardware Dashboard Command Triggers Console Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Previous Button
                IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }

                // Play / Pause Hub
                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play or Pause",
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Next Button
                IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }

                // Placeholder for your requested Sleep Countdown features
                IconButton(onClick = { /* Open Sleep timer dialog trigger */ }) {
                    Icon(Icons.Rounded.Timer, contentDescription = "Sleep Timer")
                }
            }
        }
    }
}

// Convert long milliseconds values to user-readable (MM:SS) formats
private fun formatTime(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

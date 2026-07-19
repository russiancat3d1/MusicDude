package com.yourname.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.musicplayer.ui.components.SongListItem
import com.yourname.musicplayer.ui.viewmodels.SharedMusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    viewModel: SharedMusicViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter list based on what the user types into the search field
    val filteredSongs = remember(songs, searchQuery) {
        songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search your songs...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        // Scrollable catalog listing
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Leave safety room for expanded player overlay
        ) {
            items(filteredSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isSelected = song.id == currentSong?.id,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}
package com.yourname.musicplayer.ui.viewmodels

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.yourname.musicplayer.data.model.Song
import com.yourname.musicplayer.service.MusicPlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedMusicViewModel(application: Application) : AndroidViewModel(application) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // --- UI States exposed to Jetpack Compose ---
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isShuffleModeOn = MutableStateFlow(false)
    val isShuffleModeOn = _isShuffleModeOn.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    init {
        initializeController()
        startPositionTracker()
    }

    // 1. Establish connection to the background Service
    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicPlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).build()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            updatePlaybackState()
        }, MoreExecutors.directExecutor())
    }

    // 2. Listen for changes happening directly inside ExoPlayer
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
            
            // Map the MediaItem back to our custom Song model for the UI
            mediaItem?.let { item ->
                _currentSong.value = _songs.value.find { it.id.toString() == item.mediaId }
            } ?: run {
                _currentSong.value = null
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleModeOn.value = shuffleModeEnabled
        }
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _isShuffleModeOn.value = controller.shuffleModeEnabled
            _duration.value = controller.duration.coerceAtLeast(0L)
            val currentMediaId = controller.currentMediaItem?.mediaId
            _currentSong.value = _songs.value.find { it.id.toString() == currentMediaId }
        }
    }

    // 3. Keep track of song progress (runs a loop every 500ms while playing)
    private fun startPositionTracker() = viewModelScope.launch {
        while (true) {
            if (_isPlaying.value) {
                _currentPosition.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
            }
            delay(500)
        }
    }

    // --- UI Intent / Control Actions ---

    fun setSongList(scannedSongs: List<Song>) {
        _songs.value = scannedSongs
    }

    fun playSong(song: Song) {
        val controller = mediaController ?: return

        // Check if this song is already in the queue, or build a new queue
        val existingIndex = (0 until controller.mediaItemCount).find { 
            controller.getMediaItemAt(it).mediaId == song.id.toString() 
        }

        if (existingIndex != null) {
            controller.seekTo(existingIndex, 0L)
            controller.play()
        } else {
            // Convert our list to Media3 MediaItems so ExoPlayer understands metadata
            val mediaItems = _songs.value.map { currentListSong ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(currentListSong.title)
                    .setArtist(currentListSong.artist)
                    .setAlbumTitle(currentListSong.album)
                    .setArtworkUri(currentListSong.uri) // Coil will read this Uri for thumbnail later
                    .build()

                MediaItem.Builder()
                    .setMediaId(currentListSong.id.toString())
                    .setUri(currentListSong.uri)
                    .setMediaMetadata(metadata)
                    .build()
            }
            
            controller.setMediaItems(mediaItems)
            val targetIndex = _songs.value.indexOf(song).coerceAtLeast(0)
            controller.seekTo(targetIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    // 4. Clean up connection when the app lifecycle moves away
    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
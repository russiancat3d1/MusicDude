package com.yourname.musicplayer.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    // 1. Initialize the player and media session when the service starts
    override fun onCreate() {
        super.onCreate()

        // Configure audio attributes to handle Audio Focus automatically
        // This handles pausing for phone calls and lowering volume for notifications
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // Build the underlying player engine (ExoPlayer)
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // true = handle audio focus automatically
            .setWakeMode(C.WAKE_MODE_LOCAL) // Keeps CPU awake during background playback
            .setHandleAudioBecomingNoisy(true) // Pauses music if headphones are unplugged
            .build()

        // Build the MediaSession and link it to our player
        player?.let { exoPlayer ->
            mediaSession = MediaSession.Builder(this, exoPlayer)
                .build()
        }
    }

    // 2. Return the active media session to any system controllers or your UI
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // 3. Handle when the system/user swipes away the app from recent apps
    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        // If the player isn't actively playing anything, shut down the service completely
        if (player == null || !player.playWhenReady || player.playbackState == ExoPlayer.STATE_IDLE) {
            stopSelf()
        }
    }

    // 4. Clean up resources when the service is destroyed to prevent memory leaks
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
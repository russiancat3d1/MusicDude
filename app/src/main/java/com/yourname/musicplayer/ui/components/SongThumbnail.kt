package com.yourname.musicplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui. those.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.yourname.musicplayer.data.model.Song
import com.yourname.musicplayer.utils.ThumbnailExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SongThumbnail(
    song: Song,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Load the bitmap from the MP3 file asynchronously in a background thread
    val albumArtState = produceState<Bitmap?>(initialValue = null, song.uri) {
        value = withContext(Dispatchers.IO) {
            ThumbnailExtractor.getAlbumArt(context, song.uri)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius)) // FIX: Ensure it looks exactly like this line
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = albumArtState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback icon if the MP3 file doesn't have an embedded thumbnail
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = "No Artwork",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
package com.yourname.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri

object ThumbnailExtractor {

    /**
     * Extracts the embedded picture (album art) from an audio file URI.
     * Returns a Bitmap if found, or null if there is no embedded art.
     */
    fun getAlbumArt(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            // Give the retriever the file's URI
            retriever.setDataSource(context, uri)
            
            // Extract the embedded picture array
            val artBytes = retriever.embeddedPicture
            
            if (artBytes != null) {
                // Convert the raw bytes into an Android Bitmap
                BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            // Always release the retriever to prevent memory leaks
            retriever.release()
        }
    }
}
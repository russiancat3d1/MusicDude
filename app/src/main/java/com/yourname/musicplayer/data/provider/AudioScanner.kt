package com.yourname.musicplayer.data.provider

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.yourname.musicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioScanner(private val context: Context) {

    // Runs on a background thread so it doesn't freeze the UI
    suspend fun getLocalAudioFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        // 1. Define the URI for external storage audio files
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // 2. Define which columns we want to read from the database
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        // 3. Filter out voice notes or non-music files (only get IS_MUSIC = 1)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        // 4. Sort alphabetically by title
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        // 5. Execute the query
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices for performance
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            // Loop through the results
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val duration = cursor.getLong(durationColumn)

                // Skip files shorter than 30 seconds (usually ringtones/notifications)
                if (duration < 30000) continue

                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                
                // Build the URI needed to play the file
                val contentUri = ContentUris.withAppendedId(collection, id)

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri
                    )
                )
            }
        }
        return@withContext songs
    }
}
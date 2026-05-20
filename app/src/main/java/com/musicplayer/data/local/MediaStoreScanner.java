package com.musicplayer.data.local;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.musicplayer.data.model.Song;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreScanner {

    public static List<Song> scanAudioFiles(Context context) {
        List<Song> songs = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (Cursor cursor = resolver.query(collection, projection, selection, null, null)) {
            if (cursor != null) {
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long duration = cursor.getLong(durationCol);

                    if (title == null || title.isEmpty()) {
                        title = path.substring(path.lastIndexOf('/') + 1);
                    }
                    if (artist == null || artist.isEmpty() || "<unknown>".equals(artist)) {
                        artist = "Unknown Artist";
                    }
                    if (album == null || album.isEmpty()) {
                        album = "Unknown Album";
                    }

                    songs.add(new Song(path, title, artist, album, duration, null));
                }
            }
        }
        return songs;
    }
}

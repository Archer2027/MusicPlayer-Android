package com.musicplayer.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.musicplayer.data.model.Song;

@Database(entities = {Song.class}, version = 1, exportSchema = false)
public abstract class MusicDatabase extends RoomDatabase {

    private static volatile MusicDatabase instance;

    public abstract SongDao songDao();

    public static MusicDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (MusicDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        MusicDatabase.class,
                        "music_database"
                    ).build();
                }
            }
        }
        return instance;
    }
}

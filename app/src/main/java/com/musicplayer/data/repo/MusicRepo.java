package com.musicplayer.data.repo;

import android.content.Context;

import com.musicplayer.data.local.MediaStoreScanner;
import com.musicplayer.data.local.MusicDatabase;
import com.musicplayer.data.model.Song;

import java.util.List;

public class MusicRepo {

    private final MusicDatabase database;

    public MusicRepo(Context context) {
        this.database = MusicDatabase.getInstance(context);
    }

    /** Scan device and persist results in Room */
    public List<Song> scanAndRefresh(Context context) {
        List<Song> scanned = MediaStoreScanner.scanAudioFiles(context);
        database.songDao().deleteAll();
        database.songDao().insertAll(scanned);
        return scanned;
    }

    /** Load all songs from local database */
    public List<Song> getAllSongs() {
        return database.songDao().getAllSongs();
    }

    /** Get a single song by ID */
    public Song getSongById(long id) {
        return database.songDao().getSongById(id);
    }

    /** Get total song count */
    public int getSongCount() {
        return database.songDao().getCount();
    }
}

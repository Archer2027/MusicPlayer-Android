package com.musicplayer.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.musicplayer.data.model.Song;

import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    List<Song> getAllSongs();

    @Query("SELECT * FROM songs WHERE id = :id")
    Song getSongById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Song song);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<Song> songs);

    @Query("DELETE FROM songs")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM songs")
    int getCount();
}

package com.musicplayer.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.musicplayer.data.model.Song;
import com.musicplayer.data.repo.MusicRepo;

import java.util.List;

public class PlayerViewModel extends AndroidViewModel {

    private final MusicRepo repo;
    private final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public PlayerViewModel(Application application) {
        super(application);
        repo = new MusicRepo(application);
    }

    /** Scan device for audio files and persist in Room */
    public void scanMusic() {
        loading.setValue(true);
        new Thread(() -> {
            List<Song> result = repo.scanAndRefresh(getApplication());
            songs.postValue(result);
            loading.postValue(false);
        }).start();
    }

    /** Load cached songs from Room without rescanning */
    public void loadCached() {
        new Thread(() -> {
            List<Song> cached = repo.getAllSongs();
            songs.postValue(cached);
        }).start();
    }

    public LiveData<List<Song>> getSongs() { return songs; }
    public LiveData<Boolean> getLoading() { return loading; }
    public int getSongCount() { return repo.getSongCount(); }
}

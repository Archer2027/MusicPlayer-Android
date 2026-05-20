package com.musicplayer.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.service.MusicService;
import com.musicplayer.ui.player.PlayerFragment;
import com.musicplayer.ui.playlist.PlaylistFragment;

public class MainActivity extends AppCompatActivity
    implements PlaylistFragment.SongSelectionListener {

    private MusicService musicService;
    private PlayerFragment playerFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerFragment = new PlayerFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, playerFragment)
            .commit();
    }

    /** Called by Application or Service binder to link MusicService */
    public void setMusicService(MusicService service) {
        this.musicService = service;
        if (playerFragment != null) {
            playerFragment.setMusicService(service);
        }
    }

    public void showPlaylist() {
        PlaylistFragment playlistFragment = new PlaylistFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, playlistFragment)
            .addToBackStack(null)
            .commit();
    }

    // SongSelectionListener callback
    @Override
    public void onSongSelected(Song song) {
        if (musicService != null && musicService.getPlaylist() != null) {
            int index = musicService.getPlaylist().indexOf(song);
            if (index >= 0) {
                musicService.jumpTo(index);
            }
        }
        getSupportFragmentManager().popBackStack();
    }
}

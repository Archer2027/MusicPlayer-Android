package com.musicplayer.ui.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.service.MusicService;
import com.musicplayer.viewmodel.PlayerViewModel;

public class PlayerFragment extends Fragment {

    private MusicService musicService;
    private PlayerViewModel viewModel;

    private TextView tvTitle, tvArtist, tvCurrent, tvTotal;
    private SeekBar seekBar;
    private ImageButton btnPlay, btnNext, btnPrevious;
    private Button btnPlaylist;
    private Handler handler;
    private boolean isUserSeeking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        handler = new Handler(Looper.getMainLooper());
        setupListeners();

        // Load songs and play when ready
        viewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty() && musicService != null) {
                musicService.setPlaylist(songs, 0);
            }
        });
        viewModel.scanMusic();
    }

    private void bindViews(View view) {
        tvTitle = view.findViewById(R.id.tv_song_title);
        tvArtist = view.findViewById(R.id.tv_song_artist);
        tvCurrent = view.findViewById(R.id.tv_current_time);
        tvTotal = view.findViewById(R.id.tv_total_time);
        seekBar = view.findViewById(R.id.seekbar);
        btnPlay = view.findViewById(R.id.btn_play);
        btnNext = view.findViewById(R.id.btn_next);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlaylist = view.findViewById(R.id.btn_show_playlist);
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> {
            if (musicService != null) musicService.playPause();
        });
        btnNext.setOnClickListener(v -> {
            if (musicService != null) musicService.next();
        });
        btnPrevious.setOnClickListener(v -> {
            if (musicService != null) musicService.previous();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) { isUserSeeking = true; }
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                }
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { isUserSeeking = false; }
        });

        // Progress updater — runs every 250ms
        handler.post(new Runnable() {
            @Override public void run() {
                if (musicService != null && !isUserSeeking) {
                    long pos = musicService.getCurrentPosition();
                    long dur = musicService.getDuration();
                    if (dur > 0) {
                        seekBar.setMax((int) dur);
                        seekBar.setProgress((int) pos);
                        tvCurrent.setText(formatTime(pos));
                        tvTotal.setText(formatTime(dur));
                    }
                }
                handler.postDelayed(this, 250);
            }
        });
    }

    /** Connect to service — called from MainActivity after binding */
    public void setMusicService(MusicService service) {
        this.musicService = service;
        if (musicService != null) {
            musicService.getIsPlaying().observe(getViewLifecycleOwner(), playing -> {
                btnPlay.setImageResource(playing != null && playing
                    ? android.R.drawable.ic_media_pause
                    : android.R.drawable.ic_media_play);
            });
            musicService.getCurrentSong().observe(getViewLifecycleOwner(), this::updateSongInfo);
        }
    }

    private void updateSongInfo(Song song) {
        if (song != null) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
        }
    }

    private String formatTime(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }
}

package com.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.ui.MainActivity;

import java.util.List;

public class MusicService extends MediaSessionService {

    private static final String CHANNEL_ID = "music_playback";
    private static final int NOTIFICATION_ID = 1;

    private ExoPlayer player;
    private MediaSession mediaSession;
    private List<Song> playlist;
    private int currentIndex = -1;

    // LiveData for ViewModel observation
    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Long> progress = new MutableLiveData<>(0L);

    @OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel (required for foreground service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Init ExoPlayer with audio focus attributes
        player = new ExoPlayer.Builder(this)
            .setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build();

        // Player listener -> update LiveData
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying.postValue(playing);
                updateNotification();
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (currentIndex >= 0 && currentIndex < playlist.size()) {
                    Song song = playlist.get(currentIndex);
                    currentSong.postValue(song);
                    updateNotification();
                }
            }
        });

        // MediaSession
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    // --- Public API ---

    public void setPlaylist(List<Song> songs, int startIndex) {
        this.playlist = songs;
        this.currentIndex = startIndex;

        player.clearMediaItems();
        for (Song s : songs) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(s.getTitle())
                .setArtist(s.getArtist())
                .setAlbumTitle(s.getAlbum())
                .build();
            MediaItem item = new MediaItem.Builder()
                .setUri(s.getFilePath())
                .setMediaMetadata(metadata)
                .build();
            player.addMediaItem(item);
        }
        player.prepare();
        player.seekTo(startIndex, 0);
        player.play();
        currentSong.postValue(songs.get(startIndex));
        isPlaying.postValue(true);
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    public void play() {
        if (player.getPlaybackState() == Player.STATE_ENDED && playlist != null) {
            player.seekTo(currentIndex, 0);
        }
        player.play();
        isPlaying.postValue(true);
    }

    public void pause() {
        player.pause();
        isPlaying.postValue(false);
    }

    public void playPause() {
        if (player.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void next() {
        if (playlist == null || playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        player.seekTo(currentIndex, 0);
        player.play();
        currentSong.postValue(playlist.get(currentIndex));
    }

    public void previous() {
        if (playlist == null || playlist.isEmpty()) return;
        if (player.getCurrentPosition() > 3000) {
            // Restart current song
            player.seekTo(currentIndex, 0);
        } else {
            currentIndex = currentIndex == 0 ? playlist.size() - 1 : currentIndex - 1;
            player.seekTo(currentIndex, 0);
        }
        currentSong.postValue(playlist.get(currentIndex));
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    public void jumpTo(int index) {
        if (playlist == null || index < 0 || index >= playlist.size()) return;
        currentIndex = index;
        player.seekTo(index, 0);
        player.play();
        currentSong.postValue(playlist.get(index));
    }

    // --- Getters for UI ---

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    public LiveData<Song> getCurrentSong() { return currentSong; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Long> getProgress() { return progress; }
    public int getCurrentIndex() { return currentIndex; }
    public ExoPlayer getPlayer() { return player; }
    public List<Song> getPlaylist() { return playlist; }

    // --- Notification ---

    private Notification buildNotification() {
        Song song = currentSong.getValue();
        String title = song != null ? song.getTitle() : getString(R.string.app_name);
        String artist = song != null ? song.getArtist() : "";

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    // --- MediaSessionService ---

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Don't stop -- keep playing in background
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
}

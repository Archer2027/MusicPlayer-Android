# 音乐播放器 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** Android 音乐播放器，Java + Jetpack MVVM + ExoPlayer，本地音乐扫描播放、后台播放、通知栏控制、中英文、多分辨率适配。

**架构：** Fragment UI → ViewModel (LiveData) → MusicService (Foreground + ExoPlayer + MediaSession) → Repository (Room + MediaStore)。预留在线音乐、歌词、均衡器接口。

**技术栈：** Android Java, Gradle 8.x, AGP 8.x, minSdk 24, targetSdk 34, ExoPlayer (Media3), Room 2.6, ConstraintLayout

---

### Task 1: 项目脚手架

**文件：**
- 创建：`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`
- 创建：`app/src/main/AndroidManifest.xml`, `app/src/main/res/values/strings.xml`
- 创建：`app/src/main/java/com/musicplayer/CLAUDE.md`（项目级技术栈说明）

- [ ] **Step 1: 创建 root build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
}
```

- [ ] **Step 2: 创建 settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MusicPlayer"
include(":app")
```

- [ ] **Step 3: 创建 gradle.properties**

```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
```

- [ ] **Step 4: 创建 app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
}

android {
    namespace = "com.musicplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.musicplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.10.0")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
```

- [ ] **Step 5: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 权限 -->
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.Dark">

        <!-- 权限声明 -->
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 后台播放 Service -->
        <service
            android:name=".service.MusicService"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback">
        </service>

    </application>
</manifest>
```

- [ ] **Step 6: 创建默认 strings.xml（英文）**

`app/src/main/res/values/strings.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Music Player</string>
    <string name="play">Play</string>
    <string name="pause">Pause</string>
    <string name="next">Next</string>
    <string name="previous">Previous</string>
    <string name="playlist">Playlist</string>
    <string name="no_songs">No songs found</string>
    <string name="scanning">Scanning music…</string>
    <string name="unknown_artist">Unknown Artist</string>
    <string name="unknown_album">Unknown Album</string>
    <string name="switch_language">Switch Language</string>
</resources>
```

- [ ] **Step 7: 初始化 Gradle Wrapper**

```bash
cd /Users/archer/ClaudeCode/musicplayer && gradle wrapper --gradle-version 8.5
```

- [ ] **Step 8: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "chore: Android 项目脚手架"
```

---

### Task 2: 数据模型（Room）

**文件：**
- 创建：`app/src/main/java/com/musicplayer/data/model/Song.java`
- 创建：`app/src/main/java/com/musicplayer/data/local/SongDao.java`
- 创建：`app/src/main/java/com/musicplayer/data/local/MusicDatabase.java`

- [ ] **Step 1: 创建 Song.java（Room Entity）**

```java
package com.musicplayer.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "duration")
    private long duration; // milliseconds

    @ColumnInfo(name = "album_art_path")
    private String albumArtPath;

    public Song() {}

    public Song(String filePath, String title, String artist,
                String album, long duration, String albumArtPath) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.albumArtPath = albumArtPath;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getAlbumArtPath() { return albumArtPath; }
    public void setAlbumArtPath(String albumArtPath) { this.albumArtPath = albumArtPath; }
}
```

- [ ] **Step 2: 创建 SongDao.java**

```java
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
```

- [ ] **Step 3: 创建 MusicDatabase.java**

```java
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
```

- [ ] **Step 4: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL。

- [ ] **Step 5: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: Room 数据模型（Song/SongDao/MusicDatabase）"
```

---

### Task 3: MediaStore 扫描器

**文件：**
- 创建：`app/src/main/java/com/musicplayer/data/local/MediaStoreScanner.java`

- [ ] **Step 1: 创建 MediaStoreScanner.java**

```java
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

    /**
     * Scan device for local audio files via MediaStore.
     * Returns a list of Song objects — no Room insert yet,
     * caller decides storage strategy.
     */
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
```

- [ ] **Step 2: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: MediaStore 音频扫描器"
```

---

### Task 4: Repository + 预留接口

**文件：**
- 创建：`app/src/main/java/com/musicplayer/data/repo/MusicRepo.java`
- 创建：`app/src/main/java/com/musicplayer/data/reserved/OnlineMusicRepo.java`
- 创建：`app/src/main/java/com/musicplayer/data/reserved/LyricsProvider.java`
- 创建：`app/src/main/java/com/musicplayer/data/reserved/EqualizerProvider.java`

- [ ] **Step 1: 创建 MusicRepo.java**

```java
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
```

- [ ] **Step 2: 创建 OnlineMusicRepo.java（预留接口）**

```java
package com.musicplayer.data.reserved;

import com.musicplayer.data.model.Song;
import java.util.List;

/**
 * Reserved interface for online music data source.
 * Implement when integrating with streaming APIs.
 */
public interface OnlineMusicRepo {
    void search(String query, SearchCallback callback);
    List<Song> getTrending();
    List<Song> getRecommendations();

    interface SearchCallback {
        void onResult(List<Song> results);
        void onError(String error);
    }
}
```

- [ ] **Step 3: 创建 LyricsProvider.java（预留接口）**

```java
package com.musicplayer.data.reserved;

/**
 * Reserved interface for synchronized lyrics display.
 * Implement when adding LRC / enhanced lyrics support.
 */
public interface LyricsProvider {
    /** Get lyrics text for a song */
    String getLyrics(String title, String artist);

    /** Get timestamped lyrics lines */
    List<LyricsLine> getSyncedLyrics(String title, String artist);

    class LyricsLine {
        public long timestampMs;
        public String text;
    }
}
```

- [ ] **Step 4: 创建 EqualizerProvider.java（预留接口）**

```java
package com.musicplayer.data.reserved;

/**
 * Reserved interface for audio equalizer control.
 * Implement when adding AudioEffect / Equalizer support.
 */
public interface EqualizerProvider {
    void setBandLevel(int band, int level);
    int[] getBandLevels();
    int getNumberOfBands();
    void enable(boolean enabled);
    boolean isEnabled();
    String[] getPresetNames();
    void setPreset(int index);
}
```

- [ ] **Step 5: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 6: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: Repository + 预留接口（Online/lyrics/EQ）"
```

---

### Task 5: MusicService — 后台播放核心

**文件：**
- 创建：`app/src/main/java/com/musicplayer/service/MusicService.java`
- 创建：`app/src/main/res/values/strings.xml`（已有，补充通知栏字符串）

- [ ] **Step 1: 补充 strings.xml 通知栏文案**

在 `app/src/main/res/values/strings.xml` 中添加：

```xml
<string name="notification_channel">Music Playback</string>
<string name="notification_title">Music Player</string>
```

- [ ] **Step 2: 创建 MusicService.java**

```java
package com.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import androidx.media3.session.MediaController;

import com.musicplayer.data.model.Song;

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

        // Create notification channel
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

        // Init ExoPlayer with audio attributes
        player = new ExoPlayer.Builder(this)
            .setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build();

        // Player listener → update LiveData
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying.postValue(playing);
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (mediaItem != null && currentIndex >= 0 && currentIndex < playlist.size()) {
                    currentSong.postValue(playlist.get(currentIndex));
                }
            }
        });

        // MediaSession
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    // ─── Public API for Application / ViewModel ───

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
        if (player.isPlaying()) pause(); else play();
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

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public long getDuration() {
        return player.getDuration();
    }

    // LiveData getters
    public LiveData<Song> getCurrentSong() { return currentSong; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Long> getProgress() { return progress; }

    public int getCurrentIndex() { return currentIndex; }
    public ExoPlayer getPlayer() { return player; }

    // ─── MediaSessionService ───

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Keep service alive with notification
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
```

- [ ] **Step 3: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 4: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: MusicService 后台播放 + ExoPlayer + MediaSession"
```

---

### Task 6: PlayerViewModel

**文件：**
- 创建：`app/src/main/java/com/musicplayer/viewmodel/PlayerViewModel.java`

- [ ] **Step 1: 创建 PlayerViewModel.java**

```java
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

    /** Scan device for audio files */
    public void scanMusic() {
        loading.setValue(true);
        new Thread(() -> {
            List<Song> result = repo.scanAndRefresh(getApplication());
            songs.postValue(result);
            loading.postValue(false);
        }).start();
    }

    /** Load cached songs without rescanning */
    public void loadCached() {
        new Thread(() -> {
            List<Song> cached = repo.getAllSongs();
            songs.postValue(cached);
        }).start();
    }

    // ─── LiveData getters ───
    public LiveData<List<Song>> getSongs() { return songs; }
    public LiveData<Boolean> getLoading() { return loading; }
    public int getSongCount() { return repo.getSongCount(); }
}
```

- [ ] **Step 2: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: PlayerViewModel — 扫描/加载歌曲"
```

---

### Task 7: PlayerFragment — 播放控制 UI

**文件：**
- 创建：`app/src/main/res/layout/fragment_player.xml`
- 创建：`app/src/main/java/com/musicplayer/ui/player/PlayerFragment.java`

- [ ] **Step 1: 创建 fragment_player.xml（ConstraintLayout）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground">

    <!-- 歌曲信息 -->
    <TextView
        android:id="@+id/tv_song_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="@string/no_songs"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginTop="32dp"
        android:layout_marginHorizontal="24dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tv_song_artist"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:gravity="center"
        android:layout_marginTop="8dp"
        android:layout_marginHorizontal="24dp"
        app:layout_constraintTop_toBottomOf="@id/tv_song_title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- 进度条 -->
    <SeekBar
        android:id="@+id/seekbar"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:layout_marginHorizontal="24dp"
        app:layout_constraintTop_toBottomOf="@id/tv_song_artist"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tv_current_time"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="0:00"
        android:textSize="12sp"
        android:layout_marginStart="24dp"
        android:layout_marginTop="4dp"
        app:layout_constraintTop_toBottomOf="@id/seekbar"
        app:layout_constraintStart_toStartOf="parent" />

    <TextView
        android:id="@+id/tv_total_time"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="0:00"
        android:textSize="12sp"
        android:layout_marginEnd="24dp"
        android:layout_marginTop="4dp"
        app:layout_constraintTop_toBottomOf="@id/seekbar"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- 播放控制按钮 -->
    <ImageButton
        android:id="@+id/btn_previous"
        android:layout_width="56dp"
        android:layout_height="56dp"
        android:src="@android:drawable/ic_media_previous"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/previous"
        android:layout_marginTop="32dp"
        app:layout_constraintTop_toBottomOf="@id/tv_current_time"
        app:layout_constraintEnd_toStartOf="@id/btn_play"
        app:layout_constraintHorizontal_chainStyle="spread_inside" />

    <ImageButton
        android:id="@+id/btn_play"
        android:layout_width="72dp"
        android:layout_height="72dp"
        android:src="@android:drawable/ic_media_play"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/play"
        app:layout_constraintTop_toTopOf="@id/btn_previous"
        app:layout_constraintBottom_toBottomOf="@id/btn_previous"
        app:layout_constraintStart_toEndOf="@id/btn_previous"
        app:layout_constraintEnd_toStartOf="@id/btn_next" />

    <ImageButton
        android:id="@+id/btn_next"
        android:layout_width="56dp"
        android:layout_height="56dp"
        android:src="@android:drawable/ic_media_next"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/next"
        app:layout_constraintTop_toTopOf="@id/btn_previous"
        app:layout_constraintBottom_toBottomOf="@id/btn_previous"
        app:layout_constraintStart_toEndOf="@id/btn_play" />

    <!-- 列表入口 -->
    <Button
        android:id="@+id/btn_show_playlist"
        android:layout_width="wrap_content"
        android:layout_height="48dp"
        android:text="@string/playlist"
        android:layout_marginTop="16dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btn_play"
        style="?attr/materialButtonOutlinedStyle" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: 创建 PlayerFragment.java**

```java
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

import java.util.List;

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

        // Load songs and start playback
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
        btnPlaylist.setOnClickListener(v -> {
            // Navigate to playlist — wired in MainActivity
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

        // Progress updater
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

    // Connect to service (called from MainActivity after binding)
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
```

- [ ] **Step 3: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 4: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: PlayerFragment — 播放控制 UI"
```

---

### Task 8: PlaylistFragment + MainActivity

**文件：**
- 创建：`app/src/main/res/layout/fragment_playlist.xml`
- 创建：`app/src/main/res/layout/item_song.xml`
- 创建：`app/src/main/java/com/musicplayer/ui/playlist/PlaylistFragment.java`
- 创建：`app/src/main/res/layout/activity_main.xml`
- 创建：`app/src/main/java/com/musicplayer/ui/MainActivity.java`

- [ ] **Step 1: fragment_playlist.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_playlist"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="8dp"
        android:clipToPadding="false" />
</FrameLayout>
```

- [ ] **Step 2: item_song.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="72dp"
    android:paddingHorizontal="16dp"
    android:background="?attr/selectableItemBackground">

    <TextView
        android:id="@+id/item_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:textStyle="bold"
        android:singleLine="true"
        android:ellipsize="end"
        android:layout_marginTop="14dp"
        android:layout_marginEnd="8dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/item_duration" />

    <TextView
        android:id="@+id/item_artist"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:textSize="14sp"
        android:singleLine="true"
        android:ellipsize="end"
        android:layout_marginTop="4dp"
        android:layout_marginEnd="8dp"
        app:layout_constraintTop_toBottomOf="@id/item_title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/item_duration" />

    <TextView
        android:id="@+id/item_duration"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="13sp"
        android:layout_marginEnd="8dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 3: PlaylistFragment.java**

```java
package com.musicplayer.ui.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.viewmodel.PlayerViewModel;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private PlayerViewModel viewModel;
    private SongAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        RecyclerView recycler = view.findViewById(R.id.recycler_playlist);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(song -> {
            // Jump to selected song — handled via MainActivity
            if (getActivity() instanceof SongSelectionListener) {
                ((SongSelectionListener) getActivity()).onSongSelected(song);
            }
        });
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        viewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) adapter.submitList(songs);
        });

        return view;
    }

    public interface SongSelectionListener {
        void onSongSelected(Song song);
    }

    // ─── RecyclerView Adapter ───
    private static class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

        private List<Song> songs;
        private final OnSongClickListener listener;

        interface OnSongClickListener {
            void onClick(Song song);
        }

        SongAdapter(OnSongClickListener listener) { this.listener = listener; }

        void submitList(List<Song> list) {
            this.songs = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song song = songs.get(position);
            holder.title.setText(song.getTitle());
            holder.artist.setText(song.getArtist());
            long sec = song.getDuration() / 1000;
            holder.duration.setText(String.format("%d:%02d", sec / 60, sec % 60));
            holder.itemView.setOnClickListener(v -> listener.onClick(song));
        }

        @Override
        public int getItemCount() { return songs != null ? songs.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, artist, duration;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.item_title);
                artist = v.findViewById(R.id.item_artist);
                duration = v.findViewById(R.id.item_duration);
            }
        }
    }
}
```

- [ ] **Step 4: activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_container"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 5: MainActivity.java**

```java
package com.musicplayer.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.service.MusicService;
import com.musicplayer.ui.player.PlayerFragment;
import com.musicplayer.ui.playlist.PlaylistFragment;

import java.util.List;

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

        // MusicService bound via ServiceConnection — simplified here,
        // in a real build you'd use a ServiceBinder. For now, direct
        // reference established via Application singleton.
    }

    public void setMusicService(MusicService service) {
        this.musicService = service;
        if (playerFragment != null) {
            playerFragment.setMusicService(service);
        }
    }

    private void showPlaylist() {
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
        if (musicService != null) {
            // Find song index and jump
            // (list handled by ViewModel)
        }
        getSupportFragmentManager().popBackStack(); // Return to player
    }
}
```

- [ ] **Step 6: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 7: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: PlaylistFragment + MainActivity"
```

---

### Task 9: 国际化 — 中英文 + LocaleHelper

**文件：**
- 创建：`app/src/main/res/values-zh/strings.xml`
- 创建：`app/src/main/java/com/musicplayer/i18n/LocaleHelper.java`
- 修改：`app/src/main/java/com/musicplayer/ui/MainActivity.java`（附语言切换入口）

- [ ] **Step 1: 创建 values-zh/strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">音乐播放器</string>
    <string name="play">播放</string>
    <string name="pause">暂停</string>
    <string name="next">下一首</string>
    <string name="previous">上一首</string>
    <string name="playlist">播放列表</string>
    <string name="no_songs">暂无歌曲</string>
    <string name="scanning">正在扫描…</string>
    <string name="unknown_artist">未知艺术家</string>
    <string name="unknown_album">未知专辑</string>
    <string name="switch_language">切换语言</string>
    <string name="notification_channel">音乐播放</string>
    <string name="notification_title">音乐播放器</string>
</resources>
```

- [ ] **Step 2: 创建 LocaleHelper.java**

```java
package com.musicplayer.i18n;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_KEY = "app_locale";
    private static final String PREFS_NAME = "settings";

    public static void setLocale(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY, languageCode).apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static void applySavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(PREFS_KEY, "");
        if (!lang.isEmpty()) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);

            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
}
```

- [ ] **Step 3: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 4: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: 中英文国际化 + LocaleHelper"
```

---

### Task 10: 多分辨率适配 — sw600dp 平板双栏

**文件：**
- 创建：`app/src/main/res/layout-sw600dp/activity_main.xml`（平板横屏双栏布局）

- [ ] **Step 1: 创建 layout-sw600dp/activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="horizontal"
    android:baselineAligned="false">

    <!-- 左侧：播放列表（占 1/3） -->
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_playlist"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1" />

    <!-- 右侧：播放器（占 2/3） -->
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_player"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="2" />
</LinearLayout>
```

- [ ] **Step 2: 构建验证**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git add -A && git commit -m "feat: sw600dp 平板双栏布局"
```

---

### Task 11: 构建验证 + 安装包输出

**文件：** 无新文件

- [ ] **Step 1: 全量 Debug 构建**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL。输出 APK 路径：`app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: 验证 APK 存在**

```bash
cd /Users/archer/ClaudeCode/musicplayer && ls -la app/build/outputs/apk/debug/app-debug.apk
```

- [ ] **Step 3: Commit（如有任何修正）**

```bash
cd /Users/archer/ClaudeCode/musicplayer && git status && git add -A && git commit -m "chore: 完成构建验证"
```

# 音乐播放器 — 设计 Spec

**日期：** 2026-05-20
**状态：** 已确认
**范围：** Android 音乐播放器，Java + Jetpack MVVM

## 技术栈

- Android Java + Jetpack MVVM
- ExoPlayer 播放引擎
- Room 数据库
- ConstraintLayout 多分辨率适配
- Foreground Service 后台播放
- MediaSession + 通知栏控制

## 架构

```
UI (Fragment + ConstraintLayout)
  │ LiveData
ViewModel
  │
MusicService (Foreground Service + ExoPlayer + MediaSession)
  │
Repository
  ├── Local: MediaStore 扫描 + Room 持久化
  └── OnlineMusicRepo (interface, 预留)
```

## 文件结构

```
app/src/main/
├── AndroidManifest.xml
├── res/
│   ├── layout/              # ConstraintLayout
│   ├── values/              # strings.xml (英文默认)
│   ├── values-zh/           # strings.xml (中文)
│   └── values-sw600dp/      # 平板横屏双栏
└── java/com/musicplayer/
    ├── ui/
    │   ├── MainActivity.java
    │   ├── player/PlayerFragment.java
    │   └── playlist/PlaylistFragment.java
    ├── viewmodel/PlayerViewModel.java
    ├── service/MusicService.java
    ├── data/
    │   ├── model/Song.java
    │   ├── local/
    │   │   ├── MediaStoreScanner.java
    │   │   ├── SongDao.java
    │   │   └── MusicDatabase.java
    │   ├── repo/MusicRepo.java
    │   └── reserved/
    │       ├── OnlineMusicRepo.java      # interface
    │       ├── LyricsProvider.java        # interface
    │       └── EqualizerProvider.java     # interface
    └── i18n/LocaleHelper.java
```

## MVP 功能

- 扫描本地音乐（MediaStore）
- 播放/暂停/上下一首/拖动进度条
- 播放列表（RecyclerView）
- 后台播放（Foreground Service）
- 通知栏控制（MediaSession）
- 中英文切换（strings.xml 双目录 + LocaleHelper）
- 多分辨率适配（ConstraintLayout + dp/sp + sw600dp 平板双栏）

## 预留接口

- `OnlineMusicRepo` — 在线音乐数据源
- `LyricsProvider` — 歌词数据
- `EqualizerProvider` — 均衡器控制
- `SleepTimerProvider` — 睡眠定时

## 多分辨率

- 手机竖屏：`res/layout/` 单栏
- 平板/横屏：`res/layout-sw600dp/` 双栏
- 单位：dp（密度无关）+ sp（字体）

## 多语言

- `values/strings.xml` — 英文，默认
- `values-zh/strings.xml` — 中文
- 运行时切换：`LocaleHelper.java` 刷新 Activity

## 发布

1. 签名：`keytool -genkey` 生成 keystore
2. 打包：`./gradlew bundleRelease` 输出 AAB
3. Google Play：注册（$25）→ 上传 → 审核
4. 国内市场：加固 + 多渠道

package com.musicplayer.data.reserved;

import java.util.List;

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

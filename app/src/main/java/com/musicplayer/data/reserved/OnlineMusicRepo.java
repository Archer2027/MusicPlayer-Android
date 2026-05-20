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

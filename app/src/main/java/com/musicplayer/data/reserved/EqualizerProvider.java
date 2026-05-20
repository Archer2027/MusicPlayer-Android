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

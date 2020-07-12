package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;

/**
 * Interface for MIDI Soundbank resource
 *
 * @author K.Shoji
 */
public abstract class SoundbankResource {
    private final SF2Soundbank soundbank;

    /**
     * Constructor
     *  @param soundbank the Soundbank
     */
    protected SoundbankResource(@NonNull final SF2Soundbank soundbank) {
        this.soundbank = soundbank;
    }

    /**
     * Get the soundbank
     *
     * @return the Soundbank
     */
    @NonNull
    public SF2Soundbank getSoundbank() {
        return soundbank;
    }
}

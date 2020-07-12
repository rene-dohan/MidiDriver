package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;

/**
 * Interface for MIDI Soundbank resource
 *
 * @author K.Shoji
 */
public abstract class SoundbankResource {
    private final SF2Soundbank soundbank;
    private final Class<?> dataClass;

    /**
     * Constructor
     *  @param soundbank the Soundbank
     * @param dataClass the class of data
     */
    protected SoundbankResource(@NonNull final SF2Soundbank soundbank, @NonNull final Class<?> dataClass) {
        this.soundbank = soundbank;
        this.dataClass = dataClass;
    }

    /**
     * Get the class of data(obtained by {@link #getData()}
     *
     * @return the class
     */
    @Nullable
    public Class<?> getDataClass() {
        return dataClass;
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

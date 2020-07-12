package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;

/**
 * Abstract Class for MIDI Instrument
 *
 * @author K.Shoji
 */
public abstract class Instrument extends SoundbankResource {

    /**
     * Constructor
     * @param soundbank the soundbank
     * @param dataClass the dataClass
     */
    protected Instrument(@NonNull final SF2Soundbank soundbank, @NonNull final Class<?> dataClass) {
        super(soundbank, dataClass);
    }

}

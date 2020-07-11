package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Interface for MIDI Soundbank
 *
 * @author K.Shoji
 */
public interface Soundbank {

    /**
     * Get all of {@link Instrument}s
     *
     * @return the array of {@link Instrument}s
     */
    @NonNull
    Instrument[] getInstruments();

}

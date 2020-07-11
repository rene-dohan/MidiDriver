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
     * Get the {@link Instrument}
     *
     * @param patch the {@link Patch}
     * @return {@link Instrument} matches with patch
     */
    @Nullable
    Instrument getInstrument(@NonNull Patch patch);

    /**
     * Get all of {@link Instrument}s
     *
     * @return the array of {@link Instrument}s
     */
    @NonNull
    Instrument[] getInstruments();

}

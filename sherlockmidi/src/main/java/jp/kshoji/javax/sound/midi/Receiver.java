package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

/**
 * Interface for {@link MidiMessage} receiver.
 * 
 * @author K.Shoji
 */
public interface Receiver {

    /**
	 * Close the {@link Receiver}
	 */
	void close();
}

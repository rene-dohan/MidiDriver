package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;

/**
 * Interface for MIDI Sequencer
 * 
 * @author K.Shoji
 */
public interface Sequencer extends MidiDevice {

	/**
	 * Loop eternally.
	 *
	 */
    int LOOP_CONTINUOUSLY = -1;

    /**
     * {@link Sequencer}'s Synchronization mode
     * 
     * @author K.Shoji
     */
    class SyncMode {
		public static final SyncMode INTERNAL_CLOCK = new SyncMode("Internal Clock");
        public static final SyncMode NO_SYNC = new SyncMode("No Sync");

        private final String name;

        protected SyncMode(@NonNull final String name) {
            this.name = name;
        }
        
        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
            	return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SyncMode other = (SyncMode) obj;
            return name.equals(other.name);
        }

        @Override
        public final int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + name.hashCode();
            return result;
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    /**
     * Get the {@link Sequence}
     * 
     * @return the {@link Sequence}
     */
    @Nullable
    Sequence getSequence();

    /**
     * Get if the {@link Sequencer} is recording.
     * 
     * @return true if the {@link Sequencer} is recording
     */
    boolean isRecording();

    /**
     * Get the current microsecond position.
     */
    long getMicrosecondPosition();

    /**
     * Get if the track is mute on the playback.
     *
     * @param track the track number
     * @return true if the track is mute on the playback
     */
    boolean getTrackMute(int track);

    /**
     * Get if the track is solo on the playback.
     *
     * @param track the track number
     * @return true if the track is solo on the playback.
     */
    boolean getTrackSolo(int track);

}

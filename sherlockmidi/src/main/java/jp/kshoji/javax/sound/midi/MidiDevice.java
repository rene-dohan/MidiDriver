package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Interface for MIDI Device
 *
 * @author K.Shoji
 */
public interface MidiDevice {

    /**
     * Get the device information
     *
     * @return the device information
     */
    @NonNull
    Info getDeviceInfo();

    /**
	 * Get the all of {@link Receiver}s.
	 * 
	 * @return the all of {@link Receiver}s.
	 */
    @NonNull
    List<Receiver> getReceivers();

	/**
	 * Get the all of {@link Transmitter}s.
	 * 
	 * @return the all of {@link Transmitter}s.
	 */
    @NonNull
    List<Transmitter> getTransmitters();

	/**
	 * Represents the {@link MidiDevice}'s information
	 *
	 * @author K.Shoji
	 */
	class Info {
		private final String name;
		private final String vendor;
		private final String description;
		private final String version;

        /**
         * Constructor
         *
         * @param name the name string
         * @param vendor the vendor string
         * @param description the description string
         * @param version the version string
         */
		public Info(@NonNull final String name, @NonNull final String vendor, @NonNull final String description, @NonNull final String version) {
			this.name = name;
			this.vendor = vendor;
			this.description = description;
			this.version = version;
		}

		@NonNull
		@Override
        public final String toString() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + description.hashCode();
			result = prime * result + name.hashCode();
			result = prime * result + vendor.hashCode();
			result = prime * result + version.hashCode();
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Info other = (Info) obj;
			if (!description.equals(other.description)) {
				return false;
			}
			if (!name.equals(other.name)) {
				return false;
			}
			if (!vendor.equals(other.vendor)) {
				return false;
			}
			return version.equals(other.version);
		}
	}
}

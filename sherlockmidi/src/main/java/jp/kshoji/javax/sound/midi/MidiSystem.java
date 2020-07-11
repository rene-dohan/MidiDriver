package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jp.kshoji.javax.sound.midi.MidiDevice.Info;

/**
 * MidiSystem porting for Android
 *
 * @author K.Shoji
 */
public final class MidiSystem {
	private static final Collection<MidiDevice> midiDevices = new HashSet<MidiDevice>();

	/**
	 * Utilities for {@link MidiSystem}
	 *
	 * @author K.Shoji
	 */
	public static class MidiSystemUtils {
		/**
		 * Get currently connected {@link Receiver}s
		 *
		 * @return currently connected {@link Receiver}s
		 * @throws MidiUnavailableException
		 */
        @NonNull
        public static List<Receiver> getReceivers() throws MidiUnavailableException {
			final List<Receiver> result = new ArrayList<Receiver>();
			final Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
			for (final Info midiDeviceInfo : midiDeviceInfos) {
				result.addAll(MidiSystem.getMidiDevice(midiDeviceInfo).getReceivers());
			}

			return result;
		}

		/**
		 * Get currently connected {@link Transmitter}s
		 *
		 * @return currently connected {@link Transmitter}s
		 * @throws MidiUnavailableException
		 */
        @NonNull
        public static List<Transmitter> getTransmitters() throws MidiUnavailableException {
			final List<Transmitter> result = new ArrayList<Transmitter>();
			final Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
			for (final Info midiDeviceInfo : midiDeviceInfos) {
				result.addAll(MidiSystem.getMidiDevice(midiDeviceInfo).getTransmitters());
			}

			return result;
		}
	}

    /**
     * Private Constructor; this class can't be instantiated.
     */
	private MidiSystem() {
	}

	/**
	 * Get all connected {@link Info} as array
	 *
	 * @return device information
	 */
    @NonNull
    public static Info[] getMidiDeviceInfo() {
		final List<Info> result = new ArrayList<Info>();
		synchronized (midiDevices) {
            for (final MidiDevice device : midiDevices) {
                final Info deviceInfo = device.getDeviceInfo();
                if (deviceInfo != null) {
                    result.add(deviceInfo);
                }
            }
		}
		return result.toArray(new Info[result.size()]);
	}

	/**
	 * Get {@link MidiDevice} by device information
	 *
	 * @param info the device information
	 * @return {@link MidiDevice}
	 * @throws MidiUnavailableException
	 * @throws IllegalArgumentException if the device not found.
	 */
    @NonNull
    public static MidiDevice getMidiDevice(@NonNull final Info info) throws MidiUnavailableException, IllegalArgumentException {
        if (midiDevices.isEmpty()) {
            throw new MidiUnavailableException("MidiDevice not found");
        }

        synchronized (midiDevices) {
            for (final MidiDevice midiDevice : midiDevices) {
                if (info.equals(midiDevice.getDeviceInfo())) {
                    return midiDevice;
                }
            }
		}

		throw new IllegalArgumentException("Requested device not installed: " + info);
	}

}

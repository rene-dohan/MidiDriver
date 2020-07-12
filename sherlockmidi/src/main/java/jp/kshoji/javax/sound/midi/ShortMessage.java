package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents MIDI Short Message
 * 
 * @author K.Shoji
 */
public class ShortMessage implements Cloneable {

	@Nullable
	protected byte[] data;

	/**
	 * Get the status of the MidiMessage
	 *
	 * @return the status
	 */
	public int getStatus() {
		if (data == null || data.length < 1) {
			return 0;
		}

		return data[0] & 0xff;
	}

	/**
	 * Convert the byte array to the hex dumped string
	 *
	 * @param src the byte array
	 * @return hex dumped string
	 */
	@NonNull
	static String toHexString(@Nullable final byte[] src) {
		if (src == null) {
			return "null";
		}

		final StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		boolean needComma = false;
		for (final byte srcByte : src) {
			if (needComma) {
				buffer.append(", ");
			}
			buffer.append(String.format("%02x", srcByte & 0xff));
			needComma = true;
		}
		buffer.append("]");

		return buffer.toString();
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + toHexString(data);
	}

	public static final int NOTE_OFF = 0x80;
	public static final int NOTE_ON = 0x90;
	public static final int POLY_PRESSURE = 0xa0;
	public static final int CONTROL_CHANGE = 0xb0;
	public static final int PROGRAM_CHANGE = 0xc0;
	public static final int CHANNEL_PRESSURE = 0xd0;
	public static final int PITCH_BEND = 0xe0;
	public static final int ACTIVE_SENSING = 0xfe;

	/**
	 * Constructor with raw data.
	 * 
	 * @param data the raw data
	 */
	protected ShortMessage(@NonNull final byte[] data) {
		this.data = data;
	}

    /**
	 * Get the channel of this message.
	 * 
	 * @return the channel
	 */
	public int getChannel() {
		return (getStatus() & 0x0f);
	}

	/**
	 * Get the kind of command for this message.
	 * 
	 * @return the kind of command
	 */
	public int getCommand() {
		return (getStatus() & 0xf0);
	}

	/**
	 * Get the first data for this message.
	 * 
	 * @return the first data
	 */
	public int getData1() {
		if (data.length > 1) {
			return data[1] & 0xff;
		}
		return 0;
	}

	/**
	 * Get the second data for this message.
	 * 
	 * @return the second data
	 */
	public int getData2() {
		if (data.length > 2) {
			return data[2] & 0xff;
		}
		return 0;
	}

	@Override
	public Object clone() {
		final byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, result.length);
		return new ShortMessage(result);
	}

}

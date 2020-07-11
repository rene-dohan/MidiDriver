package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

/**
 * Represents MIDI SysEx Message
 * 
 * @author K.Shoji
 */
public class SysexMessage extends MidiMessage {

    /**
	 * Constructor with raw data.
	 * 
	 * @param data the SysEx data
	 */
	protected SysexMessage(@NonNull byte[] data) {
		super(data);
	}

	/**
	 * Get the SysEx data.
	 * 
	 * @return SysEx data
	 */
    @NonNull
    public byte[] getData() {
		final byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, result.length);
		return result;
	}

	@Override
	public Object clone() {
        return new SysexMessage(getData());
	}
}

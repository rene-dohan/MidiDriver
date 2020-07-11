package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Represents MIDI Meta Message
 * 
 * @author K.Shoji
 */
public class MetaMessage extends MidiMessage {

    private static final byte[] emptyData = {};

	private int dataLength = 0;

	/**
	 * Constructor with raw data
	 * 
	 * @param data the data source with META header(2 bytes) + length( > 1 byte), the data.length must be >= 3 bytes
     * @throws NegativeArraySizeException MUST be caught.
	 */
	protected MetaMessage(@NonNull final byte[] data) {
		super(data);

		if (data.length >= 3) {
			// check length
			dataLength = data.length - 3;
			int pos = 2;
			while (pos < data.length && (data[pos] & 0x80) != 0) {
				dataLength--;
				pos++;
			}
		}

        if (dataLength < 0) {
            // 'dataLength' may negative value. Negative 'dataLength' will throw NegativeArraySizeException when getData() called.
            throw new NegativeArraySizeException("Invalid meta event. data: " + Arrays.toString(data));
        }
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	@NonNull
	@Override
	public Object clone() {
		if (data == null) {
			return new MetaMessage(emptyData);
		}
		final byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		return new MetaMessage(result);
	}

}

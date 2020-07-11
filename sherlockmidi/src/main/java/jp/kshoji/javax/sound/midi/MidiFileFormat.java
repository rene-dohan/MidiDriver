package jp.kshoji.javax.sound.midi;

/**
 * Represents MIDI File Format
 * 
 * @author K.Shoji
 */
public class MidiFileFormat {
	public static final int HEADER_MThd = 0x4d546864;
	public static final int HEADER_MTrk = 0x4d54726b;

	public static final int UNKNOWN_LENGTH = -1;

    protected float divisionType;
	protected int resolution;

	/**
	 * Get the division type of {@link MidiFileFormat}
	 * 
	 * @return the division type
	 */
	public float getDivisionType() {
		return divisionType;
	}

	/**
	 * Get the resolution of {@link MidiFileFormat}
	 * 
	 * @return the resolution
	 */
	public int getResolution() {
		return resolution;
	}

}

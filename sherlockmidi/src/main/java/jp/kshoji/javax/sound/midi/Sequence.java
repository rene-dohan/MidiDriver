package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import java.util.Vector;

/**
 * Represents MIDI Sequence
 *
 * @author K.Shoji
 */
public class Sequence {
	public static final float PPQ = 0.0f;
	public static final float SMPTE_24 = 24.0f;
	public static final float SMPTE_25 = 25.0f;
	public static final float SMPTE_30 = 30.0f;
	public static final float SMPTE_30DROP = 29.969999313354492f;

	protected float divisionType;
	protected int resolution;
	protected Vector<Track> tracks;

	private static final float[] SUPPORTING_DIVISION_TYPES = {PPQ, SMPTE_24, SMPTE_25, SMPTE_30, SMPTE_30DROP};

	/**
	 * Check if the divisionType supported
	 * @param divisionType the divisionType
	 * @return true if the specified divisionType is supported
	 */
	private static boolean isSupportingDivisionType(final float divisionType) {
		for (final float supportingDivisionType : SUPPORTING_DIVISION_TYPES) {
			if (divisionType == supportingDivisionType) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Get the array of {@link Track}s
     *
	 * @return array of tracks
	 */
    @NonNull
    public Track[] getTracks() {
		final Track[] track = new Track[tracks.size()];
		tracks.toArray(track);
		return track;
	}

}

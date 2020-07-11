package jp.kshoji.javax.sound.midi;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents MIDI Track
 * 
 * @author K.Shoji
 */
public class Track {
    private static final byte[] END_OF_TRACK = { -1, 47, 0 };
    private static final Track[] emptyTracks = {};

    private final List<MidiEvent> events = new ArrayList<MidiEvent>();

	/**
	 * {@link Comparator} for MIDI data sorting
	 */
	static final Comparator<MidiEvent> midiEventComparator = new Comparator<MidiEvent>() {
		@Override
		public int compare(MidiEvent lhs, MidiEvent rhs) {
			// sort by tick
			final int tickDifference = (int) (lhs.getTick() - rhs.getTick());
			if (tickDifference != 0) {
				return tickDifference * 256;
			}

            byte[] lhsMessage = lhs.getMessage().getMessage();
            byte[] rhsMessage = rhs.getMessage().getMessage();

            // apply zero if message is empty
            if (lhsMessage == null || lhsMessage.length < 1) {
                lhsMessage = new byte[] {0};
            }
            if (rhsMessage == null || rhsMessage.length < 1) {
                rhsMessage = new byte[] {0};
            }

			// same timing
			// sort by the MIDI data priority order, as:
			// system message > control messages > note on > note off
			// swap the priority of note on, and note off
			int lhsInt = lhsMessage[0] & 0xf0;
			int rhsInt = rhsMessage[0] & 0xf0;

			if ((lhsInt & 0x90) == 0x80) {
				lhsInt |= 0x10;
			} else {
				lhsInt &= ~0x10;
			}
			if ((rhsInt & 0x90) == 0x80) {
				rhsInt |= 0x10;
			} else {
				rhsInt &= ~0x10;
			}

			return -(lhsInt - rhsInt);
		}
	};

	/**
	 * Utilities for {@link Track}
	 * 
	 * @author K.Shoji
	 */
	public static class TrackUtils {

		/**
		 * Sort the {@link Track}'s {@link MidiEvent}, order by tick and events
		 * 
		 * @param track the Track
		 */
		public static void sortEvents(@NonNull final Track track) {
			synchronized (track.events) {
				// remove all of END_OF_TRACK
				final Collection<MidiEvent> filtered = new ArrayList<MidiEvent>();
				for (final MidiEvent event : track.events) {
					if (!Arrays.equals(END_OF_TRACK, event.getMessage().getMessage())) {
						filtered.add(event);
					}
				}
				track.events.clear();
				track.events.addAll(filtered);
				
				// sort the events
				Collections.sort(track.events, midiEventComparator);
				
				// add END_OF_TRACK to last
				if (track.events.isEmpty()) {
					track.events.add(new MidiEvent(new MetaMessage(END_OF_TRACK), 0));
				} else {
					track.events.add(new MidiEvent(new MetaMessage(END_OF_TRACK), track.events.get(track.events.size() - 1).getTick() + 1));
				}
			}
		}
	}

	/**
	 * Get length of ticks for this {@link Track}
	 * 
	 * @return the length of ticks
	 */
	public long ticks() {
		TrackUtils.sortEvents(this);

		synchronized (events) {
			if (events.isEmpty()) {
				return 0L;
			}

			return events.get(events.size() - 1).getTick();
		}
	}
}
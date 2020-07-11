package jp.kshoji.javax.sound.midi;

/**
 * Represents MIDI Voice Status
 *
 * @author K.Shoji
 */
public class VoiceStatus {

    /**
     * Indicates the voice is active or not
     */
    public boolean active;

    /**
     * The channel number 0-15
     */
    public int channel;

    /**
     * The note number 0-127
     */
    public int note;

}

package cn.sherlock.javax.sound.sampled;
/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

/**
 * <code>AudioFormat</code> is the class that specifies a particular arrangement of data in a sound stream.
 * By examining the information stored in the audio format, you can discover how to interpret the bits in the
 * binary sound data.
 * <p>
 * Every data line has an audio format associated with its data stream. The audio format of a source (playback) data line indicates
 * what kind of data the data line expects to receive for output.  For a target (capture) data line, the audio format specifies the kind
 * of the data that can be read from the line.
 * Sound files also have audio formats, of course.  The AudioFormat
 * class encapsulates an <code>AudioFormat</code> in addition to other,
 * file-specific information.  Similarly, an <code>{@link AudioInputStream}</code> has an
 * <code>AudioFormat</code>.
 * <p>
 * The <code>AudioFormat</code> class accommodates a number of common sound-file encoding techniques, including
 * pulse-code modulation (PCM), mu-law encoding, and a-law encoding.  These encoding techniques are predefined,
 * but service providers can create new encoding types.
 * The encoding that a specific format uses is named by its <code>encoding</code> field.
 *<p>
 * In addition to the encoding, the audio format includes other properties that further specify the exact
 * arrangement of the data.
 * These include the number of channels, sample rate, sample size, byte order, frame rate, and frame size.
 * Sounds may have different numbers of audio channels: one for mono, two for stereo.
 * The sample rate measures how many "snapshots" (samples) of the sound pressure are taken per second, per channel.
 * (If the sound is stereo rather than mono, two samples are actually measured at each instant of time: one for the left channel,
 * and another for the right channel; however, the sample rate still measures the number per channel, so the rate is the same
 * regardless of the number of channels.   This is the standard use of the term.)
 * The sample size indicates how many bits are used to store each snapshot; 8 and 16 are typical values.
 * For 16-bit samples (or any other sample size larger than a byte),
 * byte order is important; the bytes in each sample are arranged in
 * either the "little-endian" or "big-endian" style.
 * For encodings like PCM, a frame consists of the set of samples for all channels at a given
 * point in time, and so the size of a frame (in bytes) is always equal to the size of a sample (in bytes) times
 * the number of channels.  However, with some other sorts of encodings a frame can contain
 * a bundle of compressed data for a whole series of samples, as well as additional, non-sample
 * data.  For such encodings, the sample rate and sample size refer to the data after it is decoded into PCM,
 * and so they are completely different from the frame rate and frame size.
 *
 * <p>An <code>AudioFormat</code> object can include a set of
 * properties. A property is a pair of key and value: the key
 * is of type <code>String</code>, the associated property
 * value is an arbitrary object. Properties specify
 * additional format specifications, like the bit rate for
 * compressed formats. Properties are mainly used as a means
 * to transport additional information of the audio format
 * to and from the service providers. Therefore, properties
 * are ignored in the matches#AudioFormat method.
 * However, methods which rely on the installed service
 * providers, like may consider
 * properties, depending on the respective service provider
 * implementation.
 *
 * <p>The following table lists some common properties which
 * service providers should use, if applicable:
 *
 * <table border=0>
 *  <caption>Audio Format Properties</caption>
 *  <tr>
 *   <th>Property key</th>
 *   <th>Value type</th>
 *   <th>Description</th>
 *  </tr>
 *  <tr>
 *   <td>&quot;bitrate&quot;</td>
 *   <td>{@link Integer Integer}</td>
 *   <td>average bit rate in bits per second</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;vbr&quot;</td>
 *   <td>{@link Boolean Boolean}</td>
 *   <td><code>true</code>, if the file is encoded in variable bit
 *       rate (VBR)</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;quality&quot;</td>
 *   <td>{@link Integer Integer}</td>
 *   <td>encoding/conversion quality, 1..100</td>
 *  </tr>
 * </table>
 *
 * <p>Vendors of service providers (plugins) are encouraged
 * to seek information about other already established
 * properties in third party plugins, and follow the same
 * conventions.
 *
 * @author Kara Kytle
 * @author Florian Bomers
 * @see AudioInputStream#getFormat
 * @since 1.3
 */
public class AudioFormat {

    public static final int UNSPECIFIED_FRAME_SIZE = -1;

    public static final AudioFormat MONO_FORMAT = new AudioFormat(22050, 16, 1);
    public static final AudioFormat STEREO_FORMAT = new AudioFormat(44100, 16, 2);

    /**
     * The number of samples played or recorded per second, for sounds that have this format.
     */
    protected float sampleRate;

    /**
     * The number of bits in each sample of a sound that has this format.
     */
    protected int sampleSizeInBits;

    /**
     * The number of audio channels in this format (1 for mono, 2 for stereo).
     */
    protected int channels;

    /**
     * The number of bytes in each frame of a sound that has this format.
     */
    protected int frameSize;

    /**
     * The number of frames played or recorded per second, for sounds that have this format.
     */
    protected float frameRate;


    /**
     * Constructs an <code>AudioFormat</code> with a linear PCM encoding and
     * the given parameters.  The frame size is set to the number of bytes
     * required to contain one sample from each channel, and the frame rate
     * is set to the sample rate.
     * @param sampleRate                the number of samples per second
     * @param sampleSizeInBits  the number of bits in each sample
     * @param channels                  the number of channels (1 for mono, 2 for stereo, and so on)
     */
    private AudioFormat(float sampleRate, int sampleSizeInBits, int channels) {
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = ((sampleSizeInBits + 7) / 8) * channels;
        this.frameRate = sampleRate;
    }

    /**
     * Obtains the sample rate.
     * For compressed formats, the return value is the sample rate of the uncompressed
     * audio data.
     * When this AudioFormat is used for queries or capabilities (e.g. {@link
     * DataLine.Info#getFormats() DataLine.Info.getFormats}), a sample rate of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any sample rate is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the sample rate is not defined for this audio format.
     * @return the number of samples per second,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getFrameRate()
     */
    public float getSampleRate() {
        return sampleRate;
    }

    /**
     * Obtains the size of a sample.
     * For compressed formats, the return value is the sample size of the
     * uncompressed audio data.
     * When this AudioFormat is used for queries or capabilities (e.g. {@link
     * DataLine.Info#getFormats() DataLine.Info.getFormats}), a sample size of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any sample size is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the sample size is not defined for this audio format.
     * @return the number of bits in each sample,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getFrameSize()
     */
    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    /**
     * Obtains the number of channels.
     * When this AudioFormat is used for queries
     * AudioSystem.isConversionSupported}) or capabilities (e.g. {@link
     * DataLine.Info#getFormats() DataLine.Info.getFormats}), a return value of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any (positive) number of channels is
     * acceptable.
     * @return The number of channels (1 for mono, 2 for stereo, etc.)
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Obtains the frame size in bytes.
     * When this AudioFormat is used for queries or capabilities (e.g. {@link
     * DataLine.Info#getFormats() DataLine.Info.getFormats}), a frame size of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any frame size is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the frame size is not defined for this audio format.
     * @return the number of bytes per frame,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getSampleSizeInBits()
     */
    public int getFrameSize() {
        return frameSize;
    }

    /**
     * Obtains the frame rate in frames per second.
     * When this AudioFormat is used for queries or capabilities, a frame rate of
     * <code>AudioSystem.NOT_SPECIFIED</code> means that any frame rate is
     * acceptable. <code>AudioSystem.NOT_SPECIFIED</code> is also returned when
     * the frame rate is not defined for this audio format.
     * @return the number of frames per second,
     * or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     * @see #getSampleRate()
     */
    public float getFrameRate() {
        return frameRate;
    }


}

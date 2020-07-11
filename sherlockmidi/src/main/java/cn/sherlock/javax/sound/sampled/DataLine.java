/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package cn.sherlock.javax.sound.sampled;

import java.util.Arrays;

/**
 * <code>DataLine</code> adds media-related functionality to its
 * superinterface, <code>{@link Line}</code>.  This functionality includes
 * transport-control methods that start, stop, drain, and flush
 * the audio data that passes through the line.  A data line can also
 * report the current position, volume, and audio format of the media.
 * Data lines are used for output of audio by means of the
 * subinterfaces <code>{@link SourceDataLine}</code> or
 * <code>{@link Clip}</code>, which allow an application program to write data.  Similarly,
 * audio input is handled by the subinterface <code>{@link TargetDataLine}</code>,
 * which allows data to be read.
 * <p>
 * A data line has an internal buffer in which
 * the incoming or outgoing audio data is queued.  The
 * <code>{@link #drain()}</code> method blocks until this internal buffer
 * becomes empty, usually because all queued data has been processed.  The
 * <code>{@link #flush()}</code> method discards any available queued data
 * from the internal buffer.
 * <p>
 * A data line produces <code>{@link LineEvent.Type#START START}</code> and
 * <code>{@link LineEvent.Type#STOP STOP}</code> events whenever
 * it begins or ceases active presentation or capture of data.  These events
 * can be generated in response to specific requests, or as a result of
 * less direct state changes.  For example, if <code>{@link #start()}</code> is called
 * on an inactive data line, and data is available for capture or playback, a
 * <code>START</code> event will be generated shortly, when data playback
 * or capture actually begins.  Or, if the flow of data to an active data
 * line is constricted so that a gap occurs in the presentation of data,
 * a <code>STOP</code> event is generated.
 * <p>
 * Mixers often support synchronized control of multiple data lines.
 * Synchronization can be established through the Mixer interface's
 * <code>{@link Mixer#synchronize synchronize}</code> method.
 * See the description of the <code>{@link Mixer Mixer}</code> interface
 * for a more complete description.
 *
 * @author Kara Kytle
 * @see LineEvent
 * @since 1.3
 */
public interface DataLine extends Line {


    /**
     * Allows a line to engage in data I/O.  If invoked on a line
     * that is already running, this method does nothing.  Unless the data in
     * the buffer has been flushed, the line resumes I/O starting
     * with the first frame that was unprocessed at the time the line was
     * stopped. When audio capture or playback starts, a
     * <code>{@link LineEvent.Type#START START}</code> event is generated.
     *
     * @see LineEvent
     */
    void start();

    /**
     * Indicates whether the line is engaging in active I/O (such as playback
     * or capture).  When an inactive line becomes active, it sends a
     * <code>{@link LineEvent.Type#START START}</code> event to its listeners.  Similarly, when
     * an active line becomes inactive, it sends a
     * <code>{@link LineEvent.Type#STOP STOP}</code> event.
     * @return <code>true</code> if the line is actively capturing or rendering
     * sound, otherwise <code>false</code>
     * @see #isOpen
     * @see #addLineListener
     * @see #removeLineListener
     * @see LineEvent
     * @see LineListener
     */
    boolean isActive();

    /**
     * Obtains the current format (encoding, sample rate, number of channels,
     * etc.) of the data line's audio data.
     *
     * <p>If the line is not open and has never been opened, it returns
     * the default format. The default format is an implementation
     * specific audio format, or, if the <code>DataLine.Info</code>
     * object, which was used to retrieve this <code>DataLine</code>,
     * specifies at least one fully qualified audio format, the
     * last one will be used as the default format. Opening the
     * line with a specific audio format (e.g.
     * {@link SourceDataLine#open(AudioFormat)}) will override the
     * default format.
     *
     * @return current audio data format
     * @see AudioFormat
     */
    AudioFormat getFormat();

    /**
     * Obtains the maximum number of bytes of data that will fit in the data line's
     * internal buffer.  For a source data line, this is the size of the buffer to
     * which data can be written.  For a target data line, it is the size of
     * the buffer from which data can be read.  Note that
     * the units used are bytes, but will always correspond to an integral
     * number of sample frames of audio data.
     *
     * @return the size of the buffer in bytes
     */
    int getBufferSize();


    /**
     * Besides the class information inherited from its superclass,
     * <code>DataLine.Info</code> provides additional information specific to data lines.
     * This information includes:
     * <ul>
     * <li> the audio formats supported by the data line
     * <li> the minimum and maximum sizes of its internal buffer
     * </ul>
     * Because a <code>Line.Info</code> knows the class of the line its describes, a
     * <code>DataLine.Info</code> object can describe <code>DataLine</code>
     * subinterfaces such as <code>{@link SourceDataLine}</code>,
     * <code>{@link TargetDataLine}</code>, and <code>{@link Clip}</code>.
     * You can query a mixer for lines of any of these types, passing an appropriate
     * instance of <code>DataLine.Info</code> as the argument to a method such as
     * <code>{@link Mixer#getLine Mixer.getLine(Line.Info)}</code>.
     *
     * @see Line.Info
     * @author Kara Kytle
     * @since 1.3
     */
    class Info extends Line.Info {

        private final AudioFormat[] formats;
        private final int minBufferSize;
        private final int maxBufferSize;

        /**
         * Constructs a data line's info object from the specified information,
         * which includes a set of supported audio formats and a range for the buffer size.
         * This constructor is typically used by mixer implementations
         * when returning information about a supported line.
         *
         * @param lineClass the class of the data line described by the info object
         * @param formats set of formats supported
         * @param minBufferSize minimum buffer size supported by the data line, in bytes
         * @param maxBufferSize maximum buffer size supported by the data line, in bytes
         */
        public Info(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {

            super(lineClass);

            if (formats == null) {
                this.formats = new AudioFormat[0];
            } else {
                this.formats = Arrays.copyOf(formats, formats.length);
            }

            this.minBufferSize = minBufferSize;
            this.maxBufferSize = maxBufferSize;
        }


        /**
         * Constructs a data line's info object from the specified information,
         * which includes a single audio format and a desired buffer size.
         * This constructor is typically used by an application to
         * describe a desired line.
         *
         * @param lineClass the class of the data line described by the info object
         * @param format desired format
         * @param bufferSize desired buffer size in bytes
         */
        public Info(Class<?> lineClass, AudioFormat format, int bufferSize) {

            super(lineClass);

            if (format == null) {
                this.formats = new AudioFormat[0];
            } else {
                this.formats = new AudioFormat[]{format};
            }

            this.minBufferSize = bufferSize;
            this.maxBufferSize = bufferSize;
        }


        /**
         * Constructs a data line's info object from the specified information,
         * which includes a single audio format.
         * This constructor is typically used by an application to
         * describe a desired line.
         *
         * @param lineClass the class of the data line described by the info object
         * @param format desired format
         */
        public Info(Class<?> lineClass, AudioFormat format) {
            this(lineClass, format, AudioSystem.NOT_SPECIFIED);
        }


        /**
         * Obtains a set of audio formats supported by the data line.
         * Note that <code>isFormatSupported(AudioFormat)</code> might return
         * <code>true</code> for certain additional formats that are missing from
         * the set returned by <code>getFormats()</code>.  The reverse is not
         * the case: <code>isFormatSupported(AudioFormat)</code> is guaranteed to return
         * <code>true</code> for all formats returned by <code>getFormats()</code>.
         *
         * Some fields in the AudioFormat instances can be set to
         * {@link cn.sherlock.javax.sound.sampled.AudioSystem#NOT_SPECIFIED NOT_SPECIFIED}
         * if that field does not apply to the format,
         * or if the format supports a wide range of values for that field.
         * For example, a multi-channel device supporting up to
         * 64 channels, could set the channel field in the
         * <code>AudioFormat</code> instances returned by this
         * method to <code>NOT_SPECIFIED</code>.
         *
         * @return a set of supported audio formats.
         * @see #isFormatSupported(AudioFormat)
         */
        public AudioFormat[] getFormats() {
            return Arrays.copyOf(formats, formats.length);
        }

        /**
         * Indicates whether this data line supports a particular audio format.
         * The default implementation of this method simply returns <code>true</code> if
         * the specified format matches any of the supported formats.
         *
         * @param format the audio format for which support is queried.
         * @return <code>true</code> if the format is supported, otherwise <code>false</code>
         * @see #getFormats
         * @see AudioFormat#matches
         */
        public boolean isFormatSupported(AudioFormat format) {

            for (int i = 0; i < formats.length; i++) {
                if (format.matches(formats[i])) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Obtains the minimum buffer size supported by the data line.
         * @return minimum buffer size in bytes, or <code>AudioSystem.NOT_SPECIFIED</code>
         */
        public int getMinBufferSize() {
            return minBufferSize;
        }


        /**
         * Obtains the maximum buffer size supported by the data line.
         * @return maximum buffer size in bytes, or <code>AudioSystem.NOT_SPECIFIED</code>
         */
        public int getMaxBufferSize() {
            return maxBufferSize;
        }


        /**
         * Determines whether the specified info object matches this one.
         * To match, the superclass match requirements must be met.  In
         * addition, this object's minimum buffer size must be at least as
         * large as that of the object specified, its maximum buffer size must
         * be at most as large as that of the object specified, and all of its
         * formats must match formats supported by the object specified.
         * @return <code>true</code> if this object matches the one specified,
         * otherwise <code>false</code>.
         */
        public boolean matches(Line.Info info) {

            if (! (super.matches(info)) ) {
                return false;
            }

            Info dataLineInfo = (Info)info;

            // treat anything < 0 as NOT_SPECIFIED
            // demo code in old Java Sound Demo used a wrong buffer calculation
            // that would lead to arbitrary negative values
            if ((getMaxBufferSize() >= 0) && (dataLineInfo.getMaxBufferSize() >= 0)) {
                if (getMaxBufferSize() > dataLineInfo.getMaxBufferSize()) {
                    return false;
                }
            }

            if ((getMinBufferSize() >= 0) && (dataLineInfo.getMinBufferSize() >= 0)) {
                if (getMinBufferSize() < dataLineInfo.getMinBufferSize()) {
                    return false;
                }
            }

            AudioFormat[] localFormats = getFormats();

            if (localFormats != null) {

                for (int i = 0; i < localFormats.length; i++) {
                    if (! (localFormats[i] == null) ) {
                        if (! (dataLineInfo.isFormatSupported(localFormats[i])) ) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        /**
         * Obtains a textual description of the data line info.
         * @return a string description
         */
        public String toString() {

            StringBuffer buf = new StringBuffer();

            if ( (formats.length == 1) && (formats[0] != null) ) {
                buf.append(" supporting format " + formats[0]);
            } else if (getFormats().length > 1) {
                buf.append(" supporting " + getFormats().length + " audio formats");
            }

            if ( (minBufferSize != AudioSystem.NOT_SPECIFIED) && (maxBufferSize != AudioSystem.NOT_SPECIFIED) ) {
                buf.append(", and buffers of " + minBufferSize + " to " + maxBufferSize + " bytes");
            } else if ( (minBufferSize != AudioSystem.NOT_SPECIFIED) && (minBufferSize > 0) ) {
                buf.append(", and buffers of at least " + minBufferSize + " bytes");
            } else if (maxBufferSize != AudioSystem.NOT_SPECIFIED) {
                buf.append(", and buffers of up to " + minBufferSize + " bytes");
            }

            return super.toString() + buf;
        }
    } // class Info

} // interface DataLine

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

} // interface DataLine

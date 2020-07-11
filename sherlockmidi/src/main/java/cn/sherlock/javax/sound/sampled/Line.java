package cn.sherlock.javax.sound.sampled;
/*
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


/**
 * The <code>Line</code> interface represents a mono or multi-channel
 * audio feed. A line is an element of the digital audio
 * "pipeline," such as a mixer, an input or output port,
 * or a data path into or out of a mixer.
 * <p>
 * A line can have controls, such as gain, pan, and reverb.
 * The controls themselves are instances of classes that extend the
 * base <code>{@link Control}</code> class.
 * The <code>Line</code> interface provides two accessor methods for
 * obtaining the line's controls: <code>{@link #getControls getControls}</code> returns the
 * entire set, and <code>{@link #getControl getControl}</code> returns a single control of
 * specified type.
 * <p>
 * Lines exist in various states at different times.  When a line opens, it reserves system
 * resources for itself, and when it closes, these resources are freed for
 * other objects or applications. The <code>{@link #isOpen()}</code> method lets
 * you discover whether a line is open or closed.
 * An open line need not be processing data, however.  Such processing is
 * typically initiated by subinterface methods such as
 * <code>{@link SourceDataLine#write SourceDataLine.write}</code> and
 * <code>{@link TargetDataLine#read TargetDataLine.read}</code>.
 *<p>
 * You can register an object to receive notifications whenever the line's
 * state changes.  The object must implement the <code>{@link LineListener}</code>
 * interface, which consists of the single method
 * <code>{@link LineListener#update update}</code>.
 * This method will be invoked when a line opens and closes (and, if it's a
 * {@link DataLine}, when it starts and stops).
 *<p>
 * An object can be registered to listen to multiple lines.  The event it
 * receives in its <code>update</code> method will specify which line created
 * the event, what type of event it was
 * (<code>OPEN</code>, <code>CLOSE</code>, <code>START</code>, or <code>STOP</code>),
 * and how many sample frames the line had processed at the time the event occurred.
 * <p>
 * Certain line operations, such as open and close, can generate security
 * exceptions if invoked by unprivileged code when the line is a shared audio
 * resource.
 *
 * @author Kara Kytle
 *
 * @see LineEvent
 * @since 1.3
 */
public interface Line extends cn.sherlock.javax.sound.sampled.AutoCloseable {


    /**
     * Closes the line, indicating that any system resources
     * in use by the line can be released.  If this operation
     * succeeds, the line is marked closed and a <code>CLOSE</code> event is dispatched
     * to the line's listeners.
     * @throws SecurityException if the line cannot be
     * closed due to security restrictions.
     *
     * @see #isOpen
     * @see LineEvent
     */
    void close();



    /**
     * Indicates whether the line is open, meaning that it has reserved
     * system resources and is operational, although it might not currently be
     * playing or capturing sound.
     * @return <code>true</code> if the line is open, otherwise <code>false</code>
     *
     * @see #close()
     */
    boolean isOpen();

} // interface Line

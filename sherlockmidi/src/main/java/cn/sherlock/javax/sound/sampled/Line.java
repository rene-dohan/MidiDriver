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


    /**
     * A <code>Line.Info</code> object contains information about a line.
     * The only information provided by <code>Line.Info</code> itself
     * is the Java class of the line.
     * A subclass of <code>Line.Info</code> adds other kinds of information
     * about the line.  This additional information depends on which <code>Line</code>
     * subinterface is implemented by the kind of line that the <code>Line.Info</code>
     * subclass describes.
     * <p>
     * A <code>Line.Info</code> can be retrieved using various methods of
     * the <code>Line</code>, <code>Mixer</code>, and <code>AudioSystem</code>
     * interfaces.  Other such methods let you pass a <code>Line.Info</code> as
     * an argument, to learn whether lines matching the specified configuration
     * are available and to obtain them.
     *
     * @author Kara Kytle
     *
     * @see Mixer#getSourceLineInfo
     * @see Mixer#getTargetLineInfo
     * @see Mixer#getLine <code>Mixer.getLine(Line.Info)</code>
     * @see Mixer#getSourceLineInfo(Info) <code>Mixer.getSourceLineInfo(Line.Info)</code>
     * @see Mixer#getSourceLineInfo(Info) <code>Mixer.getTargetLineInfo(Line.Info)</code>
     * @see Mixer#isLineSupported <code>Mixer.isLineSupported(Line.Info)</code>
     * @see AudioSystem#getLine <code>AudioSystem.getLine(Line.Info)</code>
     * @see AudioSystem#getSourceLineInfo <code>AudioSystem.getSourceLineInfo(Line.Info)</code>
     * @see AudioSystem#getTargetLineInfo <code>AudioSystem.getTargetLineInfo(Line.Info)</code>
     * @see AudioSystem#isLineSupported <code>AudioSystem.isLineSupported(Line.Info)</code>
     * @since 1.3
     */
    class Info {

        /**
         * The class of the line described by the info object.
         */
        private final Class lineClass;


        /**
         * Constructs an info object that describes a line of the specified class.
         * This constructor is typically used by an application to
         * describe a desired line.
         * @param lineClass the class of the line that the new Line.Info object describes
         */
        public Info(Class<?> lineClass) {

            if (lineClass == null) {
                this.lineClass = Line.class;
            } else {
                this.lineClass = lineClass;
            }
        }



        /**
         * Obtains the class of the line that this Line.Info object describes.
         * @return the described line's class
         */
        public Class<?> getLineClass() {
            return lineClass;
        }


        /**
         * Indicates whether the specified info object matches this one.
         * To match, the specified object must be identical to or
         * a special case of this one.  The specified info object
         * must be either an instance of the same class as this one,
         * or an instance of a sub-type of this one.  In addition, the
         * attributes of the specified object must be compatible with the
         * capabilities of this one.  Specifically, the routing configuration
         * for the specified info object must be compatible with that of this
         * one.
         * Subclasses may add other criteria to determine whether the two objects
         * match.
         *
         * @param info the info object which is being compared to this one
         * @return <code>true</code> if the specified object matches this one,
         * <code>false</code> otherwise
         */
        public boolean matches(Info info) {

            // $$kk: 08.30.99: is this backwards?
            // dataLine.matches(targetDataLine) == true: targetDataLine is always dataLine
            // targetDataLine.matches(dataLine) == false
            // so if i want to make sure i get a targetDataLine, i need:
            // targetDataLine.matches(prospective_match) == true
            // => prospective_match may be other things as well, but it is at least a targetDataLine
            // targetDataLine defines the requirements which prospective_match must meet.


            // "if this Class object represents a declared class, this method returns
            // true if the specified Object argument is an instance of the represented
            // class (or of any of its subclasses)"
            // GainControlClass.isInstance(MyGainObj) => true
            // GainControlClass.isInstance(MySpecialGainInterfaceObj) => true

            // this_class.isInstance(that_object)       => that object can by cast to this class
            //                                                                          => that_object's class may be a subtype of this_class
            //                                                                          => that may be more specific (subtype) of this

            // "If this Class object represents an interface, this method returns true
            // if the class or any superclass of the specified Object argument implements
            // this interface"
            // GainControlClass.isInstance(MyGainObj) => true
            // GainControlClass.isInstance(GenericControlObj) => may be false
            // => that may be more specific

            if (! (this.getClass().isInstance(info)) ) {
                return false;
            }


            // this.isAssignableFrom(that)  =>  this is same or super to that
            //                                                          =>      this is at least as general as that
            //                                                          =>      that may be subtype of this

            return getLineClass().isAssignableFrom(info.getLineClass());
        }


        /**
         * Obtains a textual description of the line info.
         * @return a string description
         */
        public String toString() {

            String fullPackagePath = "javax.sound.sampled.";
            String initialString = getLineClass().toString();
            String finalString;

            int index = initialString.indexOf(fullPackagePath);

            if (index != -1) {
                finalString = initialString.substring(0, index) + initialString.substring( (index + fullPackagePath.length()));
            } else {
                finalString = initialString;
            }

            return finalString;
        }

    } // class Info

} // interface Line

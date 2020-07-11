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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * An instance of the <code>AudioFileFormat</code> class describes
 * an audio file, including the file type, the file's length in bytes,
 * the length in sample frames of the audio data contained in the file,
 * and the format of the audio data.
 * <p>
 * The <code>{@link AudioSystem}</code> class includes methods for determining the format
 * of an audio file, obtaining an audio input stream from an audio file, and
 * writing an audio file from an audio input stream.
 *
 * <p>An <code>AudioFileFormat</code> object can
 * include a set of properties. A property is a pair of key and value:
 * the key is of type <code>String</code>, the associated property
 * value is an arbitrary object.
 * Properties specify additional informational
 * meta data (like a author, copyright, or file duration).
 * Properties are optional information, and file reader and file
 * writer implementations are not required to provide or
 * recognize properties.
 *
 * <p>The following table lists some common properties that should
 * be used in implementations:
 *
 * <table border=1>
 *  <caption>Audio File Format Properties</caption>
 *  <tr>
 *   <th>Property key</th>
 *   <th>Value type</th>
 *   <th>Description</th>
 *  </tr>
 *  <tr>
 *   <td>&quot;duration&quot;</td>
 *   <td>{@link Long Long}</td>
 *   <td>playback duration of the file in microseconds</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;author&quot;</td>
 *   <td>{@link String String}</td>
 *   <td>name of the author of this file</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;title&quot;</td>
 *   <td>{@link String String}</td>
 *   <td>title of this file</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;copyright&quot;</td>
 *   <td>{@link String String}</td>
 *   <td>copyright message</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;date&quot;</td>
 *   <td>{@link java.util.Date Date}</td>
 *   <td>date of the recording or release</td>
 *  </tr>
 *  <tr>
 *   <td>&quot;comment&quot;</td>
 *   <td>{@link String String}</td>
 *   <td>an arbitrary text</td>
 *  </tr>
 * </table>
 *
 *
 * @author David Rivas
 * @author Kara Kytle
 * @author Florian Bomers
 * @see AudioInputStream
 * @since 1.3
 */
public class AudioFileFormat {


    // INSTANCE VARIABLES


    /**
     * File type.
     */
    private Type type;

    /**
     * File length in bytes
     */
    private int byteLength;

    /**
     * Format of the audio data contained in the file.
     */
    private AudioFormat format;

    /**
     * Audio data length in sample frames
     */
    private int frameLength;


    /** The set of properties */
    private HashMap<String, Object> properties;


    /**
     * Constructs an audio file format object.
     * This protected constructor is intended for use by providers of file-reading
     * services when returning information about an audio file or about supported audio file
     * formats.
     * @param type the type of the audio file
     * @param byteLength the length of the file in bytes, or <code>AudioSystem.NOT_SPECIFIED</code>
     * @param format the format of the audio data contained in the file
     * @param frameLength the audio data length in sample frames, or <code>AudioSystem.NOT_SPECIFIED</code>
     *
     */
    protected AudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {

        this.type = type;
        this.byteLength = byteLength;
        this.format = format;
        this.frameLength = frameLength;
        this.properties = null;
    }


    /**
     * Constructs an audio file format object.
     * This public constructor may be used by applications to describe the
     * properties of a requested audio file.
     * @param type the type of the audio file
     * @param format the format of the audio data contained in the file
     * @param frameLength the audio data length in sample frames, or <code>AudioSystem.NOT_SPECIFIED</code>
     */
    public AudioFileFormat(Type type, AudioFormat format, int frameLength) {


        this(type,AudioSystem.NOT_SPECIFIED,format,frameLength);
    }


    /**
     * Obtains the format of the audio data contained in the audio file.
     * @return the audio data format
     */
    public AudioFormat getFormat() {
        return format;
    }


    /**
     * Provides a string representation of the file format.
     * @return the file format as a string
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();

        //$$fb2002-11-01: fix for 4672864: AudioFileFormat.toString() throws unexpected NullPointerException
        if (type != null) {
            buf.append(type.toString() + " (." + type.getExtension() + ") file");
        } else {
            buf.append("unknown file format");
        }

        if (byteLength != AudioSystem.NOT_SPECIFIED) {
            buf.append(", byte length: " + byteLength);
        }

        buf.append(", data format: " + format);

        if (frameLength != AudioSystem.NOT_SPECIFIED) {
            buf.append(", frame length: " + frameLength);
        }

        return new String(buf);
    }


    /**
     * An instance of the <code>Type</code> class represents one of the
     * standard types of audio file.  Static instances are provided for the
     * common types.
     */
    public static class Type {

        // FILE FORMAT TYPE DEFINES

        /**
         * Specifies a WAVE file.
         */
        public static final Type WAVE = new Type("WAVE", "wav");


        // INSTANCE VARIABLES

        /**
         * File type name.
         */
        private final String name;

        /**
         * File type extension.
         */
        private final String extension;


        // CONSTRUCTOR

        /**
         * Constructs a file type.
         * @param name the string that names the file type
         * @param extension the string that commonly marks the file type
         * without leading dot.
         */
        public Type(String name, String extension) {

            this.name = name;
            this.extension = extension;
        }


        // METHODS

        /**
         * Finalizes the equals method
         */
        public final boolean equals(Object obj) {
            if (toString() == null) {
                return (obj != null) && (obj.toString() == null);
            }
            if (obj instanceof Type) {
                return toString().equals(obj.toString());
            }
            return false;
        }

        /**
         * Finalizes the hashCode method
         */
        public final int hashCode() {
            if (toString() == null) {
                return 0;
            }
            return toString().hashCode();
        }

        /**
         * Provides the file type's name as the <code>String</code> representation
         * of the file type.
         * @return the file type's name
         */
        public final String toString() {
            return name;
        }

        /**
         * Obtains the common file name extension for this file type.
         * @return file type extension
         */
        public String getExtension() {
            return extension;
        }

    } // class Type

} // class AudioFileFormat

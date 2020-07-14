/*
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package cn.sherlock.javax.sound.sampled.spi;

import java.io.InputStream;
import java.io.IOException;

import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Provider for audio file reading services.  Classes providing concrete
 * implementations can parse the format information from one or more types of
 * audio file, and can produce audio input streams from files of these types.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class AudioFileReader {

    /**
     * Obtains an audio input stream from the input stream provided.  The stream must
     * point to valid audio file data.  In general, audio file readers may
     * need to read some data from the stream before determining whether they
     * support it.  These parsers must
     * be able to mark the stream, read enough data to determine whether they
     * support the stream, and, if not, reset the stream's read pointer to its original
     * position.  If the input stream does not support this, this method may fail
     * with an <code>IOException</code>.
     * @param stream the input stream from which the <code>AudioInputStream</code> should be
     * constructed
     * @return an <code>AudioInputStream</code> object based on the audio file data contained
     * in the input stream.
     * @throws UnsupportedAudioFileException if the stream does not point to valid audio
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public abstract AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException;

}

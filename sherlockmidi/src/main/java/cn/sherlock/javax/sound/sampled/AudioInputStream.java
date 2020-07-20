package cn.sherlock.javax.sound.sampled;

/*
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

import android.support.annotation.NonNull;

import java.io.InputStream;
import java.io.IOException;


/**
 * An audio input stream is an input stream with a specified audio format and
 * length.  The length is expressed in sample frames, not bytes.
 * Several methods are provided for reading a certain number of bytes from
 * the stream, or an unspecified number of bytes.
 * The audio input stream keeps track  of the last byte that was read.
 * You can skip over an arbitrary number of bytes to get to a later position
 * for reading. An audio input stream may support marks.  When you set a mark,
 * the current position is remembered so that you can return to it later.
 * <p>
 * The <code>AudioSystem</code> class includes many methods that manipulate
 * <code>AudioInputStream</code> objects.
 * For example, the methods let you:
 * <ul>
 * <li> obtain an audio input stream from an external audio file, stream, or URL
 * <li> write an external file from an audio input stream
 * <li> convert an audio input stream to a different audio format
 * </ul>
 *
 * @author David Rivas
 * @author Kara Kytle
 * @author Florian Bomers
 *
 * @since 1.3
 */
public class AudioInputStream extends InputStream {

    /**
     * The <code>InputStream</code> from which this <code>AudioInputStream</code>
     * object was constructed.
     */
    private InputStream stream;

    /**
     * This stream's length, in sample frames.
     */
    protected long frameLength;

    /**
     * The size of each frame, in bytes.
     */
    protected int frameSize;

    /**
     * The current position in this stream, in sample frames (zero-based).
     */
    protected long framePos;

    /**
     * When the underlying stream could only return
     * a non-integral number of frames, store
     * the remainder in a temporary buffer
     */
    private byte[] pushBackBuffer = null;

    /**
     * number of valid bytes in the pushBackBuffer
     */
    private int pushBackLen = 0;


    /**
     * Constructs an audio input stream that has the requested format and length in sample frames,
     * using audio data from the specified input stream.
     * @param stream the stream on which this <code>AudioInputStream</code>
     * object is based
     * @param format the format of this stream's audio data
     * @param length the length in sample frames of the data in this stream
     */
    public AudioInputStream(InputStream stream, AudioFormat format, long length) {

        super();

        this.frameLength = length;
        this.frameSize = format.getFrameSize();

        // any frameSize that is not well-defined will
        // cause that this stream will be read in bytes
        if( this.frameSize == AudioFormat.UNSPECIFIED_FRAME_SIZE || frameSize <= 0) {
            this.frameSize = 1;
        }

        this.stream = stream;
        framePos = 0;
    }


//    /**
//     * Constructs an audio input stream that reads its data from the target
//     * data line indicated.  The format of the stream is the same as that of
//     * the target data line, and the length is AudioSystem#NOT_SPECIFIED.
//     * @param line the target data line from which this stream obtains its data.
//     * @see AudioSystem#NOT_SPECIFIED
//     */
//    public AudioInputStream(TargetDataLine line) {
//
//        TargetDataLineInputStream tstream = new TargetDataLineInputStream(line);
//        format = line.getFormat();
//        frameLength = AudioSystem.NOT_SPECIFIED;
//        frameSize = format.getFrameSize();
//
//        if( frameSize == AudioSystem.NOT_SPECIFIED || frameSize <= 0) {
//            frameSize = 1;
//        }
//        this.stream = tstream;
//        framePos = 0;
//        markpos = 0;
//    }


    /**
     * Reads the next byte of data from the audio input stream.  The audio input
     * stream's frame size must be one byte, or an <code>IOException</code>
     * will be thrown.
     *
     * @return the next byte of data, or -1 if the end of the stream is reached
     * @throws IOException if an input or output error occurs
     * @see #read(byte[], int, int)
     * @see #read(byte[])
     * @see #available
     * <p>
     */
    public int read() throws IOException {
        if( frameSize != 1 ) {
            throw new IOException("cannot read a single byte if frame size > 1");
        }

        byte[] data = new byte[1];
        int temp = read(data);
        if (temp <= 0) {
            // we have a weird situation if read(byte[]) returns 0!
            return -1;
        }
        return data[0] & 0xFF;
    }


    /**
     * Reads some number of bytes from the audio input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer. This method blocks until input data is
     * available, the end of the stream is detected, or an exception is thrown.
     * <p>This method will always read an integral number of frames.
     * If the length of the array is not an integral number
     * of frames, a maximum of <code>b.length - (b.length % frameSize)
     * </code> bytes will be read.
     *
     * @param b the buffer into which the data is read
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached
     * @throws IOException if an input or output error occurs
     * @see #read(byte[], int, int)
     * @see #read()
     * @see #available
     */
    public int read(@NonNull byte[] b) throws IOException {
        return read(b,0, b.length);
    }


    /**
     * Reads up to a specified maximum number of bytes of data from the audio
     * stream, putting them into the given byte array.
     * <p>This method will always read an integral number of frames.
     * If <code>len</code> does not specify an integral number
     * of frames, a maximum of <code>len - (len % frameSize)
     * </code> bytes will be read.
     *
     * @param b the buffer into which the data is read
     * @param off the offset, from the beginning of array <code>b</code>, at which
     * the data will be written
     * @param len the maximum number of bytes to read
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached
     * @throws IOException if an input or output error occurs
     * @see #read(byte[])
     * @see #read()
     * @see #skip
     * @see #available
     */
    public int read(@NonNull byte[] b, int off, int len) throws IOException {

        // make sure we don't read fractions of a frame.
        if( (len%frameSize) != 0 ) {
            len -= (len%frameSize);
            if (len == 0) {
                return 0;
            }
        }

        if (frameLength != AudioFormat.UNSPECIFIED_FRAME_SIZE) {
            if (framePos >= frameLength) {
                return -1;
            } else {

                // don't try to read beyond our own set length in frames
                if( (len/frameSize) > (frameLength-framePos) ) {
                    len = (int) (frameLength-framePos) * frameSize;
                }
            }
        }

        int bytesRead = 0;
        int thisOff = off;

        // if we've bytes left from last call to read(),
        // use them first
        if (pushBackLen > 0 && len >= pushBackLen) {
            System.arraycopy(pushBackBuffer, 0,
                             b, off, pushBackLen);
            thisOff += pushBackLen;
            len -= pushBackLen;
            bytesRead += pushBackLen;
            pushBackLen = 0;
        }

        int thisBytesRead = stream.read(b, thisOff, len);
        if (thisBytesRead == -1) {
            return -1;
        }
        if (thisBytesRead > 0) {
            bytesRead += thisBytesRead;
        }
        if (bytesRead > 0) {
            pushBackLen = bytesRead % frameSize;
            if (pushBackLen > 0) {
                // copy everything we got from the beginning of the frame
                // to our pushback buffer
                if (pushBackBuffer == null) {
                    pushBackBuffer = new byte[frameSize];
                }
                System.arraycopy(b, off + bytesRead - pushBackLen,
                                 pushBackBuffer, 0, pushBackLen);
                bytesRead -= pushBackLen;
            }
            // make sure to update our framePos
            framePos += bytesRead/frameSize;
        }
        return bytesRead;
    }


    /**
     * Skips over and discards a specified number of bytes from this
     * audio input stream.
     * @param n the requested number of bytes to be skipped
     * @return the actual number of bytes skipped
     * @throws IOException if an input or output error occurs
     * @see #read
     * @see #available
     */
    public long skip(long n) throws IOException {

        // make sure not to skip fractional frames
        if( (n%frameSize) != 0 ) {
            n -= (n%frameSize);
        }

        if (frameLength != AudioFormat.UNSPECIFIED_FRAME_SIZE) {
            // don't skip more than our set length in frames.
            if( (n/frameSize) > (frameLength-framePos) ) {
                n = (frameLength-framePos) * frameSize;
            }
        }
        long temp = stream.skip(n);

        // if no error, update our position.
        if( temp%frameSize != 0 ) {

            // Throw an IOException if we've skipped a fractional number of frames
            throw new IOException("Could not skip an integer number of frames.");
        }
        framePos += temp/frameSize;
        return temp;

    }


    /**
     * Returns the maximum number of bytes that can be read (or skipped over) from this
     * audio input stream without blocking.  This limit applies only to the next invocation of
     * a <code>read</code> or <code>skip</code> method for this audio input stream; the limit
     * can vary each time these methods are invoked.
     * Depending on the underlying stream,an IOException may be thrown if this
     * stream is closed.
     * @return the number of bytes that can be read from this audio input stream without blocking
     * @throws IOException if an input or output error occurs
     * @see #read(byte[], int, int)
     * @see #read(byte[])
     * @see #read()
     * @see #skip
     */
    public int available() throws IOException {

        int temp = stream.available();

        // don't return greater than our set length in frames
        if( (frameLength != AudioFormat.UNSPECIFIED_FRAME_SIZE) && ( (temp/frameSize) > (frameLength-framePos)) ) {
            return (int) (frameLength-framePos) * frameSize;
        } else {
            return temp;
        }
    }


    /**
     * Closes this audio input stream and releases any system resources associated
     * with the stream.
     * @throws IOException if an input or output error occurs
     */
    public void close() throws IOException {
        stream.close();
    }
}

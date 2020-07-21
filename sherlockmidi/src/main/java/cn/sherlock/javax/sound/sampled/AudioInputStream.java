package cn.sherlock.javax.sound.sampled;

/*
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

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
public class AudioInputStream {

    public interface SynthInputStream {
        int read(byte[] outputBuffer, int off, int len);
        int available();
        void close();
    }

    /**
     * The <code>InputStream</code> from which this <code>AudioInputStream</code>
     * object was constructed.
     */
    private SynthInputStream stream;

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
    public AudioInputStream(SynthInputStream stream, AudioFormat format, long length) {

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
     * @see #available
     */
    public int read(byte[] b, int off, int len) {

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
     * Returns the maximum number of bytes that can be read (or skipped over) from this
     * audio input stream without blocking.  This limit applies only to the next invocation of
     * a <code>read</code> or <code>skip</code> method for this audio input stream; the limit
     * can vary each time these methods are invoked.
     * Depending on the underlying stream,an IOException may be thrown if this
     * stream is closed.
     * @return the number of bytes that can be read from this audio input stream without blocking
     * @see #read(byte[], int, int)
     */
    public int available() {

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
     */
    public void close() {
        stream.close();
    }
}

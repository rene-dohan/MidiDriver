/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package cn.sherlock.com.sun.media.sound;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Resource Interchange File Format (RIFF) stream encoder.
 *
 * @author Karl Helgason
 */
public class RIFFWriter extends OutputStream {

    private interface RandomAccessWriter {

        void seek(long chunksizepointer) throws IOException;

        long getPointer() throws IOException;

        void close() throws IOException;

        void write(int b) throws IOException;

        void write(byte[] b, int off, int len) throws IOException;

        void write(byte[] bytes) throws IOException;

        long length() throws IOException;

        void setLength(long i) throws IOException;
    }

    private static class RandomAccessFileWriter implements RandomAccessWriter {

        RandomAccessFile raf;

        public void seek(long chunksizepointer) throws IOException {
            raf.seek(chunksizepointer);
        }

        public long getPointer() throws IOException {
            return raf.getFilePointer();
        }

        public void close() throws IOException {
            raf.close();
        }

        public void write(int b) throws IOException {
            raf.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            raf.write(b, off, len);
        }

        public void write(byte[] bytes) throws IOException {
            raf.write(bytes);
        }

        public long length() throws IOException {
            return raf.length();
        }

        public void setLength(long i) throws IOException {
            raf.setLength(i);
        }
    }

    private static class RandomAccessByteWriter implements RandomAccessWriter {

        byte[] buff = new byte[32];
        int length = 0;
        int pos = 0;
        byte[] s;
        OutputStream stream;

        public void seek(long chunksizepointer) throws IOException {
            pos = (int) chunksizepointer;
        }

        public long getPointer() throws IOException {
            return pos;
        }

        public void close() throws IOException {
            stream.write(buff, 0, length);
            stream.close();
        }

        public void write(int b) throws IOException {
            if (s == null)
                s = new byte[1];
            s[0] = (byte)b;
            write(s, 0, 1);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            int newsize = pos + len;
            if (newsize > length)
                setLength(newsize);
            int end = off + len;
            for (int i = off; i < end; i++) {
                buff[pos++] = b[i];
            }
        }

        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        public long length() throws IOException {
            return length;
        }

        public void setLength(long i) throws IOException {
            length = (int) i;
            if (length > buff.length) {
                int newlen = Math.max(buff.length << 1, length);
                byte[] newbuff = new byte[newlen];
                System.arraycopy(buff, 0, newbuff, 0, buff.length);
                buff = newbuff;
            }
        }
    }
    private int chunktype = 0; // 0=RIFF, 1=LIST; 2=CHUNK
    private RandomAccessWriter raf;
    private long chunksizepointer;
    private long startpointer;
    private RIFFWriter childchunk = null;
    private boolean open = true;
    private boolean writeoverride = false;

    private RIFFWriter(RandomAccessWriter raf, String format, int chunktype)
            throws IOException {
        if (chunktype == 0)
            if (raf.length() != 0)
                raf.setLength(0);
        this.raf = raf;
        if (raf.getPointer() % 2 != 0)
            raf.write(0);

        if (chunktype == 0)
            raf.write("RIFF".getBytes(StandardCharsets.US_ASCII));
        else if (chunktype == 1)
            raf.write("LIST".getBytes(StandardCharsets.US_ASCII));
        else
            raf.write((format + "    ").substring(0, 4).getBytes(StandardCharsets.US_ASCII));

        chunksizepointer = raf.getPointer();
        this.chunktype = 2;
        writeUnsignedInt(0);
        this.chunktype = chunktype;
        startpointer = raf.getPointer();
        if (chunktype != 2)
            raf.write((format + "    ").substring(0, 4).getBytes(StandardCharsets.US_ASCII));

    }

    public void close() throws IOException {
        if (!open)
            return;
        if (childchunk != null) {
            childchunk.close();
            childchunk = null;
        }

        int bakchunktype = chunktype;
        long fpointer = raf.getPointer();
        raf.seek(chunksizepointer);
        chunktype = 2;
        writeUnsignedInt(fpointer - startpointer);

        if (bakchunktype == 0)
            raf.close();
        else
            raf.seek(fpointer);
        open = false;
        raf = null;
    }

    public void write(int b) throws IOException {
        if (!writeoverride) {
            if (chunktype != 2) {
                throw new IllegalArgumentException(
                        "Only chunks can write bytes!");
            }
            if (childchunk != null) {
                childchunk.close();
                childchunk = null;
            }
        }
        raf.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (!writeoverride) {
            if (chunktype != 2) {
                throw new IllegalArgumentException(
                        "Only chunks can write bytes!");
            }
            if (childchunk != null) {
                childchunk.close();
                childchunk = null;
            }
        }
        raf.write(b, off, len);
    }

    // Write 32 bit signed integer to stream
    public void writeInt(int b) throws IOException {
        write((b >>> 0) & 0xFF);
        write((b >>> 8) & 0xFF);
        write((b >>> 16) & 0xFF);
        write((b >>> 24) & 0xFF);
    }

    // Write 32 bit unsigned integer to stream
    public void writeUnsignedInt(long b) throws IOException {
        writeInt((int) b);
    }
}

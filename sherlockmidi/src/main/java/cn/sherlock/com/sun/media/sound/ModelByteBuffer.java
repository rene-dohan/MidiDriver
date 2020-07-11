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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;

/**
 * This class is a pointer to a binary array either in memory or on disk.
 *
 * @author Karl Helgason
 */
public class ModelByteBuffer {

    private ModelByteBuffer root = this;
    private File file;
    private long fileoffset;
    private byte[] buffer;
    private long offset;
    private final long len;

    private class RandomFileInputStream extends InputStream {

        private RandomAccessFile raf;
        private long left;
        private long mark = 0;
        private long markleft = 0;

        public RandomFileInputStream() throws IOException {
            raf = new RandomAccessFile(root.file, "r");
            raf.seek(root.fileoffset + arrayOffset());
            left = capacity();
        }

        public int available() throws IOException {
            if (left > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int)left;
        }

        public synchronized void mark(int readlimit) {
            try {
                mark = raf.getFilePointer();
                markleft = left;
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        public boolean markSupported() {
            return true;
        }

        public synchronized void reset() throws IOException {
            raf.seek(mark);
            left = markleft;
        }

        public long skip(long n) throws IOException {
            if( n < 0)
                return 0;
            if (n > left)
                n = left;
            long p = raf.getFilePointer();
            raf.seek(p + n);
            left -= n;
            return n;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len > left)
                len = (int)left;
            if (left == 0)
                return -1;
            len = raf.read(b, off, len);
            if (len == -1)
                return -1;
            left -= len;
            return len;
        }

        public int read(byte[] b) throws IOException {
            int len = b.length;
            if (len > left)
                len = (int)left;
            if (left == 0)
                return -1;
            len = raf.read(b, 0, len);
            if (len == -1)
                return -1;
            left -= len;
            return len;
        }

        public int read() throws IOException {
            if (left == 0)
                return -1;
            int b = raf.read();
            if (b == -1)
                return -1;
            left--;
            return b;
        }

        public void close() throws IOException {
            raf.close();
        }
    }

    private ModelByteBuffer(ModelByteBuffer parent,
            long beginIndex, long endIndex, boolean independent) {
        this.root = parent.root;
        this.offset = 0;
        long parent_len = parent.len;
        if (beginIndex < 0)
            beginIndex = 0;
        if (beginIndex > parent_len)
            beginIndex = parent_len;
        if (endIndex < 0)
            endIndex = 0;
        if (endIndex > parent_len)
            endIndex = parent_len;
        if (beginIndex > endIndex)
            beginIndex = endIndex;
        offset = beginIndex;
        len = endIndex - beginIndex;
        if (independent) {
            buffer = root.buffer;
            if (root.file != null) {
                file = root.file;
                fileoffset = root.fileoffset + arrayOffset();
                offset = 0;
            } else
                offset = arrayOffset();
            root = this;
        }
    }

    public ModelByteBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.offset = 0;
        this.len = buffer.length;
    }

    public ModelByteBuffer(byte[] buffer, int offset, int len) {
        this.buffer = buffer;
        this.offset = offset;
        this.len = len;
    }

    public ModelByteBuffer(File file, long offset, long len) {
        this.file = file;
        this.fileoffset = offset;
        this.len = len;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (root.file != null && root.buffer == null) {
            InputStream is = getInputStream();
            byte[] buff = new byte[1024];
            int ret;
            while ((ret = is.read(buff)) != -1)
                out.write(buff, 0, ret);
        } else
            out.write(array(), (int) arrayOffset(), (int) capacity());
    }

    public InputStream getInputStream() {
        if (root.file != null && root.buffer == null) {
            try {
                return new RandomFileInputStream();
            } catch (IOException e) {
                //e.printStackTrace();
                return null;
            }
        }
        return new ByteArrayInputStream(array(),
                (int)arrayOffset(), (int)capacity());
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex) {
        return subbuffer(beginIndex, endIndex, false);
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex,
            boolean independent) {
        return new ModelByteBuffer(this, beginIndex, endIndex, independent);
    }

    public byte[] array() {
        return root.buffer;
    }

    public long arrayOffset() {
        if (root != this)
            return root.arrayOffset() + offset;
        return offset;
    }

    public long capacity() {
        return len;
    }

    public static void loadAll(Collection<ModelByteBuffer> col)
            throws IOException {
        File selfile = null;
        RandomAccessFile raf = null;
        try {
            for (ModelByteBuffer mbuff : col) {
                mbuff = mbuff.root;
                if (mbuff.file == null)
                    continue;
                if (mbuff.buffer != null)
                    continue;
                if (selfile == null || !selfile.equals(mbuff.file)) {
                    if (raf != null) {
                        raf.close();
                        raf = null;
                    }
                    selfile = mbuff.file;
                    raf = new RandomAccessFile(mbuff.file, "r");
                }
                raf.seek(mbuff.fileoffset);
                byte[] buffer = new byte[(int) mbuff.capacity()];

                int read = 0;
                int avail = buffer.length;
                while (read != avail) {
                    if (avail - read > 65536) {
                        raf.readFully(buffer, read, 65536);
                        read += 65536;
                    } else {
                        raf.readFully(buffer, read, avail - read);
                        read = avail;
                    }

                }

                mbuff.buffer = buffer;
                mbuff.offset = 0;
            }
        } finally {
            if (raf != null)
                raf.close();
        }
    }

}

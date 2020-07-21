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

import cn.sherlock.javax.sound.sampled.AudioFormat;

/**
 * This class is used to create AudioFloatInputStream from AudioInputStream and
 * byte buffers.
 *
 * @author Karl Helgason
 */
public class AudioFloatInputStream {

    private int pos = 0;
    private int markpos = 0;
    private AudioFloatConverter converter;
    private byte[] buffer;
    private int buffer_offset;
    private int buffer_len;
    private int framesize_pc;

    public AudioFloatInputStream(AudioFormat format, byte[] buffer, int offset, int len) {
        this.converter = AudioFloatConverter.getConverter(format);
        this.buffer = buffer;
        this.buffer_offset = offset;
        framesize_pc = format.getFrameSize() / format.getChannels();
        this.buffer_len = len / framesize_pc;
    }

    public AudioFormat getFormat() {
        return converter.getFormat();
    }

    public int read(float[] b, int off, int len) {
        if (b == null)
            throw new NullPointerException();
        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();
        if (pos >= buffer_len)
            return -1;
        if (len == 0)
            return 0;
        if (pos + len > buffer_len)
            len = buffer_len - pos;
        converter.toFloatArray(buffer, buffer_offset + pos * framesize_pc, b, off, len);
        pos += len;
        return len;
    }

    public void skip(long len) {
        if (pos >= buffer_len)
            return;
        if (len <= 0)
            return;
        if (pos + len > buffer_len)
            len = buffer_len - pos;
        pos += len;
    }

    public void mark() {
        markpos = pos;
    }

    public void reset() {
        pos = markpos;
    }
}

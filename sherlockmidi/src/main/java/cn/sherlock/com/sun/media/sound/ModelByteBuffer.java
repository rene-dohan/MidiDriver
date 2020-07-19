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
import java.io.InputStream;

/**
 * This class is a pointer to a binary array either in memory or on disk.
 *
 * @author Karl Helgason
 */
public class ModelByteBuffer {

    private ModelByteBuffer root;
    private byte[] buffer;
    private long offset;
    private final long len;

    private ModelByteBuffer(ModelByteBuffer parent, long beginIndex, long endIndex, boolean independent) {
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
            offset = arrayOffset();
            root = this;
        }
    }

    public ModelByteBuffer(byte[] buffer) {
        root = this;
        this.buffer = buffer;
        this.offset = 0;
        this.len = buffer.length;
    }

    public InputStream getInputStream() {
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

}

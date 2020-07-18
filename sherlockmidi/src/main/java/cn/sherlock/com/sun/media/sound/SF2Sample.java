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
 * Soundfont sample storage.
 *
 * @author Karl Helgason
 */
public class SF2Sample {

    protected long startLoop = 0;
    protected long endLoop = 0;
    protected int originalPitch = 60;
    protected byte pitchCorrection = 0;
    protected ModelByteBuffer data;
    private AudioFormat format = new AudioFormat(22050, 16, 1);

    public ModelByteBuffer getDataBuffer() {
        return data;
    }

    public AudioFormat getFormat() {
        return format;
    }

    /*
    public void setData(File file, int offset, int length) {
        this.data = null;
        this.sampleFile = file;
        this.sampleOffset = offset;
        this.sampleLen = length;
    }
    */
}

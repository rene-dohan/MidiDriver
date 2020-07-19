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
import java.io.InputStream;

import cn.sherlock.javax.sound.sampled.AudioFormat;

/**
 * Wavetable oscillator for pre-loaded data.
 *
 * @author Karl Helgason
 */
public class ModelByteBufferWavetable {

    public static final int LOOP_TYPE_OFF = 0;
    public static final int LOOP_TYPE_FORWARD = 1;
    public static final int LOOP_TYPE_RELEASE = 2;

    private float loopStart = -1;
    private float loopLength = -1;
    private ModelByteBuffer buffer;
    private AudioFormat format;
    private float pitchcorrection;
    private int loopType = LOOP_TYPE_OFF;

    public ModelByteBufferWavetable(ModelByteBuffer buffer, AudioFormat format, float pitchcorrection) {
        this.format = format;
        this.buffer = buffer;
        this.pitchcorrection = pitchcorrection;
    }

    public AudioFormat getFormat() {
        if (format == null) {
            if (buffer == null)
                return null;
            InputStream is = buffer.getInputStream();
            try {
                is.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return null;
        }
        return format;
    }

    public AudioFloatInputStream openStream() {
        if (buffer == null)
            return null;
        if (format == null) {
            buffer.getInputStream();
            return null;
        }
        return new AudioFloatInputStream(format, buffer.array(), (int)buffer.arrayOffset(), (int)buffer.capacity());
    }

    public int getChannels() {
        return getFormat().getChannels();
    }

    // attenuation is in cB
    public float getAttenuation() {
        return 0f;
    }

    public float getLoopLength() {
        return loopLength;
    }

    public void setLoopLength(float loopLength) {
        this.loopLength = loopLength;
    }

    public float getLoopStart() {
        return loopStart;
    }

    public void setLoopStart(float loopStart) {
        this.loopStart = loopStart;
    }

    public void setLoopType(int loopType) {
        this.loopType = loopType;
    }

    public int getLoopType() {
        return loopType;
    }

    public float getPitchcorrection() {
        return pitchcorrection;
    }

}

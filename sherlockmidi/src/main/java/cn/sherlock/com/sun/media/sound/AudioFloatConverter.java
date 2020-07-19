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
 * This class is used to convert between 8,16,24,32,32+ bit signed/unsigned
 * big/litle endian fixed/floating point byte buffers and float buffers.
 * 
 * @author Karl Helgason
 */
public class AudioFloatConverter {

    public static final AudioFloatConverter MONO_CONVERTER = new AudioFloatConverter(AudioFormat.MONO_FORMAT);
    public static final AudioFloatConverter STEREO_CONVERTER = new AudioFloatConverter(AudioFormat.STEREO_FORMAT);

    private AudioFormat format;

    public AudioFloatConverter(AudioFormat format) {
        this.format = format;
    }

    /***************************************************************************
     * 
     * 16 bit signed/unsigned, little/big-endian
     * 
     **************************************************************************/

    public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
        int ix = in_offset;
        int len = out_offset + out_len;
        for (int ox = out_offset; ox < len; ox++) {
            out_buff[ox] = ((short) ((in_buff[ix++] & 0xFF) |
                       (in_buff[ix++] << 8))) * (1.0f / 32767.0f);
        }

        return out_buff;
    }

    public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
        int ox = out_offset;
        int len = in_offset + in_len;
        for (int ix = in_offset; ix < len; ix++) {
            int x = (int) (in_buff[ix] * 32767.0);
            out_buff[ox++] = (byte) x;
            out_buff[ox++] = (byte) (x >>> 8);
        }
        return out_buff;
    }

    public static AudioFloatConverter getConverter(AudioFormat format) {
        return format == AudioFormat.MONO_FORMAT ? MONO_CONVERTER : STEREO_CONVERTER;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public void toByteArray(float[] in_buff, int in_len, byte[] out_buff, int out_offset) {
        toByteArray(in_buff, 0, in_len, out_buff, out_offset);
    }

    public void toByteArray(float[] in_buff, int in_len, byte[] out_buff) {
        toByteArray(in_buff, 0, in_len, out_buff, 0);
    }

}

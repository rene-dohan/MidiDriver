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
import java.util.Arrays;

/**
 * A resampler that uses first-order (linear) interpolation.
 *
 * This one doesn't perform float to int casting inside the processing loop.
 *
 * @author Karl Helgason
 */
public class SoftLinearResampler2 {

    private class ModelAbstractResamplerStream implements SoftResamplerStreamer {

        AudioFloatInputStream stream;
        boolean stream_eof = false;
        int loopmode;
        boolean loopdirection = true; // true = forward
        float loopstart;
        float looplen;
        float target_pitch;
        float[] current_pitch = new float[1];
        boolean started;
        boolean eof;
        int sector_pos = 0;
        int sector_size = 400;
        int sector_loopstart = -1;
        boolean markset = false;
        int streampos = 0;
        int nrofchannels = 2;
        boolean noteOff_flag = false;
        float[][] ibuffer;
        boolean ibuffer_order;
        float[] sbuffer;
        int pad;
        int pad2;
        float[] ix = new float[1];
        int[] ox = new int[1];
        float samplerateconv = 1;
        float pitchcorrection = 0;

        public ModelAbstractResamplerStream() {
            pad = getPadding();
            pad2 = getPadding() * 2;
            ibuffer = new float[2][sector_size + pad2];
            ibuffer_order = true;
        }

        public void noteOn() {
        }

        public void noteOff() {
            noteOff_flag = true;
        }

        public void open(ModelByteBufferWavetable osc, float outputsamplerate) throws IOException {

            eof = false;
            nrofchannels = osc.getChannels();
            if (ibuffer.length < nrofchannels) {
                ibuffer = new float[nrofchannels][sector_size + pad2];
            }

            stream = osc.openStream();
            streampos = 0;
            stream_eof = false;
            pitchcorrection = osc.getPitchcorrection();
            samplerateconv = stream.getFormat().getSampleRate() / outputsamplerate;
            looplen = osc.getLoopLength();
            loopstart = osc.getLoopStart();
            sector_loopstart = (int) (loopstart / sector_size);
            sector_loopstart = sector_loopstart - 1;

            sector_pos = 0;

            if (sector_loopstart < 0)
                sector_loopstart = 0;
            started = false;
            loopmode = osc.getLoopType();

            if (loopmode != 0) {
                markset = false;
            } else
                markset = true;
            // loopmode = 0;

            target_pitch = samplerateconv;
            current_pitch[0] = samplerateconv;

            ibuffer_order = true;
            loopdirection = true;
            noteOff_flag = false;

            for (int i = 0; i < nrofchannels; i++)
                Arrays.fill(ibuffer[i], sector_size, sector_size + pad2, 0);
            ix[0] = pad;
            eof = false;

            ix[0] = sector_size + pad;
            sector_pos = -1;
            streampos = -sector_size;

            nextBuffer();
        }

        public void setPitch(float pitch) {
            /*
            this.pitch = (float) Math.pow(2f,
            (pitchcorrection + pitch) / 1200.0f)
             * samplerateconv;
             */
            this.target_pitch = (float)Math.exp(
                    (pitchcorrection + pitch) * (Math.log(2.0) / 1200.0))
                    * samplerateconv;

            if (!started)
                current_pitch[0] = this.target_pitch;
        }

        public void nextBuffer() throws IOException {
            if (ix[0] < pad) {
                if (markset) {
                    // reset to target sector
                    stream.reset();
                    ix[0] += streampos - (sector_loopstart * sector_size);
                    sector_pos = sector_loopstart;
                    streampos = sector_pos * sector_size;

                    // and go one sector backward
                    ix[0] += sector_size;
                    sector_pos -= 1;
                    streampos -= sector_size;
                    stream_eof = false;
                }
            }

            if (ix[0] >= sector_size + pad) {
                if (stream_eof) {
                    eof = true;
                    return;
                }
            }

            if (ix[0] >= sector_size * 4 + pad) {
                int skips = (int)((ix[0] - sector_size * 4 + pad) / sector_size);
                ix[0] -= sector_size * skips;
                sector_pos += skips;
                streampos += sector_size * skips;
                stream.skip(sector_size * skips);
            }

            while (ix[0] >= sector_size + pad) {
                if (!markset) {
                    if (sector_pos + 1 == sector_loopstart) {
                        stream.mark();
                        markset = true;
                    }
                }
                ix[0] -= sector_size;
                sector_pos++;
                streampos += sector_size;

                for (int c = 0; c < nrofchannels; c++) {
                    float[] cbuffer = ibuffer[c];
                    if (pad2 >= 0) System.arraycopy(cbuffer, sector_size, cbuffer, 0, pad2);
                }

                int ret;
                if (nrofchannels == 1)
                    ret = stream.read(ibuffer[0], pad2, sector_size);
                else {
                    int slen = sector_size * nrofchannels;
                    if (sbuffer == null || sbuffer.length < slen)
                        sbuffer = new float[slen];
                    int sret = stream.read(sbuffer, 0, slen);
                    if (sret == -1)
                        ret = -1;
                    else {
                        ret = sret / nrofchannels;
                        for (int i = 0; i < nrofchannels; i++) {
                            float[] buff = ibuffer[i];
                            int ix = i;
                            int ix_step = nrofchannels;
                            int ox = pad2;
                            for (int j = 0; j < ret; j++, ix += ix_step, ox++)
                                buff[ox] = sbuffer[ix];
                        }
                    }

                }

                if (ret == -1) {
                    stream_eof = true;
                    for (int i = 0; i < nrofchannels; i++)
                        Arrays.fill(ibuffer[i], pad2, pad2 + sector_size, 0f);
                    return;
                }
                if (ret != sector_size) {
                    for (int i = 0; i < nrofchannels; i++)
                        Arrays.fill(ibuffer[i], pad2 + ret, pad2 + sector_size, 0f);
                }

                ibuffer_order = true;

            }

        }

        public void reverseBuffers() {
            ibuffer_order = !ibuffer_order;
            for (int c = 0; c < nrofchannels; c++) {
                float[] cbuff = ibuffer[c];
                int len = cbuff.length - 1;
                int len2 = cbuff.length / 2;
                for (int i = 0; i < len2; i++) {
                    float x = cbuff[i];
                    cbuff[i] = cbuff[len - i];
                    cbuff[len - i] = x;
                }
            }
        }

        public int read(float[][] buffer, int offset, int len)
                throws IOException {

            if (eof)
                return -1;

            if (noteOff_flag)
                if ((loopmode & 2) != 0)
                    if (loopdirection)
                        loopmode = 0;


            float pitchstep = (target_pitch - current_pitch[0]) / len;
            float[] current_pitch = this.current_pitch;
            started = true;

            int[] ox = this.ox;
            ox[0] = offset;
            int ox_end = len + offset;

            float ixend = sector_size + pad;
            if (!loopdirection)
                ixend = pad;
            while (ox[0] != ox_end) {
                nextBuffer();
                if (!loopdirection) {
                    // If we are in backward playing part of pingpong
                    // or reverse loop

                    if (streampos < (loopstart + pad)) {
                        ixend = loopstart - streampos + pad2;
                        if (ix[0] <= ixend) {
                            if ((loopmode & 4) != 0) {
                                // Ping pong loop, change loopdirection
                                loopdirection = true;
                                ixend = sector_size + pad;
                                continue;
                            }

                            ix[0] += looplen;
                            ixend = pad;
                            continue;
                        }
                    }

                    if (ibuffer_order)
                        reverseBuffers();

                    ix[0] = (sector_size + pad2) - ix[0];
                    ixend = (sector_size + pad2) - ixend;
                    ixend++;

                    float bak_ix = ix[0];
                    int bak_ox = ox[0];
                    float bak_pitch = current_pitch[0];
                    for (int i = 0; i < nrofchannels; i++) {
                        if (buffer[i] != null) {
                            ix[0] = bak_ix;
                            ox[0] = bak_ox;
                            current_pitch[0] = bak_pitch;
                            interpolate(ibuffer[i], ix, ixend, current_pitch,
                                    pitchstep, buffer[i], ox, ox_end);
                        }
                    }

                    ix[0] = (sector_size + pad2) - ix[0];
                    ixend--;
                    ixend = (sector_size + pad2) - ixend;

                    if (eof) {
                        current_pitch[0] = this.target_pitch;
                        return ox[0] - offset;
                    }

                    continue;
                }
                if (loopmode != 0) {
                    if (streampos + sector_size > (looplen + loopstart + pad)) {
                        ixend = loopstart + looplen - streampos + pad2;
                        if (ix[0] >= ixend) {
                            if ((loopmode & 4) != 0 || (loopmode & 8) != 0) {
                                // Ping pong or revese loop, change loopdirection
                                loopdirection = false;
                                ixend = pad;
                                continue;
                            }
                            ixend = sector_size + pad;
                            ix[0] -= looplen;
                            continue;
                        }
                    }
                }

                if (!ibuffer_order)
                    reverseBuffers();

                float bak_ix = ix[0];
                int bak_ox = ox[0];
                float bak_pitch = current_pitch[0];
                for (int i = 0; i < nrofchannels; i++) {
                    if (buffer[i] != null) {
                        ix[0] = bak_ix;
                        ox[0] = bak_ox;
                        current_pitch[0] = bak_pitch;
                        interpolate(ibuffer[i], ix, ixend, current_pitch,
                                pitchstep, buffer[i], ox, ox_end);
                    }
                }

                if (eof) {
                    current_pitch[0] = this.target_pitch;
                    return ox[0] - offset;
                }
            }

            current_pitch[0] = this.target_pitch;
            return len;
        }

        public void close() throws IOException {
            stream.close();
        }
    }

    public SoftResamplerStreamer openStreamer() {
        return new ModelAbstractResamplerStream();
    }

    public int getPadding() {
        return 2;
    }

    public void interpolate(float[] in, float[] in_offset, float in_end,
            float[] startpitch, float pitchstep, float[] out, int[] out_offset,
            int out_end) {

        float pitch = startpitch[0];
        float ix = in_offset[0];
        int ox = out_offset[0];
        int ox_end = out_end;

        // Check if we have do anything
        if (!(ix < in_end && ox < ox_end))
            return;

        // 15 bit shift was choosed because
        // it resulted in no drift between p_ix and ix.
        int p_ix = (int) (ix * (1 << 15));
        int p_ix_end = (int) (in_end * (1 << 15));
        int p_pitch = (int) (pitch * (1 << 15));
        // Pitch needs to recalculated
        // to ensure no drift between p_ix and ix.
        pitch = p_pitch * (1f / (1 << 15));

        if (pitchstep == 0f) {

            // To reduce
            //    while (p_ix < p_ix_end && ox < ox_end)
            // into
            //    while  (ox < ox_end)
            // We need to calculate new ox_end value.
            int p_ix_len = p_ix_end - p_ix;
            int p_mod = p_ix_len % p_pitch;
            if (p_mod != 0)
                p_ix_len += p_pitch - p_mod;
            int ox_end2 = ox + p_ix_len / p_pitch;
            if (ox_end2 < ox_end)
                ox_end = ox_end2;

            while (ox < ox_end) {
                int iix = p_ix >> 15;
                float fix = ix - iix;
                float i = in[iix];
                out[ox++] = i + (in[iix + 1] - i) * fix;
                p_ix += p_pitch;
                ix += pitch;
            }

        } else {

            int p_pitchstep = (int) (pitchstep * (1 << 15));
            pitchstep = p_pitchstep * (1f / (1 << 15));

            while (p_ix < p_ix_end && ox < ox_end) {
                int iix = p_ix >> 15;
                float fix = ix - iix;
                float i = in[iix];
                out[ox++] = i + (in[iix + 1] - i) * fix;
                ix += pitch;
                p_ix += p_pitch;
                pitch += pitchstep;
                p_pitch += p_pitchstep;
            }
        }
        in_offset[0] = ix;
        out_offset[0] = ox;
        startpitch[0] = pitch;

    }
}

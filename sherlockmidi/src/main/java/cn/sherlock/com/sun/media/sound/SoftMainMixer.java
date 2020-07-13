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
import java.util.TreeMap;

import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.javax.sound.sampled.AudioSystem;

/**
 * Software synthesizer main audio mixer.
 *
 * @author Karl Helgason
 */
public class SoftMainMixer {

    public final static int CHANNEL_LEFT = 0;
    public final static int CHANNEL_RIGHT = 1;
    public final static int CHANNEL_MONO = 2;
    public final static int CHANNEL_DELAY_LEFT = 3;
    public final static int CHANNEL_DELAY_RIGHT = 4;
    public final static int CHANNEL_DELAY_MONO = 5;
    public final static int CHANNEL_EFFECT1 = 6;
    public final static int CHANNEL_EFFECT2 = 7;
    public final static int CHANNEL_DELAY_EFFECT1 = 8;
    public final static int CHANNEL_DELAY_EFFECT2 = 9;
    public final static int CHANNEL_LEFT_DRY = 10;
    public final static int CHANNEL_RIGHT_DRY = 11;
    protected boolean active_sensing_on = false;
    private long msec_last_activity = -1;
    private boolean pusher_silent = false;
    private int pusher_silent_count = 0;
    private long sample_pos;
    protected boolean readfully = true;
    private final Object control_mutex;
    private SoftSynthesizer synth;
    private float samplerate;
    private int nrofchannels;
    private SoftVoice[] voicestatus;
    private SoftAudioBuffer[] buffers;
    private SoftReverb reverb;
    private SoftChorus chorus;
    private SoftLimiter agc;
    private int buffer_len;
    protected TreeMap<Long, Object> midimessages = new TreeMap<>();
    double last_volume_left = 1.0;
    double last_volume_right = 1.0;
    private double[] co_master_balance = new double[1];
    private double[] co_master_volume = new double[1];
    private double[] co_master_coarse_tuning = new double[1];
    private double[] co_master_fine_tuning = new double[1];
    private AudioInputStream ais;
    protected SoftControl co_master = new SoftControl() {

        double[] balance = co_master_balance;
        double[] volume = co_master_volume;
        double[] coarse_tuning = co_master_coarse_tuning;
        double[] fine_tuning = co_master_fine_tuning;

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            if (name.equals("balance"))
                return balance;
            if (name.equals("volume"))
                return volume;
            if (name.equals("coarse_tuning"))
                return coarse_tuning;
            if (name.equals("fine_tuning"))
                return fine_tuning;
            return null;
        }
    };

    protected void processAudioBuffers() {

        if(synth.weakstream != null && synth.weakstream.silent_samples != 0)
        {
            sample_pos += synth.weakstream.silent_samples;
            synth.weakstream.silent_samples = 0;
        }
        
        for (int i = 0; i < buffers.length; i++) {                        
            if(i != CHANNEL_DELAY_LEFT && 
                    i != CHANNEL_DELAY_RIGHT && 
                    i != CHANNEL_DELAY_MONO &&
                    i != CHANNEL_DELAY_EFFECT1 &&
                    i != CHANNEL_DELAY_EFFECT2) 
                buffers[i].clear();
        }
        
        if(!buffers[CHANNEL_DELAY_LEFT].isSilent())
        {
            buffers[CHANNEL_LEFT].swap(buffers[CHANNEL_DELAY_LEFT]);
        }
        if(!buffers[CHANNEL_DELAY_RIGHT].isSilent())
        {
            buffers[CHANNEL_RIGHT].swap(buffers[CHANNEL_DELAY_RIGHT]);
        }
        if(!buffers[CHANNEL_DELAY_MONO].isSilent())
        {
            buffers[CHANNEL_MONO].swap(buffers[CHANNEL_DELAY_MONO]);
        }
        if(!buffers[CHANNEL_DELAY_EFFECT1].isSilent())
        {
            buffers[CHANNEL_EFFECT1].swap(buffers[CHANNEL_DELAY_EFFECT1]);
        }
        if(!buffers[CHANNEL_DELAY_EFFECT2].isSilent())
        {
            buffers[CHANNEL_EFFECT2].swap(buffers[CHANNEL_DELAY_EFFECT2]);
        }

        double volume_left;
        double volume_right;

        // perform control logic
        synchronized (control_mutex) {

            long msec_pos = (long)(sample_pos * (1000000.0 / samplerate));

            if (active_sensing_on) {
                // Active Sensing
                // if no message occurs for max 1000 ms
                // then do AllSoundOff on all channels
                if ((msec_pos - msec_last_activity) > 1000000) {
                    active_sensing_on = false;
                    for (SoftChannel c : synth.channels)
                        c.allSoundOff();
                }

            }

            for (SoftVoice softVoice : voicestatus)
                if (softVoice.active)
                    softVoice.processControlLogic();
            sample_pos += buffer_len;

            double volume = co_master_volume[0];
            volume_left = volume;
            volume_right = volume;

            double balance = co_master_balance[0];
            if (balance > 0.5)
                volume_left *= (1 - balance) * 2;
            else
                volume_right *= balance * 2;

            chorus.processControlLogic();
            reverb.processControlLogic();
            agc.processControlLogic();

        }

        for (SoftVoice softVoice : voicestatus)
            if (softVoice.active)
                softVoice.processAudioLogic(buffers);

        if(!buffers[CHANNEL_MONO].isSilent())
        {
            float[] mono = buffers[CHANNEL_MONO].array();
            float[] left = buffers[CHANNEL_LEFT].array();            
            int bufferlen = buffers[CHANNEL_LEFT].getSize();
            if (nrofchannels != 1) {
                float[] right = buffers[CHANNEL_RIGHT].array();
                for (int i = 0; i < bufferlen; i++) {
                    float v = mono[i];
                    left[i] += v;
                    right[i] += v;
                }                
            }
            else
            {
                for (int i = 0; i < bufferlen; i++) {
                    left[i] += mono[i];
                }                                
            }            
        }

        // Run effects
        if (synth.chorus_on)
            chorus.processAudio();

        if (synth.reverb_on)
            reverb.processAudio();

        if (nrofchannels == 1)
            volume_left = (volume_left + volume_right) / 2;

        // Set Volume / Balance
        if (last_volume_left != volume_left || last_volume_right != volume_right) {
            float[] left = buffers[CHANNEL_LEFT].array();
            float[] right = buffers[CHANNEL_RIGHT].array();
            int bufferlen = buffers[CHANNEL_LEFT].getSize();

            float amp;
            float amp_delta;
            amp = (float)(last_volume_left * last_volume_left);
            amp_delta = (float)((volume_left * volume_left - amp) / bufferlen);
            for (int i = 0; i < bufferlen; i++) {
                amp += amp_delta;
                left[i] *= amp;
            }
            if (nrofchannels != 1) {
                amp = (float)(last_volume_right * last_volume_right);
                amp_delta = (float)((volume_right*volume_right - amp) / bufferlen);
                for (int i = 0; i < bufferlen; i++) {
                    amp += amp_delta;
                    right[i] *= volume_right;
                }
            }
            last_volume_left = volume_left;
            last_volume_right = volume_right;

        } else {
            if (volume_left != 1.0 || volume_right != 1.0) {
                float[] left = buffers[CHANNEL_LEFT].array();
                float[] right = buffers[CHANNEL_RIGHT].array();
                int bufferlen = buffers[CHANNEL_LEFT].getSize();
                float amp;
                amp = (float) (volume_left * volume_left);
                for (int i = 0; i < bufferlen; i++)
                    left[i] *= amp;
                if (nrofchannels != 1) {
                    amp = (float)(volume_right * volume_right);
                    for (int i = 0; i < bufferlen; i++)
                        right[i] *= amp;
                }

            }
        }
        
        if(buffers[CHANNEL_LEFT].isSilent()
            && buffers[CHANNEL_RIGHT].isSilent())
        {       
            
            int midimessages_size;
            synchronized (control_mutex) {
                midimessages_size = midimessages.size();
            }    
            
            if(midimessages_size == 0)
            {
                pusher_silent_count++;
                if(pusher_silent_count > 5)
                {
                    pusher_silent_count = 0;
                    synchronized (control_mutex) {
                        pusher_silent = true;
                        if(synth.weakstream != null)
                            synth.weakstream.setInputStream(null);
                    }                    
                }
            }
        }
        else
            pusher_silent_count = 0;

        if (synth.agc_on)
            agc.processAudio();

    }
        
    // Must only we called within control_mutex synchronization
    public void activity()
    {        
        long silent_samples = 0;
        if(pusher_silent)
        {
            pusher_silent = false;
            if(synth.weakstream != null)
            {
                synth.weakstream.setInputStream(ais);
                silent_samples = synth.weakstream.silent_samples;
            }
        }
        msec_last_activity = (long)((sample_pos + silent_samples)
                * (1000000.0 / samplerate));
    }

    public SoftMainMixer(SoftSynthesizer synth) {
        this.synth = synth;

        sample_pos = 0;

        co_master_balance[0] = 0.5;
        co_master_volume[0] = 1;
        co_master_coarse_tuning[0] = 0.5;
        co_master_fine_tuning[0] = 0.5;

        samplerate = synth.getFormat().getSampleRate();
        nrofchannels = synth.getFormat().getChannels();

        int buffersize = (int) (synth.getFormat().getSampleRate()
                                / synth.getControlRate());
        
        buffer_len = buffersize;

        control_mutex = synth.control_mutex;
        buffers = new SoftAudioBuffer[14];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = new SoftAudioBuffer(buffersize, synth.getFormat());
        }
        voicestatus = synth.getVoices();

        reverb = new SoftReverb();
        chorus = new SoftChorus();
        agc = new SoftLimiter();

        float samplerate = synth.getFormat().getSampleRate();
        float controlrate = synth.getControlRate();
        reverb.init(samplerate);
        chorus.init(samplerate, controlrate);
        agc.init(controlrate);

        reverb.setLightMode(synth.reverb_light);
        
        reverb.setMixMode(true);
        chorus.setMixMode(true);
        agc.setMixMode(false);

        chorus.setInput(0, buffers[CHANNEL_EFFECT2]);
        chorus.setOutput(0, buffers[CHANNEL_LEFT]);
        if (nrofchannels != 1)
            chorus.setOutput(1, buffers[CHANNEL_RIGHT]);
        chorus.setOutput(2, buffers[CHANNEL_EFFECT1]);

        reverb.setInput(0, buffers[CHANNEL_EFFECT1]);
        reverb.setOutput(0, buffers[CHANNEL_LEFT]);
        if (nrofchannels != 1)
            reverb.setOutput(1, buffers[CHANNEL_RIGHT]);

        agc.setInput(0, buffers[CHANNEL_LEFT]);
        if (nrofchannels != 1)
            agc.setInput(1, buffers[CHANNEL_RIGHT]);
        agc.setOutput(0, buffers[CHANNEL_LEFT]);
        if (nrofchannels != 1)
            agc.setOutput(1, buffers[CHANNEL_RIGHT]);

        InputStream in = new InputStream() {

            private SoftAudioBuffer[] buffers = SoftMainMixer.this.buffers;
            private int nrofchannels
                    = SoftMainMixer.this.synth.getFormat().getChannels();
            private int buffersize = buffers[0].getSize();
            private byte[] bbuffer = new byte[buffersize
                    * (SoftMainMixer.this.synth.getFormat()
                        .getSampleSizeInBits() / 8)
                    * nrofchannels];
            private int bbuffer_pos = 0;
            private byte[] single = new byte[1];

            public void fillBuffer() {
                /*
                boolean pusher_silent2;
                synchronized (control_mutex) {
                    pusher_silent2 = pusher_silent;
                }
                if(!pusher_silent2)*/
                processAudioBuffers(); 
                for (int i = 0; i < nrofchannels; i++)
                    buffers[i].get(bbuffer, i);
                bbuffer_pos = 0;
            }

            public int read(byte[] b, int off, int len) {
                int bbuffer_len = bbuffer.length;
                int offlen = off + len;
                int orgoff = off;
                byte[] bbuffer = this.bbuffer;
                while (off < offlen) {
                    if (available() == 0)
                        fillBuffer();
                    else {
                        int bbuffer_pos = this.bbuffer_pos;
                        while (off < offlen && bbuffer_pos < bbuffer_len)
                            b[off++] = bbuffer[bbuffer_pos++];
                        this.bbuffer_pos = bbuffer_pos;
                        if (!readfully)
                            return off - orgoff;
                    }
                }
                return len;
            }

            public int read() throws IOException {
                int ret = read(single);
                if (ret == -1)
                    return -1;
                return single[0] & 0xFF;
            }

            public int available() {
                return bbuffer.length - bbuffer_pos;
            }

            public void close() {
                SoftMainMixer.this.synth.close();
            }
        };

        ais = new AudioInputStream(in, synth.getFormat(), AudioSystem.NOT_SPECIFIED);

    }

    public AudioInputStream getInputStream() {
        return ais;
    }

    public void close() {
    }
}

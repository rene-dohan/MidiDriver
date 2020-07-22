/*
 * Copyright 2008-2010 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sherlock.javax.sound.sampled.AudioFormat;
import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.media.SourceDataLineImpl;
import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.Patch;

/**
 * The software synthesizer class.
 *
 * @author Karl Helgason
 */
public class SoftSynthesizer {

    public static final int MAX_POLY = 64;

    protected final Object control_mutex = this;

    protected int voiceIDCounter = 0;

    protected boolean reverb_light = true;
    protected boolean reverb_on = true;
    protected boolean chorus_on = true;
    protected boolean agc_on = true;

    private SoftLinearResampler2 resampler = new SoftLinearResampler2();

    private SoftMainMixer mainmixer = new SoftMainMixer(this);
    private SoftVoice[] voices = new SoftVoice[MAX_POLY];
    {
        for (int i = 0; i < MAX_POLY; i++) {
            voices[i] = new SoftVoice(this);
            voices[i].resampler = resampler.openStreamer();
        }
    }

    protected SoftChannel[] channels = new SoftChannel[16];
    {
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new SoftChannel(this, i);
        }
    }

    protected SoftChannelProxy[] external_channels = new SoftChannelProxy[channels.length];
    {
        for (int i = 0; i < external_channels.length; i++) {
            external_channels[i] = new SoftChannelProxy();
            external_channels[i].setChannel(channels[i]);
        }
    }

    private SourceDataLineImpl sourceDataLine = new SourceDataLineImpl(
            AudioFormat.STEREO_FORMAT,
            AudioFormat.STEREO_FORMAT.getFrameSize() * (int)(AudioFormat.STEREO_FORMAT.getFrameRate() * (120000L/1000000f))
    );

    private SoftAudioPusher pusher = new SoftAudioPusher(sourceDataLine,  mainmixer.getInputStream());

    private Map<String, SoftTuning> tunings = new HashMap<>();
    private Map<String, SoftInstrument> inslist = new HashMap<>();

    private boolean open = false;

    private boolean loadInstruments(List<SF2Instrument> instruments) {
        synchronized (control_mutex) {
            if (channels != null)
                for (SoftChannel c : channels)
                {
                    c.current_instrument = null;
                    c.current_director = null;
                }
            for (SF2Instrument instrument : instruments) {
                String pat = patchToString(instrument.getPatch());
                inslist.put(pat, new SoftInstrument(instrument));
            }
        }

        return true;
    }

    private String patchToString(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch) patch).isPercussion())
            return "p." + patch.getProgram() + "." + patch.getBank();
        else
            return patch.getProgram() + "." + patch.getBank();
    }

    protected SoftMainMixer getMainMixer() {
        return mainmixer;
    }

    protected SoftInstrument findInstrument(int program, int bank, int channel) {

        // Add support for GM2 banks 0x78 and 0x79
        // as specified in DLS 2.2 in Section 1.4.6
        // which allows using percussion and melodic instruments
        // on all channels
        if (bank >> 7 == 0x78 || bank >> 7 == 0x79) {
            SoftInstrument current_instrument
                    = inslist.get(program + "." + bank);
            if (current_instrument != null)
                return current_instrument;

            String p_plaf;
            if (bank >> 7 == 0x78)
                p_plaf = "p.";
            else
                p_plaf = "";

            // Instrument not found fallback to MSB:bank, LSB:0
            current_instrument = inslist.get(p_plaf + program + "."
                    + ((bank & 128) << 7));
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:bank
            current_instrument = inslist.get(p_plaf + program + "."
                    + (bank & 128));
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:0
            current_instrument = inslist.get(p_plaf + program + ".0");
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:0, program=0
            current_instrument = inslist.get(p_plaf + program + "0.0");
            return current_instrument;
        }

        // Channel 10 uses percussion instruments
        String p_plaf;
        if (channel == 9)
            p_plaf = "p.";
        else
            p_plaf = "";

        SoftInstrument current_instrument = inslist.get(p_plaf + program + "." + bank);
        if (current_instrument != null)
            return current_instrument;
        // Instrument not found fallback to MSB:0, LSB:0
        current_instrument = inslist.get(p_plaf + program + ".0");
        if (current_instrument != null)
            return current_instrument;
        // Instrument not found fallback to MSB:0, LSB:0, program=0
        current_instrument = inslist.get(p_plaf + "0.0");
        return current_instrument;
    }

    protected float getControlRate() {
        return 147f;
    }

    protected SoftVoice[] getVoices() {
        return voices;
    }

    protected SoftTuning getTuning(Patch patch) {
        String t_id = patchToString(patch);
        SoftTuning tuning = tunings.get(t_id);
        if (tuning == null) {
            tuning = new SoftTuning();
            tunings.put(t_id, tuning);
        }
        return tuning;
    }

    public MidiChannel[] getChannels() {
        synchronized (control_mutex) {
            return external_channels;
        }
    }

    public boolean loadInstrument(SF2Instrument instrument) {
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument is null");
        }
        List<SF2Instrument> instruments = new ArrayList<>();
        instruments.add(instrument);
        return loadInstruments(instruments);
    }

    public void unloadInstrument(SF2Instrument instrument) {
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument is null");
        }

        String pat = patchToString(instrument.getPatch());
        synchronized (control_mutex) {
            for (SoftChannel c: channels)
                c.current_instrument = null;
            inslist.remove(pat);
            for (SoftChannel channel : channels) {
                channel.allSoundOff();
            }
        }
    }

    public void open() throws InterruptedException {
        synchronized (control_mutex) {
            if (open) return;
            sourceDataLine.start();
            pusher.start();
            Thread.sleep(160);
            open = true;
        }
    }

    public void close() {

        pusher.stop();

        synchronized (control_mutex) {
            sourceDataLine.close();
        }
    }
}

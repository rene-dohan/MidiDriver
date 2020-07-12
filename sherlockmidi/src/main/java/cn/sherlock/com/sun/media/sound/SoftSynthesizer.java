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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import cn.sherlock.javax.sound.sampled.AudioFormat;
import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.javax.sound.sampled.AudioSystem;
import cn.sherlock.media.SourceDataLineImpl;
import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.Patch;

/**
 * The software synthesizer class.
 *
 * @author Karl Helgason
 */
public class SoftSynthesizer {

    protected static class WeakAudioStream extends InputStream
    {
        private volatile AudioInputStream stream;
        public SoftAudioPusher pusher = null;
        public AudioInputStream jitter_stream = null;
        public SourceDataLineImpl sourceDataLine = null;
        public volatile long silent_samples = 0;
        private int framesize;
        private WeakReference<AudioInputStream> weak_stream_link;
        private AudioFloatConverter converter;
        private float[] silentbuffer = null;
        private int samplesize;

        public void setInputStream(AudioInputStream stream)
        {
            this.stream = stream;
        }

        public int available() throws IOException {
            AudioInputStream local_stream = stream;
            if(local_stream != null)
                return local_stream.available();
            return 0;
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b) == -1)
                return -1;
            return b[0] & 0xFF;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            AudioInputStream local_stream = stream;
            if(local_stream != null)
                return local_stream.read(b, off, len);
            else
            {
                int flen = len / samplesize;
                if(silentbuffer == null || silentbuffer.length < flen)
                    silentbuffer = new float[flen];
                converter.toByteArray(silentbuffer, flen, b, off);

                silent_samples += (len / framesize);

                if(pusher != null)
                    if(weak_stream_link.get() == null)
                    {
                        Runnable runnable = new Runnable()
                        {
                            SoftAudioPusher _pusher = pusher;
                            AudioInputStream _jitter_stream = jitter_stream;
                            SourceDataLineImpl _sourceDataLine = sourceDataLine;
                            public void run()
                            {
                                _pusher.stop();
                                if(_jitter_stream != null)
                                    try {
                                        _jitter_stream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                if(_sourceDataLine != null)
                                    _sourceDataLine.close();
                            }
                        };
                        pusher = null;
                        jitter_stream = null;
                        sourceDataLine = null;
                        new Thread(runnable).start();
                    }
                return len;
            }
        }

        public WeakAudioStream(AudioInputStream stream) {
            this.stream = stream;
            weak_stream_link = new WeakReference<>(stream);
            converter = AudioFloatConverter.getConverter(stream.getFormat());
            samplesize = stream.getFormat().getFrameSize() / stream.getFormat().getChannels();
            framesize = stream.getFormat().getFrameSize();
        }

        public AudioInputStream getAudioInputStream()
        {
            return new AudioInputStream(this, stream.getFormat(), AudioSystem.NOT_SPECIFIED);
        }

        public void close() throws IOException
        {
            AudioInputStream astream  = weak_stream_link.get();
            if(astream != null)
                astream.close();
        }
    }

    private static SourceDataLineImpl testline = null;

    protected WeakAudioStream weakstream = null;

    protected final Object control_mutex = this;

    protected int voiceIDCounter = 0;

    // 0: default
    // 1: DLS Voice Allocation
    protected int voice_allocation_mode = 0;

    protected boolean load_default_soundbank = false;
    protected boolean reverb_light = true;
    protected boolean reverb_on = true;
    protected boolean chorus_on = true;
    protected boolean agc_on = true;

    protected SoftChannel[] channels;
    protected SoftChannelProxy[] external_channels = null;

    private boolean largemode = false;

    // 0: GM Mode off (default)
    // 1: GM Level 1
    // 2: GM Level 2
    private int gmmode = 0;

    private int deviceid = 0;

    private AudioFormat format = new AudioFormat(44100, 16, 2, true, false);

    private SourceDataLineImpl sourceDataLine = null;

    private SoftAudioPusher pusher = null;
    private AudioInputStream pusher_stream = null;

    private float controlrate = 147f;

    private boolean open = false;

    private String resamplerType = "linear";
    private SoftAbstractResampler resampler = new SoftLinearResampler();

    private int number_of_midi_channels = 16;
    private int maxpoly = 64;
    private long latency = 200000; // 200 msec
    private boolean jitter_correction = false;

    private SoftMainMixer mainmixer;
    private SoftVoice[] voices;

    private Map<String, SoftTuning> tunings = new HashMap<>();
    private Map<String, SoftInstrument> inslist = new HashMap<>();

    private void getBuffers(SF2Instrument instrument,
                            List<ModelByteBuffer> buffers) {
        for (ModelPerformer performer : instrument.getPerformers()) {
            if (performer.getOscillators() != null) {
                for (ModelOscillator osc : performer.getOscillators()) {
                    if (osc instanceof ModelByteBufferWavetable) {
                        ModelByteBufferWavetable w = (ModelByteBufferWavetable)osc;
                        ModelByteBuffer buff = w.getBuffer();
                        if (buff != null)
                            buffers.add(buff);
                        buff = w.get8BitExtensionBuffer();
                        if (buff != null)
                            buffers.add(buff);
                    }
                }
            }
        }
    }

    private boolean loadSamples(List<SF2Instrument> instruments) {
        if (largemode)
            return true;
        List<ModelByteBuffer> buffers = new ArrayList<>();
        for (SF2Instrument instrument : instruments)
            getBuffers(instrument, buffers);
        try {
            ModelByteBuffer.loadAll(buffers);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean loadInstruments(List<SF2Instrument> instruments) {
        if (!isOpen())
            return false;
        if (!loadSamples(instruments))
            return false;

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

    private void processPropertyInfo() {
        AudioSynthesizerPropertyInfo[] items = getPropertyInfo();

        String resamplerType = (String)items[0].value;
        if (resamplerType.equalsIgnoreCase("point"))
        {
            this.resampler = new SoftPointResampler();
            this.resamplerType = "point";
        }
        else if (resamplerType.equalsIgnoreCase("linear"))
        {
            this.resampler = new SoftLinearResampler2();
            this.resamplerType = "linear";
        }
        else if (resamplerType.equalsIgnoreCase("linear1"))
        {
            this.resampler = new SoftLinearResampler();
            this.resamplerType = "linear1";
        }
        else if (resamplerType.equalsIgnoreCase("linear2"))
        {
            this.resampler = new SoftLinearResampler2();
            this.resamplerType = "linear2";
        }
        else if (resamplerType.equalsIgnoreCase("cubic"))
        {
            this.resampler = new SoftCubicResampler();
            this.resamplerType = "cubic";
        }
        else if (resamplerType.equalsIgnoreCase("lanczos"))
        {
            this.resampler = new SoftLanczosResampler();
            this.resamplerType = "lanczos";
        }
        else if (resamplerType.equalsIgnoreCase("sinc"))
        {
            this.resampler = new SoftSincResampler();
            this.resamplerType = "sinc";
        }

        setFormat((AudioFormat)items[2].value);
        controlrate = (Float)items[1].value;
        latency = (Long)items[3].value;
        deviceid = (Integer)items[4].value;
        maxpoly = (Integer)items[5].value;
        reverb_on = (Boolean)items[6].value;
        chorus_on = (Boolean)items[7].value;
        agc_on = (Boolean)items[8].value;
        largemode = (Boolean)items[9].value;
        number_of_midi_channels = (Integer)items[10].value;
        jitter_correction = (Boolean)items[11].value;
        reverb_light = (Boolean)items[12].value;
        load_default_soundbank = (Boolean)items[13].value;
    }

    private String patchToString(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch) patch).isPercussion())
            return "p." + patch.getProgram() + "." + patch.getBank();
        else
            return patch.getProgram() + "." + patch.getBank();
    }

    private void setFormat(AudioFormat format) {
        if (format.getChannels() > 2) {
            throw new IllegalArgumentException(
                    "Only mono and stereo audio supported.");
        }
        if (AudioFloatConverter.getConverter(format) == null)
            throw new IllegalArgumentException("Audio format not supported.");
        this.format = format;
    }

    protected SoftMainMixer getMainMixer() {
        if (!isOpen())
            return null;
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

        SoftInstrument current_instrument
                = inslist.get(p_plaf + program + "." + bank);
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

    protected int getVoiceAllocationMode() {
        return voice_allocation_mode;
    }

    protected int getGeneralMidiMode() {
        return gmmode;
    }

    protected float getControlRate() {
        return controlrate;
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

    public AudioFormat getFormat() {
        synchronized (control_mutex) {
            return format;
        }
    }

    public MidiChannel[] getChannels() {

        synchronized (control_mutex) {
            // if (external_channels == null) => the synthesizer is not open,
            // create 16 proxy channels
            // otherwise external_channels has the same length as channels array
            if (external_channels == null) {
                external_channels = new SoftChannelProxy[16];
                for (int i = 0; i < external_channels.length; i++)
                    external_channels[i] = new SoftChannelProxy();
            }
            MidiChannel[] ret;
            if (isOpen())
                ret = new MidiChannel[channels.length];
            else
                ret = new MidiChannel[16];
            System.arraycopy(external_channels, 0, ret, 0, ret.length);
            return ret;
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
        if (!isOpen())
            return;

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

    private Properties getStoredProperties() {
        return AccessController
                .doPrivileged(new PrivilegedAction<Properties>() {
                    public Properties run() {
                        Properties p = new Properties();
                        String notePath = "/com/sun/media/sound/softsynthesizer";
                        try {
                            Preferences prefroot = Preferences.userRoot();
                            if (prefroot.nodeExists(notePath)) {
                                Preferences prefs = prefroot.node(notePath);
                                String[] prefs_keys = prefs.keys();
                                for (String prefs_key : prefs_keys) {
                                    String val = prefs.get(prefs_key, null);
                                    if (val != null)
                                        p.setProperty(prefs_key, val);
                                }
                            }
                        } catch (BackingStoreException | SecurityException ignored) {
                        }
                        return p;
                    }
                });
    }

    private AudioSynthesizerPropertyInfo[] getPropertyInfo() {
        List<AudioSynthesizerPropertyInfo> list = new ArrayList<>();

        AudioSynthesizerPropertyInfo item;

        // If info != null or synthesizer is closed
        //   we return how the synthesizer will be set on next open
        // If info == null and synthesizer is open
        //   we return current synthesizer properties.
        boolean o = open;

        item = new AudioSynthesizerPropertyInfo("interpolation", o?resamplerType:"linear");
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("control rate", o?controlrate:147f);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("format",
                o?format:new AudioFormat(44100, 16, 2, true, false));
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("latency", o?latency:120000L);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("device id", o?deviceid:0);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("max polyphony", o?maxpoly:64);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("reverb", !o || reverb_on);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("chorus", !o || chorus_on);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("auto gain control", !o || agc_on);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("large mode", o && largemode);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("midi channels", o?channels.length:16);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("jitter correction", !o || jitter_correction);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("light reverb", !o || reverb_light);
        list.add(item);

        item = new AudioSynthesizerPropertyInfo("load default soundbank", !o || load_default_soundbank);
        list.add(item);

        AudioSynthesizerPropertyInfo[] items;
        items = list.toArray(new AudioSynthesizerPropertyInfo[0]);

        Properties storedProperties = getStoredProperties();

        for (AudioSynthesizerPropertyInfo item2 : items) {
            String v = storedProperties.getProperty(item2.name);
            if (v != null) {
                Class c = (item2.valueClass);
                if (c.isInstance(v))
                    item2.value = v;
                else {
                    if (c == Boolean.class) {
                        if (v.equalsIgnoreCase("true"))
                            item2.value = Boolean.TRUE;
                        if (v.equalsIgnoreCase("false"))
                            item2.value = Boolean.FALSE;
                    } else if (c == AudioFormat.class) {
                        int channels = 2;
                        boolean signed = true;
                        boolean bigendian = false;
                        int bits = 16;
                        float sampleRate = 44100f;
                        try {
                            StringTokenizer st = new StringTokenizer(v, ", ");
                            String prevToken = "";
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken().toLowerCase();
                                if (token.equals("mono"))
                                    channels = 1;
                                if (token.startsWith("channel"))
                                    channels = Integer.parseInt(prevToken);
                                if (token.contains("unsigned"))
                                    signed = false;
                                if (token.equals("big-endian"))
                                    bigendian = true;
                                if (token.equals("bit"))
                                    bits = Integer.parseInt(prevToken);
                                if (token.equals("hz"))
                                    sampleRate = Float.parseFloat(prevToken);
                                prevToken = token;
                            }
                            item2.value = new AudioFormat(sampleRate, bits,
                                    channels, signed, bigendian);
                        } catch (NumberFormatException ignored) {
                        }

                    } else
                        try {
                            if (c == Byte.class)
                                item2.value = Byte.valueOf(v);
                            else if (c == Short.class)
                                item2.value = Short.valueOf(v);
                            else if (c == Integer.class)
                                item2.value = Integer.valueOf(v);
                            else if (c == Long.class)
                                item2.value = Long.valueOf(v);
                            else if (c == Float.class)
                                item2.value = Float.valueOf(v);
                            else if (c == Double.class)
                                item2.value = Double.valueOf(v);
                        } catch (NumberFormatException ignored) {
                        }
                }
            }
        }

        return items;
    }

    public void open() {
        synchronized (control_mutex) {
            if (open) return;
            try {

                AudioInputStream ais = openStream(getFormat());

                weakstream = new WeakAudioStream(ais);
                ais = weakstream.getAudioInputStream();

                SourceDataLineImpl line = testline == null ? AudioSystem.getSourceDataLine() : testline;

                double latency = this.latency;

                if (!line.isOpen()) {
                    int bufferSize = getFormat().getFrameSize()
                            * (int)(getFormat().getFrameRate() * (latency/1000000f));
                    // can throw LineUnavailableException,
                    // IllegalArgumentException, SecurityException
                    line.open(getFormat(), bufferSize);

                    // Remember that we opened that line
                    // so we can close again in SoftSynthesizer.close()
                    sourceDataLine = line;
                }
                if (!line.isActive())
                    line.start();

                int controlbuffersize = 512;
                try {
                    controlbuffersize = ais.available();
                } catch (IOException ignored) {
                }

                // Tell mixer not fill read buffers fully.
                // This lowers latency, and tells DataPusher
                // to read in smaller amounts.
                //mainmixer.readfully = false;
                //pusher = new DataPusher(line, ais);

                int buffersize = line.getBufferSize();
                buffersize -= buffersize % controlbuffersize;

                if (buffersize < 3 * controlbuffersize)
                    buffersize = 3 * controlbuffersize;

                if (jitter_correction) {
                    ais = new SoftJitterCorrector(ais, buffersize,
                            controlbuffersize);
                    if(weakstream != null)
                        weakstream.jitter_stream = ais;
                }
                pusher = new SoftAudioPusher(line, ais, controlbuffersize);
                pusher_stream = ais;
                pusher.start();

                if(weakstream != null)
                {
                    weakstream.pusher = pusher;
                    weakstream.sourceDataLine = sourceDataLine;
                }

            } catch (IllegalArgumentException | SecurityException e) {
                if (open) close();
                throw e;
            }
        }
    }

    private AudioInputStream openStream(AudioFormat targetFormat) {

        synchronized (control_mutex) {
            if (open) throw new RuntimeException("Synthesizer is already open");

            gmmode = 0;
            voice_allocation_mode = 0;

            processPropertyInfo();

            open = true;

            if (targetFormat != null)
                setFormat(targetFormat);

            voices = new SoftVoice[maxpoly];
            for (int i = 0; i < maxpoly; i++)
                voices[i] = new SoftVoice(this);

            mainmixer = new SoftMainMixer(this);

            channels = new SoftChannel[number_of_midi_channels];
            for (int i = 0; i < channels.length; i++)
                channels[i] = new SoftChannel(this, i);

            if (external_channels == null) {
                // Always create external_channels array
                // with 16 or more channels
                // so getChannels works correctly
                // when the synhtesizer is closed.
                if (channels.length < 16)
                    external_channels = new SoftChannelProxy[16];
                else
                    external_channels = new SoftChannelProxy[channels.length];
                for (int i = 0; i < external_channels.length; i++)
                    external_channels[i] = new SoftChannelProxy();
            }  // We must resize external_channels array
            // but we must also copy the old SoftChannelProxy
            // into the new one


            for (int i = 0; i < channels.length; i++)
                external_channels[i].setChannel(channels[i]);

            for (SoftVoice voice: getVoices())
                voice.resampler = resampler.openStreamer();

            return mainmixer.getInputStream();
        }
    }

    public void close() {

        if (!isOpen())
            return;

        SoftAudioPusher pusher_to_be_closed = null;
        AudioInputStream pusher_stream_to_be_closed = null;
        synchronized (control_mutex) {
            if (pusher != null) {
                pusher_to_be_closed = pusher;
                pusher_stream_to_be_closed = pusher_stream;
                pusher = null;
                pusher_stream = null;
            }
        }

        if (pusher_to_be_closed != null) {
            // Pusher must not be closed synchronized against control_mutex,
            // this may result in synchronized conflict between pusher
            // and current thread.
            pusher_to_be_closed.stop();

            try {
                pusher_stream_to_be_closed.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        synchronized (control_mutex) {

            if (mainmixer != null)
                mainmixer.close();
            open = false;
            mainmixer = null;
            voices = null;
            channels = null;

            if (external_channels != null)
                for (SoftChannelProxy external_channel : external_channels)
                    external_channel.setChannel(null);

            if (sourceDataLine != null) {
                sourceDataLine.close();
                sourceDataLine = null;
            }

            inslist.clear();
            tunings.clear();

        }
    }

    private boolean isOpen() {
        synchronized (control_mutex) {
            return open;
        }
    }
}

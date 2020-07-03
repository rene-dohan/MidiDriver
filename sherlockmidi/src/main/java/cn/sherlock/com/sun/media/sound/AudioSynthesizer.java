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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import jp.kshoji.javax.sound.midi.Instrument;
import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Patch;
import jp.kshoji.javax.sound.midi.Soundbank;
import cn.sherlock.javax.sound.sampled.AudioFormat;
import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.javax.sound.sampled.SourceDataLine;
import jp.kshoji.javax.sound.midi.Transmitter;
import jp.kshoji.javax.sound.midi.VoiceStatus;

/**
 * <code>AudioSynthesizer</code> is a <code>Synthesizer</code>
 * which renders it's output audio into <code>SourceDataLine</code>
 * or <code>AudioInputStream</code>.
 *
 * @author Karl Helgason
 */
public interface AudioSynthesizer {

    /**
     * Get the device information
     *
     * @return the device information
     */
    @NonNull
    Info getDeviceInfo();

    /**
     * Open the {@link AudioSynthesizer}. This method must be called at getting the new instance.
     *
     * @throws MidiUnavailableException
     */
    void open() throws MidiUnavailableException;

    /**
     * Close the {@link AudioSynthesizer}. This method must be called at finishing to use the instance.
     */
    void close();

    /**
     * Check if the {@link AudioSynthesizer} opened.
     *
     * @return true if already opened
     */
    boolean isOpen();

    /**
     * Get the {@link AudioSynthesizer}'s timeStamp.
     * @return -1 if the timeStamp not supported.
     */
    long getMicrosecondPosition();

    /**
     * Get the number of the {@link MidiDeviceReceiver}s.
     *
     * @return the number of the {@link MidiDeviceReceiver}s.
     */
    int getMaxReceivers();

    /**
     * Get the number of the {@link Transmitter}s.
     *
     * @return the number of the {@link Transmitter}s.
     */
    int getMaxTransmitters();

    /**
     * Get the default {@link MidiDeviceReceiver}.
     *
     * @return the default {@link MidiDeviceReceiver}.
     * @throws MidiUnavailableException
     */
    @NonNull
    MidiDeviceReceiver getReceiver() throws MidiUnavailableException;

    /**
     * Get the all of {@link MidiDeviceReceiver}s.
     *
     * @return the all of {@link MidiDeviceReceiver}s.
     */
    @NonNull
    List<MidiDeviceReceiver> getReceivers();

    /**
     * Get the default {@link Transmitter}.
     *
     * @return the default {@link Transmitter}.
     * @throws MidiUnavailableException
     */
    @NonNull
    Transmitter getTransmitter() throws MidiUnavailableException;

    /**
     * Get the all of {@link Transmitter}s.
     *
     * @return the all of {@link Transmitter}s.
     */
    @NonNull
    List<Transmitter> getTransmitters();

    /**
     * Represents the {@link AudioSynthesizer}'s information
     *
     * @author K.Shoji
     */
    class Info {
        private final String name;
        private final String vendor;
        private final String description;
        private final String version;

        /**
         * Constructor
         *
         * @param name the name string
         * @param vendor the vendor string
         * @param description the description string
         * @param version the version string
         */
        public Info(@NonNull final String name, @NonNull final String vendor, @NonNull final String description, @NonNull final String version) {
            this.name = name;
            this.vendor = vendor;
            this.description = description;
            this.version = version;
        }

        /**
         * Get the name of {@link AudioSynthesizer}
         *
         * @return the name of {@link AudioSynthesizer}
         */
        @NonNull
        public final String getName() {
            return name;
        }

        /**
         * Get the vendor of {@link AudioSynthesizer}
         *
         * @return the vendor of {@link AudioSynthesizer}
         */
        @NonNull
        public final String getVendor() {
            return vendor;
        }

        /**
         * Get the description of {@link AudioSynthesizer}
         *
         * @return the description of {@link AudioSynthesizer}
         */
        @NonNull
        public final String getDescription() {
            return description;
        }

        /**
         * Get the version of {@link AudioSynthesizer}
         *
         * @return the version of {@link AudioSynthesizer}
         */
        @NonNull
        public final String getVersion() {
            return version;
        }

        @NonNull
        @Override
        public final String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + description.hashCode();
            result = prime * result + name.hashCode();
            result = prime * result + vendor.hashCode();
            result = prime * result + version.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Info other = (Info) obj;
            if (!description.equals(other.description)) {
                return false;
            }
            if (!name.equals(other.name)) {
                return false;
            }
            if (!vendor.equals(other.vendor)) {
                return false;
            }
            if (!version.equals(other.version)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Get the all of {@link MidiChannel}s
     *
     * @return the array of MidiChannel
     */
    @NonNull
    MidiChannel[] getChannels();

    /**
     * Get the latency in microseconds
     *
     * @return the latency in microseconds
     */
    long getLatency();

    /**
     * Get the maximum count of polyphony
     *
     * @return the maximum count of polyphony
     */
    int getMaxPolyphony();

    /**
     * Get the current {@link VoiceStatus} of the Synthesizer
     *
     * @return the array of VoiceStatus
     */
    @NonNull
    VoiceStatus[] getVoiceStatus();

    /**
     * Get the default {@link Soundbank}
     *
     * @return the Soundbank
     */
    @Nullable
    Soundbank getDefaultSoundbank();

    /**
     * Check if the specified {@link Soundbank} is supported
     *
     * @param soundbank the Soundbank
     * @return true if the Soundbank is supported
     */
    boolean isSoundbankSupported(@NonNull Soundbank soundbank);

    /**
     * Get the all available {@link Instrument}s
     *
     * @return the array of Instrument
     */
    @NonNull
    Instrument[] getAvailableInstruments();

    /**
     * Get the all loaded {@link Instrument}s
     *
     * @return the array of Instrument
     */
    @NonNull
    Instrument[] getLoadedInstruments();

    /**
     * Remap an Instrument
     *
     * @param from to be replaced
     * @param to the new Instrument
     * @return true if succeed to remap
     */
    boolean remapInstrument(@NonNull Instrument from, @NonNull Instrument to);

    /**
     * Load all instruments belongs specified {@link Soundbank}
     *
     * @param soundbank the Soundbank
     * @return true if succeed to load
     */
    boolean loadAllInstruments(@NonNull Soundbank soundbank);

    /**
     * Unload all instruments belongs specified {@link Soundbank}
     *
     * @param soundbank the Soundbank
     */
    void unloadAllInstruments(@NonNull Soundbank soundbank);

    /**
     * Load the specified {@link Instrument}
     *
     * @param instrument the instrument
     * @return true if succeed to load
     */
    boolean loadInstrument(@NonNull Instrument instrument);

    /**
     * Unload the specified {@link Instrument}
     *
     * @param instrument the instrument
     */
    void unloadInstrument(@NonNull Instrument instrument);

    /**
     * Load all instruments belongs specified {@link Soundbank} and {@link Patch}es
     *
     * @param soundbank the the Soundbank
     * @param patchList the array of Patch
     * @return true if succeed to load
     */
    boolean loadInstruments(@NonNull Soundbank soundbank, @NonNull Patch[] patchList);

    /**
     * Unload all instruments belongs specified {@link Soundbank} and {@link Patch}es
     *
     * @param soundbank the the Soundbank
     * @param patchList the array of Patch
     */
    void unloadInstruments(@NonNull Soundbank soundbank, @NonNull Patch[] patchList);

    /**
     * Obtains the current format (encoding, sample rate, number of channels,
     * etc.) of the synthesizer audio data.
     *
     * <p>If the synthesizer is not open and has never been opened, it returns
     * the default format.
     *
     * @return current audio data format
     * @see AudioFormat
     */
    public AudioFormat getFormat();

    /**
     * Gets information about the possible properties for the synthesizer.
     *
     * @param info a proposed list of tag/value pairs that will be sent on open.
     * @return an array of <code>AudioSynthesizerPropertyInfo</code> objects
     * describing possible properties. This array may be an empty array if
     * no properties are required.
     */
    public AudioSynthesizerPropertyInfo[] getPropertyInfo(
            Map<String, Object> info);

    /**
     * Opens the synthesizer and starts rendering audio into
     * <code>SourceDataLine</code>.
     *
     * <p>An application opening a synthesizer explicitly with this call
     * has to close the synthesizer by calling {@link #close}. This is
     * necessary to release system resources and allow applications to
     * exit cleanly.
     *
     * <p>Note that some synthesizers, once closed, cannot be reopened.
     * Attempts to reopen such a synthesizer will always result in
     * a <code>MidiUnavailableException</code>.
     *
     * @param line which <code>AudioSynthesizer</code> writes output audio into.
     * If <code>line</code> is null, then line from system default mixer is used.
     * @param info a <code>Map<String,Object></code> object containing
     * properties for additional configuration supported by synthesizer.
     * If <code>info</code> is null then default settings are used.
     *
     * @throws MidiUnavailableException thrown if the synthesizer cannot be
     * opened due to resource restrictions.
     * @throws SecurityException thrown if the synthesizer cannot be
     * opened due to security restrictions.
     *
     * @see #close
     * @see #isOpen
     */
    public void open(SourceDataLine line, Map<String, Object> info)
            throws MidiUnavailableException;

    /**
     * Opens the synthesizer and renders audio into returned
     * <code>AudioInputStream</code>.
     *
     * <p>An application opening a synthesizer explicitly with this call
     * has to close the synthesizer by calling {@link #close}. This is
     * necessary to release system resources and allow applications to
     * exit cleanly.
     *
     * <p>Note that some synthesizers, once closed, cannot be reopened.
     * Attempts to reopen such a synthesizer will always result in
     * a <code>MidiUnavailableException<code>.
     *
     * @param targetFormat specifies the <code>AudioFormat</code>
     * used in returned <code>AudioInputStream</code>.
     * @param info a <code>Map<String,Object></code> object containing
     * properties for additional configuration supported by synthesizer.
     * If <code>info</code> is null then default settings are used.
     *
     * @throws MidiUnavailableException thrown if the synthesizer cannot be
     * opened due to resource restrictions.
     * @throws SecurityException thrown if the synthesizer cannot be
     * opened due to security restrictions.
     *
     * @see #close
     * @see #isOpen
     */
    public AudioInputStream openStream(AudioFormat targetFormat,
                                       Map<String, Object> info) throws MidiUnavailableException;
}

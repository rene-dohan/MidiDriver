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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.kshoji.javax.sound.midi.Patch;

/**
 * A SoundFont 2.04 soundbank reader.
 *
 * Based on SoundFont 2.04 specification from:
 * <p>  http://developer.creative.com <br>
 *      http://www.soundfont.com/ ;
 *
 * @author Karl Helgason
 */
public class SF2Soundbank {

    // The Sample Data loaded from the SoundFont
    private ModelByteBuffer sampleData = null;
    private ModelByteBuffer sampleData24 = null;
    private File sampleFile = null;
    private boolean largeFormat = false;
    private List<SF2Instrument> instruments = new ArrayList<>();
    private List<SF2Layer> layers = new ArrayList<SF2Layer>();
    private List<SF2Sample> samples = new ArrayList<SF2Sample>();

    public SF2Soundbank(InputStream inputstream) throws IOException {
        readSoundbank(inputstream);
    }

    private void readSoundbank(InputStream inputstream) throws IOException {
        RIFFReader riff = new RIFFReader(inputstream);
        if (!riff.getFormat().equals("RIFF")) {
            throw new RIFFInvalidFormatException(
                    "Input stream is not a valid RIFF stream!");
        }
        if (!riff.getType().equals("sfbk")) {
            throw new RIFFInvalidFormatException(
                    "Input stream is not a valid SoundFont!");
        }
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("INFO"))
                    readInfoChunk(chunk);
                if (chunk.getType().equals("sdta"))
                    readSdtaChunk(chunk);
                if (chunk.getType().equals("pdta"))
                    readPdtaChunk(chunk);
            }
        }
    }

    private void readInfoChunk(RIFFReader riff) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("ifil")) {
                chunk.readUnsignedShort();
                chunk.readUnsignedShort();
            } else if (format.equals("isng")) {
                chunk.readString(chunk.available());
            } else if (format.equals("INAM")) {
                chunk.readString(chunk.available());
            } else if (format.equals("irom")) {
                chunk.readString(chunk.available());
            } else if (format.equals("iver")) {
                chunk.readUnsignedShort();
                chunk.readUnsignedShort();
            } else if (format.equals("ICRD")) {
                chunk.readString(chunk.available());
            } else if (format.equals("IENG")) {
                chunk.readString(chunk.available());
            } else if (format.equals("IPRD")) {
                chunk.readString(chunk.available());
            } else if (format.equals("ICOP")) {
                chunk.readString(chunk.available());
            } else if (format.equals("ICMT")) {
                chunk.readString(chunk.available());
            } else if (format.equals("ISFT")) {
                chunk.readString(chunk.available());
            }

        }
    }

    private void readSdtaChunk(RIFFReader riff) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("smpl")) {
                if (!largeFormat) {
                    byte[] sampleData = new byte[chunk.available()];

                    int read = 0;
                    int avail = chunk.available();
                    while (read != avail) {
                        if (avail - read > 65536) {
                            chunk.readFully(sampleData, read, 65536);
                            read += 65536;
                        } else {
                            chunk.readFully(sampleData, read, avail - read);
                            read = avail;
                        }

                    }
                    this.sampleData = new ModelByteBuffer(sampleData);
                    //chunk.read(sampleData);
                } else {
                    this.sampleData = new ModelByteBuffer(sampleFile,
                            chunk.getFilePointer(), chunk.available());
                }
            }
            if (chunk.getFormat().equals("sm24")) {
                if (!largeFormat) {
                    byte[] sampleData24 = new byte[chunk.available()];
                    //chunk.read(sampleData24);

                    int read = 0;
                    int avail = chunk.available();
                    while (read != avail) {
                        if (avail - read > 65536) {
                            chunk.readFully(sampleData24, read, 65536);
                            read += 65536;
                        } else {
                            chunk.readFully(sampleData24, read, avail - read);
                            read = avail;
                        }

                    }
                    this.sampleData24 = new ModelByteBuffer(sampleData24);
                } else {
                    this.sampleData24 = new ModelByteBuffer(sampleFile,
                            chunk.getFilePointer(), chunk.available());
                }

            }
        }
    }

    private void readPdtaChunk(RIFFReader riff) throws IOException {

        List<SF2Instrument> presets = new ArrayList<SF2Instrument>();
        List<Integer> presets_bagNdx = new ArrayList<Integer>();
        List<SF2InstrumentRegion> presets_splits_gen
                = new ArrayList<SF2InstrumentRegion>();
        List<SF2InstrumentRegion> presets_splits_mod
                = new ArrayList<SF2InstrumentRegion>();

        List<SF2Layer> instruments = new ArrayList<SF2Layer>();
        List<Integer> instruments_bagNdx = new ArrayList<Integer>();
        List<SF2LayerRegion> instruments_splits_gen
                = new ArrayList<SF2LayerRegion>();
        List<SF2LayerRegion> instruments_splits_mod
                = new ArrayList<SF2LayerRegion>();

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("phdr")) {
                // Preset Header / Instrument
                if (chunk.available() % 38 != 0)
                    throw new RIFFInvalidDataException();
                int count = chunk.available() / 38;
                for (int i = 0; i < count; i++) {
                    SF2Instrument preset = new SF2Instrument(this);
                    preset.name = chunk.readString(20);
                    preset.preset = chunk.readUnsignedShort();
                    preset.bank = chunk.readUnsignedShort();
                    presets_bagNdx.add(chunk.readUnsignedShort());
                    chunk.readUnsignedInt();
                    chunk.readUnsignedInt();
                    chunk.readUnsignedInt();
                    presets.add(preset);
                    if (i != count - 1)
                        this.instruments.add(preset);
                }
            } else if (format.equals("pbag")) {
                // Preset Zones / Instruments splits
                if (chunk.available() % 4 != 0)
                    throw new RIFFInvalidDataException();
                int count = chunk.available() / 4;

                // Skip first record
                {
                    int gencount = chunk.readUnsignedShort();
                    int modcount = chunk.readUnsignedShort();
                    while (presets_splits_gen.size() < gencount)
                        presets_splits_gen.add(null);
                    while (presets_splits_mod.size() < modcount)
                        presets_splits_mod.add(null);
                    count--;
                }

                int offset = presets_bagNdx.get(0);
                // Offset should be 0 (but just case)
                for (int i = 0; i < offset; i++) {
                    if (count == 0)
                        throw new RIFFInvalidDataException();
                    int gencount = chunk.readUnsignedShort();
                    int modcount = chunk.readUnsignedShort();
                    while (presets_splits_gen.size() < gencount)
                        presets_splits_gen.add(null);
                    while (presets_splits_mod.size() < modcount)
                        presets_splits_mod.add(null);
                    count--;
                }

                for (int i = 0; i < presets_bagNdx.size() - 1; i++) {
                    int zone_count = presets_bagNdx.get(i + 1)
                                     - presets_bagNdx.get(i);
                    SF2Instrument preset = presets.get(i);
                    for (int ii = 0; ii < zone_count; ii++) {
                        if (count == 0)
                            throw new RIFFInvalidDataException();
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        SF2InstrumentRegion split = new SF2InstrumentRegion();
                        preset.regions.add(split);
                        while (presets_splits_gen.size() < gencount)
                            presets_splits_gen.add(split);
                        while (presets_splits_mod.size() < modcount)
                            presets_splits_mod.add(split);
                        count--;
                    }
                }
            } else if (format.equals("pmod")) {
                // Preset Modulators / Split Modulators
                for (int i = 0; i < presets_splits_mod.size(); i++) {
                    SF2Modulator modulator = new SF2Modulator();
                    modulator.sourceOperator = chunk.readUnsignedShort();
                    modulator.destinationOperator = chunk.readUnsignedShort();
                    modulator.amount = chunk.readShort();
                    modulator.amountSourceOperator = chunk.readUnsignedShort();
                    modulator.transportOperator = chunk.readUnsignedShort();
                    SF2InstrumentRegion split = presets_splits_mod.get(i);
                    if (split != null)
                        split.modulators.add(modulator);
                }
            } else if (format.equals("pgen")) {
                // Preset Generators / Split Generators
                for (int i = 0; i < presets_splits_gen.size(); i++) {
                    int operator = chunk.readUnsignedShort();
                    short amount = chunk.readShort();
                    SF2InstrumentRegion split = presets_splits_gen.get(i);
                    if (split != null)
                        split.generators.put(operator, amount);
                }
            } else if (format.equals("inst")) {
                // Instrument Header / Layers
                if (chunk.available() % 22 != 0)
                    throw new RIFFInvalidDataException();
                int count = chunk.available() / 22;
                for (int i = 0; i < count; i++) {
                    SF2Layer layer = new SF2Layer(this);
                    layer.name = chunk.readString(20);
                    instruments_bagNdx.add(chunk.readUnsignedShort());
                    instruments.add(layer);
                    if (i != count - 1)
                        this.layers.add(layer);
                }
            } else if (format.equals("ibag")) {
                // Instrument Zones / Layer splits
                if (chunk.available() % 4 != 0)
                    throw new RIFFInvalidDataException();
                int count = chunk.available() / 4;

                // Skip first record
                {
                    int gencount = chunk.readUnsignedShort();
                    int modcount = chunk.readUnsignedShort();
                    while (instruments_splits_gen.size() < gencount)
                        instruments_splits_gen.add(null);
                    while (instruments_splits_mod.size() < modcount)
                        instruments_splits_mod.add(null);
                    count--;
                }

                int offset = instruments_bagNdx.get(0);
                // Offset should be 0 (but just case)
                for (int i = 0; i < offset; i++) {
                    if (count == 0)
                        throw new RIFFInvalidDataException();
                    int gencount = chunk.readUnsignedShort();
                    int modcount = chunk.readUnsignedShort();
                    while (instruments_splits_gen.size() < gencount)
                        instruments_splits_gen.add(null);
                    while (instruments_splits_mod.size() < modcount)
                        instruments_splits_mod.add(null);
                    count--;
                }

                for (int i = 0; i < instruments_bagNdx.size() - 1; i++) {
                    int zone_count = instruments_bagNdx.get(i + 1) - instruments_bagNdx.get(i);
                    SF2Layer layer = layers.get(i);
                    for (int ii = 0; ii < zone_count; ii++) {
                        if (count == 0)
                            throw new RIFFInvalidDataException();
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        SF2LayerRegion split = new SF2LayerRegion();
                        layer.regions.add(split);
                        while (instruments_splits_gen.size() < gencount)
                            instruments_splits_gen.add(split);
                        while (instruments_splits_mod.size() < modcount)
                            instruments_splits_mod.add(split);
                        count--;
                    }
                }

            } else if (format.equals("imod")) {
                // Instrument Modulators / Split Modulators
                for (int i = 0; i < instruments_splits_mod.size(); i++) {
                    SF2Modulator modulator = new SF2Modulator();
                    modulator.sourceOperator = chunk.readUnsignedShort();
                    modulator.destinationOperator = chunk.readUnsignedShort();
                    modulator.amount = chunk.readShort();
                    modulator.amountSourceOperator = chunk.readUnsignedShort();
                    modulator.transportOperator = chunk.readUnsignedShort();
                    SF2LayerRegion split = instruments_splits_gen.get(i);
                    if (split != null)
                        split.modulators.add(modulator);
                }
            } else if (format.equals("igen")) {
                // Instrument Generators / Split Generators
                for (int i = 0; i < instruments_splits_gen.size(); i++) {
                    int operator = chunk.readUnsignedShort();
                    short amount = chunk.readShort();
                    SF2LayerRegion split = instruments_splits_gen.get(i);
                    if (split != null)
                        split.generators.put(operator, amount);
                }
            } else if (format.equals("shdr")) {
                // Sample Headers
                if (chunk.available() % 46 != 0)
                    throw new RIFFInvalidDataException();
                int count = chunk.available() / 46;
                for (int i = 0; i < count; i++) {
                    SF2Sample sample = new SF2Sample(this);
                    sample.name = chunk.readString(20);
                    long start = chunk.readUnsignedInt();
                    long end = chunk.readUnsignedInt();
                    sample.data = sampleData.subbuffer(start * 2, end * 2, true);
                    if (sampleData24 != null)
                        sample.data24 = sampleData24.subbuffer(start, end, true);
                    /*
                    sample.data = new ModelByteBuffer(sampleData, (int)(start*2),
                            (int)((end - start)*2));
                    if (sampleData24 != null)
                        sample.data24 = new ModelByteBuffer(sampleData24,
                                (int)start, (int)(end - start));
                     */
                    sample.startLoop = chunk.readUnsignedInt() - start;
                    sample.endLoop = chunk.readUnsignedInt() - start;
                    if (sample.startLoop < 0)
                        sample.startLoop = -1;
                    if (sample.endLoop < 0)
                        sample.endLoop = -1;
                    sample.sampleRate = chunk.readUnsignedInt();
                    sample.originalPitch = chunk.readUnsignedByte();
                    sample.pitchCorrection = chunk.readByte();
                    chunk.readUnsignedShort();
                    chunk.readUnsignedShort();
                    if (i != count - 1)
                        this.samples.add(sample);
                }
            }
        }

        Iterator<SF2Layer> liter = this.layers.iterator();
        while (liter.hasNext()) {
            SF2Layer layer = liter.next();
            Iterator<SF2LayerRegion> siter = layer.regions.iterator();
            SF2Region globalsplit = null;
            while (siter.hasNext()) {
                SF2LayerRegion split = siter.next();
                if (split.generators.get(SF2LayerRegion.GENERATOR_SAMPLEID) != null) {
                    int sampleid = split.generators.get(
                            SF2LayerRegion.GENERATOR_SAMPLEID);
                    split.generators.remove(SF2LayerRegion.GENERATOR_SAMPLEID);
                    split.sample = samples.get(sampleid);
                } else {
                    globalsplit = split;
                }
            }
            if (globalsplit != null) {
                layer.getRegions().remove(globalsplit);
                SF2GlobalRegion gsplit = new SF2GlobalRegion();
                gsplit.generators = globalsplit.generators;
                gsplit.modulators = globalsplit.modulators;
                layer.setGlobalZone(gsplit);
            }
        }


        Iterator<SF2Instrument> iiter = this.instruments.iterator();
        while (iiter.hasNext()) {
            SF2Instrument instrument = iiter.next();
            Iterator<SF2InstrumentRegion> siter = instrument.regions.iterator();
            SF2Region globalsplit = null;
            while (siter.hasNext()) {
                SF2InstrumentRegion split = siter.next();
                if (split.generators.get(SF2LayerRegion.GENERATOR_INSTRUMENT) != null) {
                    int instrumentid = split.generators.get(
                            SF2InstrumentRegion.GENERATOR_INSTRUMENT);
                    split.generators.remove(SF2LayerRegion.GENERATOR_INSTRUMENT);
                    split.layer = layers.get(instrumentid);
                } else {
                    globalsplit = split;
                }
            }

            if (globalsplit != null) {
                instrument.getRegions().remove(globalsplit);
                SF2GlobalRegion gsplit = new SF2GlobalRegion();
                gsplit.generators = globalsplit.generators;
                gsplit.modulators = globalsplit.modulators;
                instrument.setGlobalZone(gsplit);
            }
        }

    }

    public SF2Instrument getInstrument(Patch patch) {
        int program = patch.getProgram();
        int bank = patch.getBank();
        boolean percussion = false;
        if (patch instanceof ModelPatch)
            percussion = ((ModelPatch)patch).isPercussion();
        for (SF2Instrument instrument : instruments) {
            Patch patch2 = instrument.getPatch();
            int program2 = patch2.getProgram();
            int bank2 = patch2.getBank();
            if (program == program2 && bank == bank2) {
                boolean percussion2 = false;
                if (patch2 instanceof ModelPatch)
                    percussion2 = ((ModelPatch) patch2).isPercussion();
                if (percussion == percussion2)
                    return instrument;
            }
        }
        return null;
    }

}

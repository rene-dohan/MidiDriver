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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sherlock.javax.sound.sampled.AudioFormat;
import jp.kshoji.javax.sound.midi.Patch;

/**
 * Soundfont instrument.
 *
 * @author Karl Helgason
 */
public class SF2Instrument {

    protected int preset = 0;
    protected int bank = 0;
    protected List<SF2InstrumentRegion> regions = new ArrayList<>();
    private ModelPerformer[] performers;

    public Patch getPatch() {
        if (bank == 128)
            return new ModelPatch(0, preset, true);
        else
            return new ModelPatch(bank << 7, preset, false);
    }

    public List<SF2InstrumentRegion> getRegions() {
        return regions;
    }

    public ModelStandardIndexedDirector getDirector(SoftChannel player) {
        return new ModelStandardIndexedDirector(getPerformers(), player);
    }

    public ModelPerformer[] getPerformers() {
        if (performers == null) {
            int performercount = 0;
            for (SF2InstrumentRegion presetzone : regions)
                performercount += presetzone.layerRegions.size();

            performers = new ModelPerformer[performercount];
            int pi = 0;

            for (SF2InstrumentRegion presetzone : regions) {
                Map<Integer, Short> pgenerators = new HashMap<>(presetzone.getGenerators());

                for (SF2LayerRegion layerzone : presetzone.layerRegions) {
                    ModelPerformer performer = new ModelPerformer();

                    performers[pi++] = performer;

                    int keyfrom = 0;
                    int keyto = 127;
                    int velfrom = 0;
                    int velto = 127;

                    if (layerzone.contains(SF2Region.GENERATOR_EXCLUSIVECLASS)) {
                        performer.setExclusiveClass(layerzone.getInteger(
                                SF2Region.GENERATOR_EXCLUSIVECLASS));
                    }
                    if (layerzone.contains(SF2Region.GENERATOR_KEYRANGE)) {
                        byte[] bytes = layerzone.getBytes(
                                SF2Region.GENERATOR_KEYRANGE);
                        if (bytes[0] >= 0)
                            if (bytes[0] > keyfrom)
                                keyfrom = bytes[0];
                        if (bytes[1] >= 0)
                            if (bytes[1] < keyto)
                                keyto = bytes[1];
                    }
                    if (layerzone.contains(SF2Region.GENERATOR_VELRANGE)) {
                        byte[] bytes = layerzone.getBytes(
                                SF2Region.GENERATOR_VELRANGE);
                        if (bytes[0] >= 0)
                            if (bytes[0] > velfrom)
                                velfrom = bytes[0];
                        if (bytes[1] >= 0)
                            if (bytes[1] < velto)
                                velto = bytes[1];
                    }
                    if (presetzone.contains(SF2Region.GENERATOR_KEYRANGE)) {
                        byte[] bytes = presetzone.getBytes(
                                SF2Region.GENERATOR_KEYRANGE);
                        if (bytes[0] > keyfrom)
                            keyfrom = bytes[0];
                        if (bytes[1] < keyto)
                            keyto = bytes[1];
                    }
                    if (presetzone.contains(SF2Region.GENERATOR_VELRANGE)) {
                        byte[] bytes = presetzone.getBytes(
                                SF2Region.GENERATOR_VELRANGE);
                        if (bytes[0] > velfrom)
                            velfrom = bytes[0];
                        if (bytes[1] < velto)
                            velto = bytes[1];
                    }
                    performer.setKeyFrom(keyfrom);
                    performer.setKeyTo(keyto);
                    performer.setVelFrom(velfrom);
                    performer.setVelTo(velto);

                    int startAddrsOffset = layerzone.getShort(
                            SF2Region.GENERATOR_STARTADDRSOFFSET);
                    int endAddrsOffset = layerzone.getShort(
                            SF2Region.GENERATOR_ENDADDRSOFFSET);
                    int startloopAddrsOffset = layerzone.getShort(
                            SF2Region.GENERATOR_STARTLOOPADDRSOFFSET);
                    int endloopAddrsOffset = layerzone.getShort(
                            SF2Region.GENERATOR_ENDLOOPADDRSOFFSET);

                    startAddrsOffset += layerzone.getShort(
                            SF2Region.GENERATOR_STARTADDRSCOARSEOFFSET) * 32768;
                    endAddrsOffset += layerzone.getShort(
                            SF2Region.GENERATOR_ENDADDRSCOARSEOFFSET) * 32768;
                    startloopAddrsOffset += layerzone.getShort(
                            SF2Region.GENERATOR_STARTLOOPADDRSCOARSEOFFSET) * 32768;
                    endloopAddrsOffset += layerzone.getShort(
                            SF2Region.GENERATOR_ENDLOOPADDRSCOARSEOFFSET) * 32768;
                    startloopAddrsOffset -= startAddrsOffset;
                    endloopAddrsOffset -= startAddrsOffset;

                    int rootkey = layerzone.sample.originalPitch;
                    if (layerzone.getShort(SF2Region.GENERATOR_OVERRIDINGROOTKEY) != -1) {
                        rootkey = layerzone.getShort(SF2Region.GENERATOR_OVERRIDINGROOTKEY);
                    }
                    float pitchcorrection = (-rootkey * 100) + layerzone.sample.pitchCorrection;
                    ModelByteBuffer buff = layerzone.sample.data;

                    if (startAddrsOffset != 0 || endAddrsOffset != 0) {
                        buff = buff.subbuffer(startAddrsOffset * 2, buff.capacity() + endAddrsOffset * 2);
                    }

                    ModelByteBufferWavetable osc = new ModelByteBufferWavetable(buff, AudioFormat.MONO_FORMAT, pitchcorrection);

                    Map<Integer, Short> generators = new HashMap<>(layerzone.getGenerators());
                    for (Map.Entry<Integer, Short> gen : pgenerators.entrySet()) {
                        short val;
                        if (!generators.containsKey(gen.getKey()))
                            val = layerzone.getShort(gen.getKey());
                        else
                            val = generators.get(gen.getKey());
                        val += gen.getValue();
                        generators.put(gen.getKey(), val);
                    }

                    // SampleMode:
                    // 0 indicates a sound reproduced with no loop
                    // 1 indicates a sound which loops continuously
                    // 2 is unused but should be interpreted as indicating no loop
                    // 3 indicates a sound which loops for the duration of key
                    //   depression then proceeds to play the remainder of the sample.
                    int sampleMode = getGeneratorValue(generators,
                            SF2Region.GENERATOR_SAMPLEMODES);
                    if ((sampleMode == 1) || (sampleMode == 3)) {
                        if (layerzone.sample.startLoop >= 0 && layerzone.sample.endLoop > 0) {
                            osc.setLoopStart((int) (layerzone.sample.startLoop + startloopAddrsOffset));
                            osc.setLoopLength((int) (layerzone.sample.endLoop - layerzone.sample.startLoop + endloopAddrsOffset - startloopAddrsOffset));
                            if (sampleMode == 1)
                                osc.setLoopType(ModelByteBufferWavetable.LOOP_TYPE_FORWARD);
                            if (sampleMode == 3)
                                osc.setLoopType(ModelByteBufferWavetable.LOOP_TYPE_RELEASE);
                        }
                    }
                    performer.getOscillators().add(osc);


                    short volDelay = getGeneratorValue(generators,
                            SF2Region.GENERATOR_DELAYVOLENV);
                    short volAttack = getGeneratorValue(generators,
                            SF2Region.GENERATOR_ATTACKVOLENV);
                    short volHold = getGeneratorValue(generators,
                            SF2Region.GENERATOR_HOLDVOLENV);
                    short volDecay = getGeneratorValue(generators,
                            SF2Region.GENERATOR_DECAYVOLENV);
                    short volSustain = getGeneratorValue(generators,
                            SF2Region.GENERATOR_SUSTAINVOLENV);
                    short volRelease = getGeneratorValue(generators,
                            SF2Region.GENERATOR_RELEASEVOLENV);

                    if (volHold != -12000) {
                        short volKeyNumToHold = getGeneratorValue(generators,
                                SF2Region.GENERATOR_KEYNUMTOVOLENVHOLD);
                        volHold += 60 * volKeyNumToHold;
                        float fvalue = -volKeyNumToHold * 128;
                        ModelIdentifier src = ModelSource.SOURCE_NOTEON_KEYNUMBER;
                        ModelIdentifier dest = ModelDestination.DESTINATION_EG1_HOLD;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(new ModelSource(src), fvalue,
                                        new ModelDestination(dest)));
                    }
                    if (volDecay != -12000) {
                        short volKeyNumToDecay = getGeneratorValue(generators,
                                SF2Region.GENERATOR_KEYNUMTOVOLENVDECAY);
                        volDecay += 60 * volKeyNumToDecay;
                        float fvalue = -volKeyNumToDecay * 128;
                        ModelIdentifier src = ModelSource.SOURCE_NOTEON_KEYNUMBER;
                        ModelIdentifier dest = ModelDestination.DESTINATION_EG1_DECAY;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(new ModelSource(src), fvalue,
                                        new ModelDestination(dest)));
                    }

                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_EG1_DELAY, volDelay);
                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_EG1_ATTACK, volAttack);
                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_EG1_HOLD, volHold);
                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_EG1_DECAY, volDecay);
                    //float fvolsustain = (960-volSustain)*(1000.0f/960.0f);

                    volSustain = (short) (1000 - volSustain);
                    if (volSustain < 0)
                        volSustain = 0;
                    if (volSustain > 1000)
                        volSustain = 1000;

                    addValue(performer,
                            ModelDestination.DESTINATION_EG1_SUSTAIN, volSustain);
                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_EG1_RELEASE, volRelease);

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODENVTOFILTERFC) != 0
                            || getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODENVTOPITCH) != 0) {
                        short modDelay = getGeneratorValue(generators,
                                SF2Region.GENERATOR_DELAYMODENV);
                        short modAttack = getGeneratorValue(generators,
                                SF2Region.GENERATOR_ATTACKMODENV);
                        short modHold = getGeneratorValue(generators,
                                SF2Region.GENERATOR_HOLDMODENV);
                        short modDecay = getGeneratorValue(generators,
                                SF2Region.GENERATOR_DECAYMODENV);
                        short modSustain = getGeneratorValue(generators,
                                SF2Region.GENERATOR_SUSTAINMODENV);
                        short modRelease = getGeneratorValue(generators,
                                SF2Region.GENERATOR_RELEASEMODENV);


                        if (modHold != -12000) {
                            short modKeyNumToHold = getGeneratorValue(generators,
                                    SF2Region.GENERATOR_KEYNUMTOMODENVHOLD);
                            modHold += 60 * modKeyNumToHold;
                            float fvalue = -modKeyNumToHold * 128;
                            ModelIdentifier src = ModelSource.SOURCE_NOTEON_KEYNUMBER;
                            ModelIdentifier dest = ModelDestination.DESTINATION_EG2_HOLD;
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(new ModelSource(src),
                                            fvalue, new ModelDestination(dest)));
                        }
                        if (modDecay != -12000) {
                            short modKeyNumToDecay = getGeneratorValue(generators,
                                    SF2Region.GENERATOR_KEYNUMTOMODENVDECAY);
                            modDecay += 60 * modKeyNumToDecay;
                            float fvalue = -modKeyNumToDecay * 128;
                            ModelIdentifier src = ModelSource.SOURCE_NOTEON_KEYNUMBER;
                            ModelIdentifier dest = ModelDestination.DESTINATION_EG2_DECAY;
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(new ModelSource(src),
                                            fvalue, new ModelDestination(dest)));
                        }

                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_EG2_DELAY, modDelay);
                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_EG2_ATTACK, modAttack);
                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_EG2_HOLD, modHold);
                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_EG2_DECAY, modDecay);
                        if (modSustain < 0)
                            modSustain = 0;
                        if (modSustain > 1000)
                            modSustain = 1000;
                        addValue(performer, ModelDestination.DESTINATION_EG2_SUSTAIN,
                                1000 - modSustain);
                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_EG2_RELEASE, modRelease);

                        if (getGeneratorValue(generators,
                                SF2Region.GENERATOR_MODENVTOFILTERFC) != 0) {
                            double fvalue = getGeneratorValue(generators,
                                    SF2Region.GENERATOR_MODENVTOFILTERFC);
                            ModelIdentifier src = ModelSource.SOURCE_EG2;
                            ModelIdentifier dest
                                    = ModelDestination.DESTINATION_FILTER_FREQ;
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(new ModelSource(src),
                                            fvalue, new ModelDestination(dest)));
                        }

                        if (getGeneratorValue(generators,
                                SF2Region.GENERATOR_MODENVTOPITCH) != 0) {
                            double fvalue = getGeneratorValue(generators,
                                    SF2Region.GENERATOR_MODENVTOPITCH);
                            ModelIdentifier src = ModelSource.SOURCE_EG2;
                            ModelIdentifier dest = ModelDestination.DESTINATION_PITCH;
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(new ModelSource(src),
                                            fvalue, new ModelDestination(dest)));
                        }

                    }

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOFILTERFC) != 0
                            || getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOPITCH) != 0
                            || getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOVOLUME) != 0) {
                        short lfo_freq = getGeneratorValue(generators,
                                SF2Region.GENERATOR_FREQMODLFO);
                        short lfo_delay = getGeneratorValue(generators,
                                SF2Region.GENERATOR_DELAYMODLFO);
                        addTimecentValue(performer,
                                ModelDestination.DESTINATION_LFO1_DELAY, lfo_delay);
                        addValue(performer,
                                ModelDestination.DESTINATION_LFO1_FREQ, lfo_freq);
                    }

                    short vib_freq = getGeneratorValue(generators,
                            SF2Region.GENERATOR_FREQVIBLFO);
                    short vib_delay = getGeneratorValue(generators,
                            SF2Region.GENERATOR_DELAYVIBLFO);
                    addTimecentValue(performer,
                            ModelDestination.DESTINATION_LFO2_DELAY, vib_delay);
                    addValue(performer,
                            ModelDestination.DESTINATION_LFO2_FREQ, vib_freq);


                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_VIBLFOTOPITCH) != 0) {
                        double fvalue = getGeneratorValue(generators,
                                SF2Region.GENERATOR_VIBLFOTOPITCH);
                        ModelIdentifier src = ModelSource.SOURCE_LFO2;
                        ModelIdentifier dest = ModelDestination.DESTINATION_PITCH;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(
                                        new ModelSource(src,
                                                ModelStandardTransform.DIRECTION_MIN2MAX,
                                                ModelStandardTransform.POLARITY_BIPOLAR),
                                        fvalue, new ModelDestination(dest)));
                    }

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOFILTERFC) != 0) {
                        double fvalue = getGeneratorValue(generators,
                                SF2Region.GENERATOR_MODLFOTOFILTERFC);
                        ModelIdentifier src = ModelSource.SOURCE_LFO1;
                        ModelIdentifier dest = ModelDestination.DESTINATION_FILTER_FREQ;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(
                                        new ModelSource(src,
                                                ModelStandardTransform.DIRECTION_MIN2MAX,
                                                ModelStandardTransform.POLARITY_BIPOLAR),
                                        fvalue, new ModelDestination(dest)));
                    }

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOPITCH) != 0) {
                        double fvalue = getGeneratorValue(generators,
                                SF2Region.GENERATOR_MODLFOTOPITCH);
                        ModelIdentifier src = ModelSource.SOURCE_LFO1;
                        ModelIdentifier dest = ModelDestination.DESTINATION_PITCH;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(
                                        new ModelSource(src,
                                                ModelStandardTransform.DIRECTION_MIN2MAX,
                                                ModelStandardTransform.POLARITY_BIPOLAR),
                                        fvalue, new ModelDestination(dest)));
                    }

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_MODLFOTOVOLUME) != 0) {
                        double fvalue = getGeneratorValue(generators,
                                SF2Region.GENERATOR_MODLFOTOVOLUME);
                        ModelIdentifier src = ModelSource.SOURCE_LFO1;
                        ModelIdentifier dest = ModelDestination.DESTINATION_GAIN;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(
                                        new ModelSource(src,
                                                ModelStandardTransform.DIRECTION_MIN2MAX,
                                                ModelStandardTransform.POLARITY_BIPOLAR),
                                        fvalue, new ModelDestination(dest)));
                    }

                    if (layerzone.getShort(SF2Region.GENERATOR_KEYNUM) != -1) {
                        double val = layerzone.getShort(SF2Region.GENERATOR_KEYNUM) / 128.0;
                        addValue(performer, ModelDestination.DESTINATION_KEYNUMBER, val);
                    }

                    if (layerzone.getShort(SF2Region.GENERATOR_VELOCITY) != -1) {
                        double val = layerzone.getShort(SF2Region.GENERATOR_VELOCITY)
                                / 128.0;
                        addValue(performer, ModelDestination.DESTINATION_VELOCITY, val);
                    }

                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_INITIALFILTERFC) < 13500) {
                        short filter_freq = getGeneratorValue(generators,
                                SF2Region.GENERATOR_INITIALFILTERFC);
                        short filter_q = getGeneratorValue(generators,
                                SF2Region.GENERATOR_INITIALFILTERQ);
                        addValue(performer,
                                ModelDestination.DESTINATION_FILTER_FREQ, filter_freq);
                        addValue(performer,
                                ModelDestination.DESTINATION_FILTER_Q, filter_q);
                    }

                    int tune = 100 * getGeneratorValue(generators,
                            SF2Region.GENERATOR_COARSETUNE);
                    tune += getGeneratorValue(generators,
                            SF2Region.GENERATOR_FINETUNE);
                    if (tune != 0) {
                        addValue(performer,
                                ModelDestination.DESTINATION_PITCH, (short) tune);
                    }
                    if (getGeneratorValue(generators, SF2Region.GENERATOR_PAN) != 0) {
                        short val = getGeneratorValue(generators,
                                SF2Region.GENERATOR_PAN);
                        addValue(performer, ModelDestination.DESTINATION_PAN, val);
                    }
                    if (getGeneratorValue(generators, SF2Region.GENERATOR_INITIALATTENUATION) != 0) {
                        short val = getGeneratorValue(generators,
                                SF2Region.GENERATOR_INITIALATTENUATION);
                        addValue(performer,
                                ModelDestination.DESTINATION_GAIN, -0.376287f * val);
                    }
                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_CHORUSEFFECTSSEND) != 0) {
                        short val = getGeneratorValue(generators,
                                SF2Region.GENERATOR_CHORUSEFFECTSSEND);
                        addValue(performer, ModelDestination.DESTINATION_CHORUS, val);
                    }
                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_REVERBEFFECTSSEND) != 0) {
                        short val = getGeneratorValue(generators,
                                SF2Region.GENERATOR_REVERBEFFECTSSEND);
                        addValue(performer, ModelDestination.DESTINATION_REVERB, val);
                    }
                    if (getGeneratorValue(generators,
                            SF2Region.GENERATOR_SCALETUNING) != 100) {
                        short fvalue = getGeneratorValue(generators,
                                SF2Region.GENERATOR_SCALETUNING);
                        ModelIdentifier dest = ModelDestination.DESTINATION_PITCH;
                        if (fvalue == 0) {
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(null, rootkey * 100,
                                            new ModelDestination(dest)));
                        } else {
                            performer.getConnectionBlocks().add(
                                    new ModelConnectionBlock(null, rootkey * (100 - fvalue),
                                            new ModelDestination(dest)));
                        }

                        ModelIdentifier src = ModelSource.SOURCE_NOTEON_KEYNUMBER;
                        performer.getConnectionBlocks().add(
                                new ModelConnectionBlock(new ModelSource(src),
                                        128 * fvalue, new ModelDestination(dest)));

                    }

                    performer.getConnectionBlocks().add(
                            new ModelConnectionBlock(
                                    new ModelSource(ModelSource.SOURCE_NOTEON_VELOCITY,
                                            new ModelTransform() {
                                                public double transform(double value) {
                                                    if (value < 0.5)
                                                        return 1 - value * 2;
                                                    else
                                                        return 0;
                                                }
                                            }),
                                    -2400,
                                    new ModelDestination(
                                            ModelDestination.DESTINATION_FILTER_FREQ)));


                    performer.getConnectionBlocks().add(
                            new ModelConnectionBlock(
                                    new ModelSource(ModelSource.SOURCE_LFO2,
                                            ModelStandardTransform.DIRECTION_MIN2MAX,
                                            ModelStandardTransform.POLARITY_BIPOLAR,
                                            ModelStandardTransform.TRANSFORM_LINEAR),
                                    new ModelSource(new ModelIdentifier("midi_cc", "1", 0),
                                            ModelStandardTransform.DIRECTION_MIN2MAX,
                                            ModelStandardTransform.POLARITY_UNIPOLAR,
                                            ModelStandardTransform.TRANSFORM_LINEAR),
                                    50, new ModelDestination(
                                    ModelDestination.DESTINATION_PITCH)));

                }
            }
        }
        return performers;
    }

    private void addTimecentValue(ModelPerformer performer, ModelIdentifier dest, short value) {
        double fvalue;
        if (value == -12000)
            fvalue = Double.NEGATIVE_INFINITY;
        else
            fvalue = value;
        performer.getConnectionBlocks().add(new ModelConnectionBlock(fvalue, new ModelDestination(dest)));
    }

    private void addValue(ModelPerformer performer, ModelIdentifier dest, short value) {
        performer.getConnectionBlocks().add(new ModelConnectionBlock(value, new ModelDestination(dest)));
    }

    private void addValue(ModelPerformer performer, ModelIdentifier dest, double value) {
        performer.getConnectionBlocks().add(new ModelConnectionBlock(value, new ModelDestination(dest)));
    }

    private short getGeneratorValue(Map<Integer, Short> generators, int gen) {
        if (generators.containsKey(gen))
            return generators.get(gen);
        return SF2Region.getDefaultValue(gen);
    }
}

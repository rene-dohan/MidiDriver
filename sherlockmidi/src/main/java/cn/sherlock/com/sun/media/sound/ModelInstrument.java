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

import jp.kshoji.javax.sound.midi.Instrument;
import jp.kshoji.javax.sound.midi.Patch;
import jp.kshoji.javax.sound.midi.Soundbank;

/**
 * The model instrument class.
 *
 * <p>The main methods to override are:<br>
 * getPerformer, getDirector, getChannelMixer.
 *
 * <p>Performers are used to define what voices which will
 * playback when using the instrument.<br>
 *
 * ChannelMixer is used to add channel-wide processing
 * on voices output or to define non-voice oriented instruments.<br>
 *
 * Director is used to change how the synthesizer
 * chooses what performers to play on midi events.
 *
 * @author Karl Helgason
 */
public abstract class ModelInstrument extends Instrument {

    protected ModelInstrument(Soundbank soundbank, Patch patch, String name,
            Class<?> dataClass) {
        super(soundbank, patch, name, dataClass);
    }

    public ModelDirector getDirector(ModelPerformer[] performers,
                                     ModelDirectedPlayer player) {
        return new ModelStandardIndexedDirector(performers, player);
    }

    public ModelPerformer[] getPerformers() {
        return new ModelPerformer[0];
    }

    public ModelChannelMixer getChannelMixer() {
        return null;
    }

}

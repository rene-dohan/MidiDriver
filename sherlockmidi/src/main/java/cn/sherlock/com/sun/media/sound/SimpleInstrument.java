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
import java.util.List;
import jp.kshoji.javax.sound.midi.Patch;

/**
 * A simple instrument that is made of other ModelInstrument, ModelPerformer
 * objects.
 *
 * @author Karl Helgason
 */
public class SimpleInstrument extends ModelInstrument {

    private static class SimpleInstrumentPart {
        ModelPerformer[] performers;
        int keyFrom;
        int keyTo;
        int velFrom;
        int velTo;
        int exclusiveClass;
    }
    protected int preset = 0;
    protected int bank = 0;
    protected boolean percussion = false;
    protected String name = "";
    protected List<SimpleInstrumentPart> parts
            = new ArrayList<SimpleInstrumentPart>();

    public SimpleInstrument() {
        super(null, null, null, null);
    }

    public void add(ModelPerformer[] performers, int keyFrom, int keyTo,
            int velFrom, int velTo, int exclusiveClass) {
        SimpleInstrumentPart part = new SimpleInstrumentPart();
        part.performers = performers;
        part.keyFrom = keyFrom;
        part.keyTo = keyTo;
        part.velFrom = velFrom;
        part.velTo = velTo;
        part.exclusiveClass = exclusiveClass;
        parts.add(part);
    }

    public void add(ModelPerformer[] performers) {
        add(performers, 0, 127, 0, 127, -1);
    }

    public void add(ModelPerformer performer) {
        add(new ModelPerformer[]{performer});
    }

    public Object getData() {
        return null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPatch(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch)patch).isPercussion()) {
            percussion = true;
            bank = patch.getBank();
            preset = patch.getProgram();
        } else {
            percussion = false;
            bank = patch.getBank();
            preset = patch.getProgram();
        }
    }
}

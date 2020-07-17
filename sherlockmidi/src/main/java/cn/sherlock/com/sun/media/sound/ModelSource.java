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

/**
 * This class is used to identify sources in connection blocks,
 * see ModelConnectionBlock.
 *
 * @author Karl Helgason
 */
public class ModelSource {

    public static final ModelIdentifier SOURCE_NOTEON_KEYNUMBER =
            new ModelIdentifier("noteon", "keynumber");     // midi keynumber
    public static final ModelIdentifier SOURCE_NOTEON_VELOCITY =
            new ModelIdentifier("noteon", "velocity");      // midi velocity
    public static final ModelIdentifier SOURCE_EG2 =
            new ModelIdentifier("eg", null, 1);
    public static final ModelIdentifier SOURCE_LFO1 =
            new ModelIdentifier("lfo", null, 0);
    public static final ModelIdentifier SOURCE_LFO2 =
            new ModelIdentifier("lfo", null, 1);
    private ModelIdentifier source;
    private ModelTransform transform;

    public ModelSource(ModelIdentifier id) {
        source = id;
        this.transform = new ModelStandardTransform();
    }

    public ModelSource(ModelIdentifier id, boolean direction, boolean polarity) {
        source = id;
        this.transform = new ModelStandardTransform(direction, polarity);
    }

    public ModelSource(ModelIdentifier id, boolean direction, boolean polarity,
            int transform) {
        source = id;
        this.transform =
                new ModelStandardTransform(direction, polarity, transform);
    }

    public ModelSource(ModelIdentifier id, ModelTransform transform) {
        source = id;
        this.transform = transform;
    }

    public ModelIdentifier getIdentifier() {
        return source;
    }

    public ModelTransform getTransform() {
        return transform;
    }

}

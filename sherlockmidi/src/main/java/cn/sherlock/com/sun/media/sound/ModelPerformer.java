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

/**
 * This class is used to define how to synthesize audio in universal maner
 * for both SF2 and DLS instruments.
 *
 * @author Karl Helgason
 */
public class ModelPerformer {

    private List<ModelByteBufferWavetable> oscillators = new ArrayList<>();
    private List<ModelConnectionBlock> connectionBlocks = new ArrayList<>();
    private int keyFrom = 0;
    private int keyTo = 127;
    private int velFrom = 0;
    private int velTo = 127;
    private int exclusiveClass = 0;

    public List<ModelConnectionBlock> getConnectionBlocks() {
        return connectionBlocks;
    }

    public List<ModelByteBufferWavetable> getOscillators() {
        return oscillators;
    }

    public int getExclusiveClass() {
        return exclusiveClass;
    }

    public void setExclusiveClass(int exclusiveClass) {
        this.exclusiveClass = exclusiveClass;
    }

    public boolean isSelfNonExclusive() {
        return false;
    }

    public int getKeyFrom() {
        return keyFrom;
    }

    public void setKeyFrom(int keyFrom) {
        this.keyFrom = keyFrom;
    }

    public int getKeyTo() {
        return keyTo;
    }

    public void setKeyTo(int keyTo) {
        this.keyTo = keyTo;
    }

    public int getVelFrom() {
        return velFrom;
    }

    public void setVelFrom(int velFrom) {
        this.velFrom = velFrom;
    }

    public int getVelTo() {
        return velTo;
    }

    public void setVelTo(int velTo) {
        this.velTo = velTo;
    }

    public boolean isReleaseTriggered() {
        return false;
    }

    public boolean isDefaultConnectionsEnabled() {
        return true;
    }

}

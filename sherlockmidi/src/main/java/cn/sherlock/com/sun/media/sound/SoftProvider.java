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

/**
 * Software synthesizer provider class.
 *
 * @author Karl Helgason
 */
public class SoftProvider {

    /**
     * Constructor
     */
    public SoftProvider() {
    }

    /**
     * Check if the specified Device is supported
     *
     * @param info the information
     * @return true if the Device is supported
     */
    public boolean isDeviceSupported(@NonNull AudioSynthesizer.Info info) {
        AudioSynthesizer.Info[] informationArray = getDeviceInfo();

        for (AudioSynthesizer.Info information : informationArray) {
            if (info.equals(information)) {
                return true;
            }
        }

        return false;
    }

    protected final static AudioSynthesizer.Info softinfo = SoftSynthesizer.info;
    private static AudioSynthesizer.Info[] softinfos = {softinfo};

    public AudioSynthesizer.Info[] getDeviceInfo() {
        return softinfos;
    }

    public AudioSynthesizer getDevice(AudioSynthesizer.Info info) {
        if (info == softinfo) {
            return new SoftSynthesizer();
        }
        return null;
    }
}

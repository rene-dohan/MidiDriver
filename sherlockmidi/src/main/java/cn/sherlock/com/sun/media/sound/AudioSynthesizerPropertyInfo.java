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
 * Information about property used in  opening <code>AudioSynthesizer</code>.
 *
 * @author Karl Helgason
 */
public class AudioSynthesizerPropertyInfo {

    /**
     * Constructs a <code>AudioSynthesizerPropertyInfo</code> object with a given
     * name and value. The <code>description</code> and <code>choices</code>
     * are intialized by <code>null</code> values.
     *
     * @param value the current value or class used for values.
     *
     */
    public AudioSynthesizerPropertyInfo(Object value) {
        if (!(value instanceof Class)) {
            this.value = value;
        }
    }

    /**
     * The <code>value</code> field specifies the current value of
     * the property.
     */
    public Object value = null;

}

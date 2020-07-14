package cn.sherlock.javax.sound.sampled;

/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

import java.io.InputStream;

import cn.sherlock.media.SourceDataLineImpl;

/* $fb TODO:
 * - consistent usage of (typed) collections
 */

/**
 * The <code>AudioSystem</code> class acts as the entry point to the
 * sampled-audio system resources. This class lets you query and access the
 * mixers that are installed on the system. <code>AudioSystem</code> includes a
 * number of methods for converting audio data between different formats, and
 * for translating between audio files and streams. It also provides a method
 * for obtaining a <code>{@link Line}</code> directly from the
 * <code>AudioSystem</code> without dealing explicitly with mixers.
 *
 * <p>
 * Properties can be used to specify the default mixer for specific line types.
 * Both system properties and a properties file are considered. The
 * <code>sound.properties</code> properties file is read from an
 * implementation-specific location (typically it is the <code>lib</code>
 * directory in the Java installation directory). If a property exists both as a
 * system property and in the properties file, the system property takes
 * precedence. If none is specified, a suitable default is chosen among the
 * available devices. The syntax of the properties file is specified in
 * {@link java.util.Properties#load(InputStream) Properties.load}. The following
 * table lists the available property keys and which methods consider them:
 *
 * <table border=0>
 * <caption>Audio System Property Keys</caption>
 * <tr>
 * <th>Property Key</th>
 * <th>Interface</th>
 * <th>Affected Method(s)</th>
 * </tr>
 * <tr>
 * <td><code>javax.sound.sampled.Clip</code></td>
 * <td>{@link Clip}</td>
 * <td>{@link #getLine}, {@link #getClip}</td>
 * </tr>
 * <tr>
 * <td><code>javax.sound.sampled.Port</code></td>
 * <td>{@link Port}</td>
 * <td>{@link #getLine}</td>
 * </tr>
 * <tr>
 * <td><code>javax.sound.sampled.SourceDataLine</code></td>
 * <td>{@link SourceDataLineImpl}</td>
 * </tr>
 * <tr>
 * <td><code>javax.sound.sampled.TargetDataLine</code></td>
 * <td>{@link TargetDataLine}</td>
 * <td>{@link #getLine}, {@link #getTargetDataLine}</td>
 * </tr>
 * </table>
 *
 * The property value consists of the provider class name and the mixer name,
 * separated by the hash mark (&quot;#&quot;). The provider class name is the
 * fully-qualified name of a concrete
 * {@link javax.sound.sampled.spi.MixerProvider mixer provider} class. The mixer
 * name is matched against the <code>String</code> returned by the
 * <code>getName</code> method of <code>Mixer.Info</code>. Either the class
 * name, or the mixer name may be omitted. If only the class name is specified,
 * the trailing hash mark is optional.
 *
 * <p>
 * If the provider class is specified, and it can be successfully retrieved from
 * the installed providers, the list of <code>Mixer.Info</code> objects is
 * retrieved from the provider. Otherwise, or when these mixers do not provide a
 * subsequent match, the list is retrieved from {@link #getMixerInfo} to contain
 * all available <code>Mixer.Info</code> objects.
 *
 * <p>
 * If a mixer name is specified, the resulting list of <code>Mixer.Info</code>
 * objects is searched: the first one with a matching name, and whose
 * <code>Mixer</code> provides the respective line interface, will be returned.
 * If no matching <code>Mixer.Info</code> object is found, or the mixer name is
 * not specified, the first mixer from the resulting list, which provides the
 * respective line interface, will be returned.
 *
 * For example, the property <code>javax.sound.sampled.Clip</code> with a value
 * <code>&quot;com.sun.media.sound.MixerProvider#SunClip&quot;</code> will have
 * the following consequences when <code>getLine</code> is called requesting a
 * <code>Clip</code> instance: if the class
 * <code>com.sun.media.sound.MixerProvider</code> exists in the list of
 * installed mixer providers, the first <code>Clip</code> from the first mixer
 * with name <code>&quot;SunClip&quot;</code> will be returned. If it cannot be
 * found, the first <code>Clip</code> from the first mixer of the specified
 * provider will be returned, regardless of name. If there is none, the first
 * <code>Clip</code> from the first <code>Mixer</code> with name
 * <code>&quot;SunClip&quot;</code> in the list of all mixers (as returned by
 * <code>getMixerInfo</code>) will be returned, or, if not found, the first
 * <code>Clip</code> of the first <code>Mixer</code>that can be found in the
 * list of all mixers is returned. If that fails, too, an
 * <code>IllegalArgumentException</code> is thrown.
 *
 * @author Kara Kytle
 * @author Florian Bomers
 * @author Matthias Pfisterer
 * @author Kevin P. Smith
 *
 * @see AudioFormat
 * @see AudioInputStream
 * @see Mixer
 * @since 1.3
 */
public class AudioSystem {

	/**
	 * An integer that stands for an unknown numeric value. This value is
	 * appropriate only for signed quantities that do not normally take negative
	 * values. Examples include file sizes, frame sizes, buffer sizes, and
	 * sample rates. A number of Java Sound constructors accept a value of
	 * <code>NOT_SPECIFIED</code> for such parameters. Other methods may also
	 * accept or return this value, as documented.
	 */
	public static final int NOT_SPECIFIED = -1;

	/**
	 * Obtains an audio input stream of the indicated format, by converting the
	 * provided audio input stream.
	 * 
	 * @param targetFormat
	 *            the desired audio format after conversion
	 * @param sourceStream
	 *            the stream to be converted
	 * @return an audio input stream of the indicated format
	 * @throws IllegalArgumentException
	 *             if the conversion is not supported #see
	 *             #getTargetEncodings(AudioFormat)
	 */
	public static AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {

		if (sourceStream.getFormat().matches(targetFormat)) {
			return sourceStream;
		}

		// we ran out of options...
		throw new IllegalArgumentException("Unsupported conversion: " + targetFormat + " from " + sourceStream.getFormat());
	}


	// $$fb 2002-04-12: fix for 4662082: behavior of
	// AudioSystem.getTargetEncodings() methods doesn't match the spec


}

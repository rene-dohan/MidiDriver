/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package cn.sherlock.javax.sound.sampled.spi;

import cn.sherlock.javax.sound.sampled.AudioFormat;
import cn.sherlock.javax.sound.sampled.AudioInputStream;

/**
 * A format conversion provider provides format conversion services from one or
 * more input formats to one or more output formats. Converters include codecs,
 * which encode and/or decode audio data, as well as transcoders, etc. Format
 * converters provide methods for determining what conversions are supported and
 * for obtaining an audio stream from which converted data can be read.
 * <p>
 * The source format represents the format of the incoming audio data, which
 * will be converted.
 * <p>
 * The target format represents the format of the processed, converted audio
 * data. This is the format of the data that can be read from the stream
 * returned by one of the <code>getAudioInputStream</code> methods.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class FormatConversionProvider {

	// NEW METHODS

    /**
	 * Obtains the set of target formats with the encoding specified supported
	 * by the format converter If no target formats with the specified encoding
	 * are supported for this source format, an array of length 0 is returned.
	 * 
	 * @param targetEncoding
	 *            desired encoding of the stream after processing
	 * @param sourceFormat
	 *            format of the incoming data
	 * @return array of supported target formats.
	 */
	public abstract AudioFormat[] getTargetFormats(
			AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat);

	/**
	 * Indicates whether the format converter supports conversion to one
	 * particular format from another.
	 * 
	 * @param targetFormat
	 *            desired format of outgoing data
	 * @param sourceFormat
	 *            format of the incoming data
	 * @return <code>true</code> if the conversion is supported, otherwise
	 *         <code>false</code>
	 */
	public boolean isConversionSupported(AudioFormat targetFormat,
			AudioFormat sourceFormat) {

		AudioFormat[] targetFormats = getTargetFormats(
				targetFormat.getEncoding(), sourceFormat);

		for (AudioFormat format : targetFormats) {
			if (targetFormat.matches(format)) {
				return true;
			}
		}
		return false;
	}

    /**
	 * Obtains an audio input stream with the specified format from the given
	 * audio input stream.
	 * 
	 * @param targetFormat
	 *            desired data format of the stream after processing
	 * @param sourceStream
	 *            stream from which data to be processed should be read
	 * @return stream from which processed data with the specified format may be
	 *         read
	 * @throws IllegalArgumentException
	 *             if the format combination supplied is not supported.
	 */
	public abstract AudioInputStream getAudioInputStream(
			AudioFormat targetFormat, AudioInputStream sourceStream);

}

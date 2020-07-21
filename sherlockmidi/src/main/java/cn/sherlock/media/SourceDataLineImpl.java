package cn.sherlock.media;

import android.media.AudioManager;
import android.media.AudioTrack;
import cn.sherlock.javax.sound.sampled.AudioFormat;

public class SourceDataLineImpl {

	private AudioTrack audioTrack;

	public void start() {
		if (audioTrack != null) {
			audioTrack.play();
		}
	}

	public void close() {
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}
	}

	public SourceDataLineImpl(AudioFormat format, int bufferSize) {
		// Get the smallest buffer to minimize latency.
		int sampleRateInHz = (int) format.getSampleRate();
		// int sampleSizeInBit = format.getSampleSizeInBits();
		int channelConfig;
		if (format.getChannels() == 1) {
			channelConfig = android.media.AudioFormat.CHANNEL_OUT_MONO;
		} else if (format.getChannels() == 2) {
			channelConfig = android.media.AudioFormat.CHANNEL_OUT_STEREO;
		} else {
			throw new IllegalArgumentException(
					"format.getChannels() must in (1,2)");
		}
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
				channelConfig, android.media.AudioFormat.ENCODING_PCM_16BIT,
				bufferSize, AudioTrack.MODE_STREAM);
	}

	public int write(byte[] b, int off, int len) {
		if(audioTrack != null){
			return audioTrack.write(b, off, len);
		}
		return 0;
	}

}

package cn.sherlock.media;

import android.media.AudioManager;
import android.media.AudioTrack;
import cn.sherlock.javax.sound.sampled.AudioFormat;
import cn.sherlock.javax.sound.sampled.SourceDataLine;

public class SourceDataLineImpl implements SourceDataLine {

	private AudioTrack audioTrack;
	private int bufferSize;

	public SourceDataLineImpl() {
	}

	@Override
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

	@Override
	public boolean isOpen() {
		return audioTrack != null;
	}

	private boolean isRunning() {
		if (audioTrack != null) {
			return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
		}
		return false;
	}

	@Override
	public boolean isActive() {
		return isRunning();
	}

	@Override
	public int getBufferSize() {
		if (audioTrack != null) {
			return bufferSize;
		}
		return 0;
	}

	@Override
	public void open(AudioFormat format, int bufferSize) {
		// Get the smallest buffer to minimize latency.
		this.bufferSize = bufferSize;
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

	@Override
	public int write(byte[] b, int off, int len) {
		if(audioTrack != null){
			return audioTrack.write(b, off, len);
		}
		return 0;
	}

}

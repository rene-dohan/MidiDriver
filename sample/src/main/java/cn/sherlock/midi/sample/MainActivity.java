package cn.sherlock.midi.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Patch;

public class MainActivity extends Activity {

	private SoftSynthesizer synth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			synth = new SoftSynthesizer();
			synth.open();
			synth.loadInstruments(
					new SF2Soundbank(getAssets().open("gm.sf2")),
					new Patch[]{new Patch(0, 1)}
			);
		} catch (IOException | MidiUnavailableException e) {
			throw new RuntimeException(e);
		}

		this.findViewById(R.id.piano).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
					new Thread(playRunnable).start();
				}
				return true;
			}
		});
	}

	private Runnable playRunnable = new Runnable() {

		@Override
		public void run() {
			int pause = 500;
			MidiChannel channel = synth.getChannels()[0];
			channel.programChange(1);

			try {
				channel.noteOn(60, 64);
				Thread.sleep(pause);
				channel.noteOff(60);

				channel.noteOn(62, 64);
				Thread.sleep(pause);
				channel.noteOff(62);

				channel.noteOn(64, 64);
				Thread.sleep(pause);
				channel.noteOff(64);

				channel.noteOn(65, 64);
				Thread.sleep(pause);
				channel.noteOff(65);

				channel.noteOn(67, 64);
				Thread.sleep(pause);
				channel.noteOff(67);

			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (synth != null) {
			synth.close();
		}
	}
}

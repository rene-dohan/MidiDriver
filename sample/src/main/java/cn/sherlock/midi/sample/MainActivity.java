package cn.sherlock.midi.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

import cn.sherlock.com.sun.media.sound.SF2Instrument;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.Patch;

public class MainActivity extends Activity {

	private SoftSynthesizer synth;
	private SF2Soundbank soundbank;
	private SF2Instrument currentInstrument;
	private MidiChannel channel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream soundbankStream = getAssets().open("gm.sf2");
					soundbank = new SF2Soundbank(soundbankStream);
					synth = new SoftSynthesizer();
					channel = synth.getChannels()[0];

					synth.open();
					changeInstrument(1); // Piano is program 1
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
		this.findViewById(R.id.piano).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							int pause = 500;
							channel.controlChange(65, 127);
							channel.controlChange(5, 80);
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
					}).start();
				}
				return true;
			}
		});

		this.findViewById(R.id.woodblock).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
					synth.unloadInstrument(currentInstrument);
					changeInstrument(1 + (int)(Math.random() * 127));
				}
				return true;
			}
		});
	}

	private void changeInstrument(int program) {
		currentInstrument = soundbank.getInstrument(new Patch(0, program));
		synth.loadInstrument(currentInstrument);
		channel.programChange(program);
		Log.d("NEW INSTRUMENT", "PROGRAM " + program);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (synth != null) {
			synth.close();
		}
	}
}

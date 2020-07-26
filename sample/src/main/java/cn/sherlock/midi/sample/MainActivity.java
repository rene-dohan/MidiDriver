package cn.sherlock.midi.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;

import com.sun.media.sound.SF2Instrument;
import com.sun.media.sound.SF2Soundbank;
import com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class MainActivity extends Activity {

	private Soundbank soundbank;
	private Synthesizer synth;
	private MidiChannel channel;
	private Instrument currentInstrument;

	private static void printInstrumentDetails(Instrument instrument) {
		Patch patch = instrument.getPatch();
		Log.d("INSTRUMENT",instrument.getName() + " - (" + patch.getBank() + ", " + patch.getProgram() + ")");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread(() -> {
			try {
				synth = new SoftSynthesizer();
				if (!synth.isOpen()) synth.open();

				//InputStream soundbankStream = getAssets().open("gm.sf2");
				//soundbank = new SF2Soundbank(soundbankStream);
				soundbank = synth.getDefaultSoundbank();

				Log.d("INSTRUMENT","Instruments");
				Log.d("INSTRUMENT","----------------");
				for (Instrument instrument : soundbank.getInstruments()) {
					printInstrumentDetails(instrument);
				}

				channel = synth.getChannels()[0];

				changeInstrument(20); // Piano is program 1
			} catch (MidiUnavailableException e) {
				throw new RuntimeException(e);
			}
		}).start();

		this.findViewById(R.id.piano).setOnTouchListener((v, event) -> {
			v.performClick();
			if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
				new Thread(() -> {
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
				}).start();
			}
			return true;
		});

		this.findViewById(R.id.woodblock).setOnTouchListener((v, event) -> {
			v.performClick();
			if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
				synth.unloadInstrument(currentInstrument);
				changeInstrument(1 + (int)(Math.random() * 127));
			}
			return true;
		});
	}

	private void changeInstrument(int program) {
		currentInstrument = soundbank.getInstrument(new Patch(0, program));
		synth.loadInstrument(currentInstrument);
		channel.programChange(program);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (synth != null) {
			synth.close();
		}
	}
}
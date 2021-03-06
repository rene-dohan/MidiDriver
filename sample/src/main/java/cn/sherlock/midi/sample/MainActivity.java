package cn.sherlock.midi.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

import cn.sherlock.com.sun.media.sound.MidiDeviceReceiver;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MainActivity extends Activity {

	private SoftSynthesizer synth;
	private MidiDeviceReceiver recv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			SF2Soundbank sf = new SF2Soundbank(getAssets().open("gm.sf2"));
			synth = new SoftSynthesizer();
			synth.open();
			synth.loadAllInstruments(sf);
			synth.getChannels()[0].programChange(0);
			synth.getChannels()[1].programChange(1);
			recv = synth.getReceiver();
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
		private int pause = 500;

		@Override
		public void run() {
			try {
				ShortMessage sm = new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 64);
				recv.send(sm);
				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 64, 64));
				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 67, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 60, 64));
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 64, 64));
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 67, 64));

				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 60, 64));

				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 64, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 64, 64));

				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 67, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 67, 64));

				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 64, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 64, 64));

				recv.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 64));
				Thread.sleep(pause);
				recv.send(new ShortMessage(ShortMessage.NOTE_OFF, 1, 60, 64));
			} catch (InvalidMidiDataException | InterruptedException e) {
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

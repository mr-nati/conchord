package com.conchord.android;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button buttonPlay;
	Button buttonFfwd;
	MediaPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set up MediaPlayer
		mPlayer = MediaPlayer.create(getApplicationContext(),
				R.raw.the_alan_parsons_project_sirius);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		buttonPlay = (Button) findViewById(R.id.button_play);
		buttonFfwd = (Button) findViewById(R.id.button_ffwd);

		buttonPlay.setText("play");
		buttonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayer.isPlaying()) {
					Toast.makeText(getApplicationContext(), "already playing",
							Toast.LENGTH_SHORT).show();
				} else {
					mPlayer.start();
					buttonPlay.setText("pause");
				}
			}
		});

		buttonFfwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					mPlayer.seekTo(mPlayer.getCurrentPosition() + 5000);
					mPlayer.start();
				} else {
					Toast.makeText(getApplicationContext(), "press play first",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

package com.conchord.android;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.conchord.android.util.MediaFiles;

public class MainActivity extends Activity {

	static Button buttonPlay;
	static Button buttonFfwd;
	static MediaPlayer mPlayer;
	
	static Button buttonOtherActivity;
	
	private PendingIntent pIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		Intent myIntent = new Intent(MainActivity.this, MyAlarmService.class);
		
		pIntent = PendingIntent.getService(MainActivity.this, 0, myIntent, 0);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(System.currentTimeMillis());
		
		cal.set(Calendar.MINUTE, 46);
		cal.set(Calendar.SECOND, 0);
		
	//	cal.add(Calendar.SECOND, 20);
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);
		Toast.makeText(this, "set time for " + cal.toString(), Toast.LENGTH_SHORT).show();
		

		// set up MediaPlayer
		mPlayer = MediaPlayer.create(getApplicationContext(),
				MediaFiles.power_trip);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		buttonPlay = (Button) findViewById(R.id.button_play);
		buttonFfwd = (Button) findViewById(R.id.button_ffwd);
		
		buttonOtherActivity = (Button) findViewById(R.id.other_activity);
		
		buttonPlay.setText("play");
		buttonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clickedPlayButton();
			}
		});

		buttonFfwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clickedFfwdButton(getApplicationContext());
			}
		});
		
		buttonOtherActivity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), OtherActivity.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static void clickedPlayButton() {
		if (mPlayer.isPlaying()) {
/*				Toast.makeText(getApplicationContext(), "already playing",
					Toast.LENGTH_SHORT).show();*/
			mPlayer.pause();
			buttonPlay.setText("resume");
		} else {
			mPlayer.start();
			buttonPlay.setText("pause");
		}
	}
	
	public static void clickedFfwdButton(Context context) {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			mPlayer.seekTo(mPlayer.getCurrentPosition() + 5000);
			mPlayer.start();
		} else {
			Toast.makeText(context, "press play first",
					Toast.LENGTH_SHORT).show();
		}
	}

}

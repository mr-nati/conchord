package com.conchord.android.util;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.conchord.android.MyAlarmService;
import com.conchord.android.OtherActivity;
import com.conchord.android.R;
import com.conchord.android.R.id;
import com.conchord.android.R.layout;
import com.conchord.android.R.menu;
import com.conchord.android.R.raw;

public class OldMainActivity extends Activity implements LocationListener {

	static Button buttonPlay;
	static Button buttonFfwd;
	public static MediaPlayer mPlayer;
	static Button buttonOtherActivity;

	static Button buttonGetNTPtime;
	static Button buttonSetNewPlayTime;
	static TextView textViewPlayTime;
	static TextView textViewNTPtime;

	private PendingIntent pIntent;
	private AlarmManager alarmManager;
	private LocationManager locMngr;
	private static final String TAG = "MainActivity";

	private boolean haveALocation = false;
	private long timeToPlayAtInMillis = 1381651530000L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialize();
		// setupButtons();

		// set up MediaPlayer
		if (android.os.Build.VERSION.SDK_INT < 9) {
			mPlayer = MediaPlayer.create(getApplicationContext(),
					MediaFiles.call_me_acapella);
		} else {
			mPlayer = MediaPlayer.create(getApplicationContext(),
					MediaFiles.call_me_instrumental);
		}
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		

		textViewPlayTime = (TextView) findViewById(R.id.textViewPlayTime);
		textViewPlayTime.setText(new Date(timeToPlayAtInMillis).toGMTString());
		textViewNTPtime = (TextView) findViewById(R.id.textViewNTPtime);
		buttonGetNTPtime = (Button) findViewById(R.id.button_getNTPtime);
		buttonGetNTPtime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textViewNTPtime.setText(new Date(getNTPtime()).toGMTString());
			}
		});
		buttonSetNewPlayTime = (Button) findViewById(R.id.button_setNewPlayTime);
		buttonSetNewPlayTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPlayer.pause();
				mPlayer.create(getApplicationContext(),
						R.raw.lecrae_power_trip);
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				
				timeToPlayAtInMillis += 20000;
				textViewPlayTime.setText(new Date(timeToPlayAtInMillis)
						.toGMTString());
				// Reset alarm manager
				alarmManager.set(AlarmManager.RTC_WAKEUP, timeToPlayAtInMillis,
						pIntent);
			}
		});

		// Subscribe to Location Updates
		// locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
		// this);

		// makeSureGPSisEnabledOnDevice();

		// 1. GPS time to play sound at in milliseconds
		/* timeToPlayAtInMillis = System.currentTimeMillis() + 25000; */

		// 2. Get amount of milliseconds play time is from now
		long currentTimeInMillis = getNTPtime();
		long millisLeft = timeToPlayAtInMillis - currentTimeInMillis;

		// 3. Add "milliseconds away" to current system time
		// setAlarmPlayTime(millisLeft);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + millisLeft);
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);

		Toast.makeText(getBaseContext(),
				"play time is at " + cal.getTime().toGMTString(),
				Toast.LENGTH_SHORT).show();

		// 4. Set alarm for future time in millis

	}

	private long getNTPtime() {
		SntpClient client = new SntpClient();
		if (client.requestTime(Utils.someCaliNtpServers[0], 10000)) {

			return client.getNtpTime() + SystemClock.elapsedRealtime()
					- client.getNtpTimeReference();
		} else {
			Toast.makeText(getBaseContext(), "NTP Request failed",
					Toast.LENGTH_LONG).show();
			return 0;
		}
	}

	@Override
	public void onLocationChanged(Location loc) {
		// haveALocation = true;
		//
		// long currentGpsTime = loc.getTime();
		// Toast.makeText(getBaseContext(),
		// "current gps time: " + new Date(currentGpsTime).toGMTString(),
		// Toast.LENGTH_LONG).show();
		// long millisecondsAway = timeToPlayAtInMillis - currentGpsTime;

		// Toast.makeText(getBaseContext(),
		// "playing sound in " + (millisecondsAway / 1000) + " seconds",
		// Toast.LENGTH_SHORT).show();

		// setAlarmPlayTime(millisecondsAway);
	}

	private void initialize() {
		setContentView(R.layout.activity_main);

		// Initialize the intent to start alarm service
		Intent myIntent = new Intent(OldMainActivity.this, MyAlarmService.class);

		// Initialize the pendingIntent to start the Intent
		pIntent = PendingIntent.getService(OldMainActivity.this, 0, myIntent, 0);

		// Initialize the alarmManager
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Initialize the locMngr
		locMngr = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	/**
	 * 
	 * @param millisecondsTilPlayTime
	 *           Num of milliseconds from current gps time to play sound at
	 */
	private void setAlarmPlayTime(long millisecondsTilPlayTime) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ millisecondsTilPlayTime, pIntent);
	}

	private void setupButtons() {
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
				Intent intent = new Intent(getApplicationContext(),
						OtherActivity.class);
				startActivity(intent);
			}
		});

		buttonGetNTPtime = (Button) findViewById(R.id.button_getNTPtime);
		buttonGetNTPtime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				long currentTimeInMillis = getNTPtime();
				textViewNTPtime.setText(new Date(currentTimeInMillis).toGMTString());

				long millisLeft = timeToPlayAtInMillis - currentTimeInMillis;
				setAlarmPlayTime(millisLeft);
				Toast.makeText(getBaseContext(),
						"Alarm set for " + (millisLeft / 1000) + " sec from now",
						Toast.LENGTH_SHORT).show();
			}
		});

		buttonSetNewPlayTime = (Button) findViewById(R.id.button_setNewPlayTime);
		buttonSetNewPlayTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				timeToPlayAtInMillis += 20000;
				textViewPlayTime.setText(new Date(timeToPlayAtInMillis)
						.toGMTString());
			}
		});

		textViewPlayTime = (TextView) findViewById(R.id.textViewPlayTime);
		textViewPlayTime.setText(new Date(timeToPlayAtInMillis).toGMTString());
		textViewNTPtime = (TextView) findViewById(R.id.textViewNTPtime);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static void clickedPlayButton() {
		if (mPlayer.isPlaying()) {
			/*
			 * Toast.makeText(getApplicationContext(), "already playing",
			 * Toast.LENGTH_SHORT).show();
			 */
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
			Toast.makeText(context, "press play first", Toast.LENGTH_SHORT).show();
		}
	}

	private void makeSureGPSisEnabledOnDevice() {
		boolean enabled = locMngr.isProviderEnabled(locMngr.GPS_PROVIDER);
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}

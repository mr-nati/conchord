package com.conchord.android.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.util.ConchordMediaPlayer;
import com.conchord.android.util.Constants;
import com.conchord.android.util.SntpClient;
import com.conchord.android.util.Utils;
import com.conchord.android.util.Utils.MediaFiles;

public class SessionActivity extends Activity {

	public static ConchordMediaPlayer mPlayer;
	private static TextView textViewPlayTime;
	private PendingIntent pIntent;
	private AlarmManager alarmManager;
	private long timeToPlayAtInMillis = 1381715350000L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_session);

		String name = getIntent().getStringExtra(Constants.sessionKey);
		Date timeNow = Calendar.getInstance().getTime();
		Utils.createSession(name, timeNow.getMinutes());

		// Initialize the intent to start alarm service
		// Intent myIntent = new Intent(SessionActivity.this,
		// MyAlarmService.class);

		// Initialize the pendingIntent to start the Intent
		// pIntent = PendingIntent.getService(SessionActivity.this, 0, myIntent,
		// 0);

		// Initialize the alarmManager
		// alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		inflateXML();
		// buildMediaPlayers();
		connectToFirebase();

		// 1. Create time to play sound at in milliseconds
		// startSession();

		// 2. Get amount of milliseconds play time is from now
		// long currentTimeInMillis = getNTPtime();
		// long millisLeft = timeToPlayAtInMillis - currentTimeInMillis;

		// 3. Add "milliseconds away" to current system time
		// setAlarmPlayTime(millisLeft);

		// Calendar cal = Calendar.getInstance();
		// cal.setTimeInMillis(System.currentTimeMillis() + millisLeft);
		// setAlarmPlayTime(cal.getTimeInMillis());

		// makeLongToast("play time is at " + cal.getTime().toGMTString());

		// 4. Set alarm for future time in millis
		// setUpFirebase();

	}

	private void connectToFirebase() {
		// Create a reference to a Firebase location
		// Firebase listRef = new Firebase(Utils.firebaseUrl);
		// makeLongToast("connected to: " + Utils.firebaseUrl);
	}

	@SuppressWarnings("deprecation")
	private void inflateXML() {
		textViewPlayTime = (TextView) findViewById(R.id.textViewPlayTime);
		textViewPlayTime.setText(new Date(timeToPlayAtInMillis).toGMTString());
	}

	private void buildMediaPlayers() {
		if (android.os.Build.VERSION.SDK_INT < 9) {
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.call_me_acapella);
		} else {
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.call_me_instrumental);
		}
	}

	private void startSession() {
		// Set start time to 15 seconds from now
		timeToPlayAtInMillis = getNTPtime() + 15000;
	}

	private void setAlarmPlayTime(long millisecondsTilPlayTime) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ millisecondsTilPlayTime, pIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void makeSureGPSisEnabledOnDevice() {
		LocationManager locMngr = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locMngr.isProviderEnabled(locMngr.GPS_PROVIDER);
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
	}

	public void makeLongToast(String text) {
		Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
	}

	private long getNTPtime() {
		SntpClient client = new SntpClient();
		if (client.requestTime(Utils.someCaliNtpServers[0], 10000)) {

			return client.getNtpTime() + SystemClock.elapsedRealtime()
					- client.getNtpTimeReference();
		} else {
			makeLongToast("NTP error");
			return 0;
		}
	}

}
package com.conchord.android.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.util.ConchordMediaPlayer;
import com.conchord.android.util.Constants;
import com.conchord.android.util.Session;
import com.conchord.android.util.SntpClient;
import com.conchord.android.util.Utils;
import com.conchord.android.util.Utils.MediaFiles;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

public class SessionActivity extends Activity {

	public static final String TAG = SessionActivity.class.getSimpleName();
	private TextView textViewMySessionId;

	// Stuff to play music
	public static ConchordMediaPlayer mPlayer;
	private static TextView textViewPlayTime;
	private PendingIntent pIntent;
	private AlarmManager alarmManager;
	private long timeToPlayAtInMillis = 1381715350000L;

	/* Session information */
	private String sessionName;
	private static Firebase sessionFirebase;
	private static Firebase sessionUsersFirebase;

	/* My session id information */
	private String mySessionId;
	private Firebase mySessionUserFirebase;

	private boolean isHost = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_session);

		// Get the session name and whether user is host or not
		sessionName = getIntent().getStringExtra(Constants.KEY_SESSION);
		isHost = getIntent().getBooleanExtra(Constants.isHostKey, false);

		// If the isHost bool was never passed in
		if (isHost == false
				&& getIntent().getBooleanExtra(Constants.isHostKey, true)) {
			Log.e(TAG, "There's an issue with the \"isHost\" boolean flag.");
			Toast.makeText(getApplicationContext(),
					"Fatal error: \"isHost\" boolean was not passed in",
					Toast.LENGTH_LONG).show();
			finish();
		}

		if (isHost) {
			Toast.makeText(getApplicationContext(), "you da host",
					Toast.LENGTH_SHORT).show();

			// create the session (no point returning it)
			createSession(Calendar.getInstance().getTime().getMinutes());

			String sessionFirebaseUrl = Constants.sessionsUrl + sessionName;
			Log.d(TAG, "sessionFirebase URL = " + sessionFirebaseUrl);
			sessionFirebase = new Firebase(sessionFirebaseUrl);

			// add your id to list of users
			/* good */sessionUsersFirebase = new Firebase(Constants.firebaseUrl
					+ sessionFirebase.getPath().toString()
					+ Constants.usersUrlSuffix);

			mySessionUserFirebase = sessionFirebase.push();
			mySessionId = mySessionUserFirebase.getName();
			String myUserInSessionUrl = Constants.sessionsUrl + sessionName
					+ Constants.usersUrlSuffix + mySessionId;
			mySessionUserFirebase = new Firebase(myUserInSessionUrl);

			mySessionUserFirebase.child("id").setValue(mySessionId);

			// make your id the "host id" field for the session
			sessionFirebase.child(Constants.KEY_HOST_ID).setValue(mySessionId);

		} else {
			Toast.makeText(getApplicationContext(), "you aint no host",
					Toast.LENGTH_SHORT).show();

		}

		// Once we have a Firebase session by string name,
		// we should add the host user to /users and put
		// his id as the host_id

		/*
		 * TODO: Need a way to close the activity if the firebase is somehow
		 * destroyed
		 */

		// Initialize the intent to start alarm service
		// Intent myIntent = new Intent(SessionActivity.this,
		// MyAlarmService.class);

		// Initialize the pendingIntent to start the Intent
		// pIntent = PendingIntent.getService(SessionActivity.this, 0,
		// myIntent,
		// 0);

		// Initialize the alarmManager
		// alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		inflateXML();
		// buildMediaPlayers();

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
		// Firebase listRef = new Firebase(Constants.firebaseUrl);
		// makeLongToast("connected to: " + Utils.firebaseUrl);

		// Create Firebase for list of users in current session
		Firebase sessionUsersFirebase = new Firebase(Constants.sessionsUrl
				+ sessionName + Constants.usersUrlSuffix);

		// Generate a reference to a new location with push()
		// mySessionIdFirebase = sessionUsersFirebase.push();

		// Get the name generated by push
		// mySessionId = mySessionIdFirebase.getName();

		/*
		 * List<String> list = new ArrayList<String>(); list.add(mySessionID);
		 */

		// mySessionIdFirebase.setValue(list);

		// Firebase x = mySessionIdFirebase.push();
		// Log.d(TAG, x.getName() + ", parent = " + x.getParent().getName());
		// x.setValue("hey");

		// Display my session id
		textViewMySessionId.setText(mySessionId);

	}

	@SuppressWarnings("deprecation")
	private void inflateXML() {
		textViewPlayTime = (TextView) findViewById(R.id.textViewPlayTime);
		textViewPlayTime.setText(new Date(timeToPlayAtInMillis).toGMTString());

		textViewMySessionId = (TextView) findViewById(R.id.textViewMySessionID);
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

	/**
	 * Sets the Action Bar for new Android versions.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void actionBarSetup(String title, String subtitle) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar ab = getActionBar();
			ab.setTitle(title);
			ab.setSubtitle(subtitle);
		}
	}

	public Firebase createSession(int songId) {
		// Reference to "sessions" Firebase
		Firebase sessions = new Firebase(Constants.sessionsUrl);

		Session session = new Session(sessionName, HomeActivity.textViewUserID
				.getText().toString(), songId);

		// Url of Firebase for "session" being created
		String url = Constants.sessionsUrl + session.getName();

		// Add the session to the Firebase
		sessions.child(session.getName()).setValue(session);

		Firebase child_v1 = sessions.child(session.getName());
		Log.e(TAG, "childv1 = " + child_v1.getName());
		Firebase child_v2 = sessions.child(session.getName());
		Log.e(TAG, "childv2 = " + child_v2.getName());

		Log.d(TAG, "in createSession, sessions Name = " + sessions.getName());
		Log.d(TAG, "in createSession, sessions.child.getNamesesh = "
				+ sessions.child(session.getName()).getName());

		// Return a reference to the Firebase of this new Session
		return sessions.push();
	}

	@Override
	protected void onPause() {
		// disconnect from session
		Toast.makeText(
				getApplicationContext(),
				"removing " + mySessionId + " from "
						+ sessionUsersFirebase,
				Toast.LENGTH_SHORT).show();
		sessionUsersFirebase.child(mySessionId).removeValue();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// connectToFirebase();
	}

}
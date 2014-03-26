package com.conchord.android.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.network.SafeAsyncTask;
import com.conchord.android.util.ConchordMediaPlayer;
import com.conchord.android.util.Constants;
import com.conchord.android.util.MyAlarmService;
import com.conchord.android.util.Session;
import com.conchord.android.util.SntpClient;
import com.conchord.android.util.Utils;
import com.conchord.android.util.Utils.MediaFiles;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class SessionActivity extends Activity {

	// TODO: Need to get connections to Firebases in onResume and need to
	// disconnect all in onPause

	public static final String TAG = "R2D2:  ";
	private TextView textViewMySessionId;
	private Button buttonPlay;

	private TextView textViewStartTime;

	// Controls screen sleep/wake
	PowerManager pm;
	PowerManager.WakeLock wl;

	// Stuff to play music
	public static ConchordMediaPlayer mPlayer;
	private static TextView textViewPlayTime;
	private PendingIntent pIntent;
	private AlarmManager alarmManager;
	private long timeToPlayAtInMillis = 0;
	private long ntpTime = 0;

	private long hostCalibrationSongTime = 0;
	private long hostCalibrationNtpTime = 0;

	/* Session information */
	private static String sessionName;
	private static Firebase sessionFirebase;
	private static Firebase sessionUsersFirebase;
	private ArrayList<DataSnapshot> sessionUsersDataSnapshots = new ArrayList<DataSnapshot>();
	private static Firebase sessionPlayTimeFirebase;

	// private static Firebase sessionCalibrateFirebase;
	// private static Firebase sessionCalibrateNtpTimeFirebase;
	// private static Firebase sessionCalibrateSongTimeFirebase;

	private static Firebase sessionClosedFirebase;

	/* My session id information */
	private String mySessionId;
	private Firebase mySessionUserFirebase;

	private boolean isHost = false;
	/*
	 * After each ntp query we check if the host is trying to set the play time
	 * for the session
	 */
	private boolean needToSetFirebasePlayTime = false;

	private boolean receivingRelativePlayTime = false;
	private boolean needToSetFirebaseCalibration = false;
	private boolean needToCalibrate = false;

	private TextView textViewNtpPlayTime, textViewSnapshotNtp,
			textViewRequestTime, textViewRoundtripTime, textViewSnapshotLocal,
			textViewTimeRemainingFromNtp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the session name and whether user is host or not
		sessionName = getIntent().getStringExtra(Constants.KEY_SESSION);
		isHost = getIntent().getBooleanExtra(Constants.KEY_IS_HOST, false);

		if (isHost) {
			setContentView(R.layout.layout_session_host);
		} else {
			setContentView(R.layout.layout_session);
		}

		makeSure_isHost_WasPassedIn();

		setupFirebase();

		// Initialize the intent to start alarm service
		Intent myIntent = new Intent(SessionActivity.this, MyAlarmService.class);

		// Initialize the pendingIntent to start the Intent
		pIntent = PendingIntent
				.getService(SessionActivity.this, 0, myIntent, 0);

		// Initialize the alarmManager
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		inflateXML();

		setupGUI();

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

		buildMediaPlayers();

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

		// REMOVE THIS @TODO

		textViewStartTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getNTPtime();
			}
		});

	}

	private void setupFirebase() {
		// If this is the host, we'll create the session on Firebase
		if (isHost) {
			createSession(Calendar.getInstance().getTime().getMinutes());
		} else {
			makeShortToast("you aint no host");
		}

		String sessionFirebaseUrl = Constants.sessionsUrl + sessionName;
		sessionFirebase = new Firebase(sessionFirebaseUrl);
		sessionFirebase.onDisconnect().removeValue();

		// access play time
		String sessionPlayTimeUrl = Constants.sessionsUrl + sessionName + "/"
				+ Constants.KEY_PLAY_TIME;
		sessionPlayTimeFirebase = new Firebase(sessionPlayTimeUrl);

		// add your id to list of users
		String sessionUsersFirebaseUrl = Constants.firebaseUrl
				+ sessionFirebase.getPath().toString()
				+ Constants.usersUrlSuffix;
		sessionUsersFirebase = new Firebase(sessionUsersFirebaseUrl);
		mySessionUserFirebase = sessionFirebase.push();
		mySessionId = mySessionUserFirebase.getName();
		String myUserInSessionUrl = Constants.sessionsUrl + sessionName
				+ Constants.usersUrlSuffix + mySessionId;
		mySessionUserFirebase = new Firebase(myUserInSessionUrl);

		mySessionUserFirebase.child(Constants.KEY_ID).setValue(mySessionId);

		// If this is the host, assign their id to the host_id for the session
		if (isHost) {
			sessionFirebase.child(Constants.KEY_HOST_ID).setValue(mySessionId);
		}

		// this Firebase is for syncing
		// String sessionCalibrateUrl = Constants.sessionsUrl + sessionName +
		// "/"
		// + "calibrate";
		// sessionCalibrateFirebase = new Firebase(sessionCalibrateUrl);
		// sessionCalibrateFirebase
		// .addValueEventListener(sessionCalibrationListener);

		// this flag says whether the session is open or closed
		String sessionClosedFirebaseUrl = Constants.sessionsUrl + sessionName
				+ "/" + Constants.KEY_SESSION_CLOSED;
		sessionClosedFirebase = new Firebase(sessionClosedFirebaseUrl);
		sessionClosedFirebase.setValue(false);

	}

	private static void setFirebasePlayTime(String playTime) {
		String sessionPlayTimeUrl = Constants.sessionsUrl + sessionName + "/"
				+ Constants.KEY_PLAY_TIME;
		sessionPlayTimeFirebase = new Firebase(sessionPlayTimeUrl);
		sessionPlayTimeFirebase.setValue(playTime);
	}

	private void inflateXML() {
		textViewMySessionId = (TextView) findViewById(R.id.textViewMySessionID);

		if (isHost) {
			buttonPlay = (Button) findViewById(R.id.buttonPlay);
		}

		textViewStartTime = (TextView) findViewById(R.id.textViewStartTimeInMillis);

		textViewNtpPlayTime = (TextView) findViewById(R.id.textViewNTPplayTime);
		textViewSnapshotNtp = (TextView) findViewById(R.id.textViewSnapshotNTPtime);
		textViewRequestTime = (TextView) findViewById(R.id.textViewRequesttime);
		textViewRoundtripTime = (TextView) findViewById(R.id.textViewRoundtripTime);
		textViewSnapshotLocal = (TextView) findViewById(R.id.textViewSnapshotLocalTime);
		textViewTimeRemainingFromNtp = (TextView) findViewById(R.id.textViewRemainingLocalTime);

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupGUI() {
		// Display my session id
		textViewMySessionId.setText(mySessionId);
		textViewMySessionId.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				needToSetFirebaseCalibration = true;
				getNTPtime();
			}
		});

		// Sets the Action Bar for new Android versions
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar ab = getActionBar();
			ab.setTitle(sessionName);
			ab.setSubtitle("onchord");
		} else {
			// Even set that little grey title bar up top
			((TextView) findViewById(android.R.id.title)).setText(sessionName);
		}

		if (isHost) {
			buttonPlay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// tell the getNTPTime method that we need to post to
					// Firebase
					needToSetFirebasePlayTime = true;
					getNTPtime();

					// set Alarm for 3 seconds after firebase play time to do
					// the
					// calibration thing

					sessionClosedFirebase.setValue(true);

					v.setEnabled(false);
				}
			});
		}
	}

	public Firebase createSession(int songId) {
		// Reference to "sessions" Firebase
		Firebase sessions = new Firebase(Constants.sessionsUrl);

		Session session = new Session(sessionName, songId);

		// Add the session to the Firebase
		sessions.child(session.getName()).setValue(session);

		// Return a reference to the Firebase of this new Session
		return sessions.push();
	}

	ValueEventListener sessionListener = new ValueEventListener() {

		@Override
		public void onDataChange(DataSnapshot arg0) {
			if (arg0.getValue() == null) {
				if (!isHost)
					makeLongToast("The host killed the jam session!");
				finish();
			}
		}

		@Override
		public void onCancelled() {
			// TODO Auto-generated method stub
		}
	};

	ValueEventListener sessionUsersListener = new ValueEventListener() {

		@Override
		public void onDataChange(DataSnapshot arg0) {
			if (arg0.getValue() == null) {
				Log.e(TAG,
						"There appear to be NO users in this session...where's the host?");
			} else {

				/*
				 * GET BACK TO WORK RIGHT HERE, TRYING TO FIGURE OUT IF I CAN
				 * GET USER IDs FROM THE SNAPSHOT AS PEOPLE ENTER/EXIT
				 */

				Log.d(TAG, TAG + "There are " + arg0.getChildrenCount()
						+ " users.");

				/*
				 * for (DataSnapshot x : arg0.getChildren()) { Log.d(TAG, TAG +
				 * "users child value = " + x.getValue().toString()); }
				 */
				// makeShortToast(arg0.getChildren() + " were just added.");
				// makeShortToast("There are " + arg0.getChildrenCount() +
				// " users.");
			}

		}

		@Override
		public void onCancelled() {
			// TODO Auto-generated method stub

		}
	};

	ValueEventListener sessionPlayTimeListener = new ValueEventListener() {

		@Override
		public void onDataChange(DataSnapshot arg0) {
			if (isHost || arg0.getValue() == null)
				return;

			// Log.e(TAG, TAG + "nanos received playtime = " +
			// System.nanoTime());
			// makeShortToast(arg0.getValue().toString());

			// make sure you're not the host
			// verify that it's a legit play time
			// get ready to play

			timeToPlayAtInMillis = Long.valueOf(arg0.getValue().toString());
			// makeShortToast(timeToPlayAtInMillis + "");
			// Log.e(TAG, "R2D2...received play time of " +
			// timeToPlayAtInMillis);

			// tell getNTPtime we are going to be receiving a local time
			receivingRelativePlayTime = true;
			getNTPtime();

		}

		@Override
		public void onCancelled() {
			// TODO Auto-generated method stub

		}
	};

	ValueEventListener sessionCalibrationListener = new ValueEventListener() {

		@Override
		public void onDataChange(DataSnapshot arg0) {
			if (isHost || arg0.getValue() == null)
				return;

			hostCalibrationNtpTime = Long.valueOf(arg0
					.child(Constants.KEY_NTP_TIME).getValue().toString());
			hostCalibrationSongTime = Long.valueOf(arg0
					.child(Constants.KEY_SONG_TIME).getValue().toString());

			needToCalibrate = true;
			getNTPtime();
		}

		@Override
		public void onCancelled() {
			// TODO Auto-generated method stub

		}

	};

	@Override
	protected void onResume() {
		super.onResume();

		wl.acquire();

		// Make sure you're the host.
		sessionFirebase.child(Constants.KEY_HOST_ID).addValueEventListener(
				new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot arg0) {

						if (arg0.getValue() != null) {

							String hostId = arg0.getValue().toString();

							if (hostId != null) {

								if (!hostId.equals(mySessionId) && isHost) {

									makeShortToast("Oooh, someone just beat you to that name! Try another one.");
									Log.d(TAG,
											TAG
													+ "sessionFirebase.child(Constants.KEY_HOST_ID) value changed");
									Log.d(TAG,
											TAG + "arg0.getName() = "
													+ arg0.getName());
									Log.d(TAG, TAG + "hostId = " + hostId
											+ ", mySessionId = " + mySessionId);
									isHost = false;
									finish();
								}
							}
						}
					}

					@Override
					public void onCancelled() {
						// TODO Auto-generated method stub

					}
				});

		sessionFirebase.addValueEventListener(sessionListener);
		sessionUsersFirebase.addValueEventListener(sessionUsersListener);
		sessionPlayTimeFirebase.addValueEventListener(sessionPlayTimeListener);

	}

	private void buildMediaPlayers() {

		// device should get assignment before building media players

		// if (android.os.Build.VERSION.SDK_INT < 9) {
		// mPlayer = new ConchordMediaPlayer(getApplicationContext(),
		// MediaFiles.call_me_acapella);
		// } else {
		// mPlayer = new ConchordMediaPlayer(getApplicationContext(),
		// MediaFiles.call_me_instrumental);
		// }
		// TODO: remove

		switch (android.os.Build.VERSION.SDK_INT) {
		case 8:	// 2.2.3
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.sail_bass_and_drums);
			break;
		case 10:	// 2.3.4
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.sail_synth);
			break;
		case 16:	// 4.1.1
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.sail_synth);
			break;
		case 19:	// 4.4
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.sail_vocals_piano_electric);
			break;
		default:
			mPlayer = new ConchordMediaPlayer(getApplicationContext(),
					MediaFiles.sail_bass_and_drums);
			break;
		}

		// mPlayer = new ConchordMediaPlayer(getApplicationContext(),
		// MediaFiles.facebook_pop);

		/*
		 * mPlayer = new ConchordMediaPlayer(getApplicationContext(),
		 * MediaFiles.call_me_acapella);
		 */

	}

	private void setAlarm(long time) {
		textViewStartTime.setText("start @ " + time);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
	}

	/*
	 * private void makeSureGPSisEnabledOnDevice() { LocationManager locMngr =
	 * (LocationManager) getSystemService(LOCATION_SERVICE); boolean enabled =
	 * locMngr.isProviderEnabled(locMngr.GPS_PROVIDER); if (!enabled) { Intent
	 * intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	 * startActivity(intent); } }
	 */

	private void getNTPtime() {
		// new GetNTPTimeAsyncTask().execute();
		new NtpAsyncTask().execute();
	}

	// TODO Make multiple AsyncTasks instead of all these flags. Perhaps this
	// will help with synchronization...

	class NtpAsyncTask extends AsyncTask<String, Void, Long> {
		SntpClient client;

		@Override
		protected Long doInBackground(String... arg0) {

			// TODO Auto-generated method stub
			client = new SntpClient();
			int i = 0;

			while (!client.requestTime(Utils.someCaliNtpServers[0],
					Constants.ROUNDTRIP_TIMEOUT)
					&& i < Constants.NUM_NTP_ATTEMPTS) {
				Log.e(TAG, "R2D2...client.requestTime failed");
				client = new SntpClient();
				i++;
			}

			Long time = client.getNtpTime()/*
											 * + SystemClock.elapsedRealtime() -
											 * client.getNtpTimeReference()
											 */;
			// Log.e(TAG, "R2D2...ntp time is " + time);
			// Log.e(TAG, "R2D2 : time = " + System.currentTimeMillis());

			// Log.e("", "R2D2: " + "ntpTime = " + ntpTime);
			// Log.e("", "R2D2: " + "cttmillis = " +
			// SystemClock.currentThreadTimeMillis());
			// Log.e("", "R2D2: " + " elapsed real time " +
			// SystemClock.elapsedRealtime());
			// if (mPlayer.isPlaying()) Log.e("R2D2: ", "R2D2: " + "songTime = "
			// + mPlayer.getCurrentPosition());

			return time;

			/*
			 * Log.e(TAG, "R2D2, send time = " + System.currentTimeMillis());
			 * Log.e(TAG, "R2D2; current thread time: " +
			 * SystemClock.currentThreadTimeMillis());
			 */
			/*
			 * if (client.requestTime(Utils.someCaliNtpServers[0], 100)) { //
			 * Log.e(TAG, "R2D2, receive time = " + System.currentTimeMillis());
			 * Long time = client.getNtpTime() + SystemClock.elapsedRealtime() -
			 * client.getNtpTimeReference(); Log.e(TAG, "R2D2...ntp time is " +
			 * time); // Log.e(TAG, "R2D2 : time = " +
			 * System.currentTimeMillis());
			 * 
			 * // Log.e("", "R2D2: " + "ntpTime = " + ntpTime); // Log.e("",
			 * "R2D2: " + "cttmillis = " + //
			 * SystemClock.currentThreadTimeMillis()); // Log.e("", "R2D2: " +
			 * " elapsed real time " + // SystemClock.elapsedRealtime()); // if
			 * (mPlayer.isPlaying()) Log.e("R2D2: ", "R2D2: " + "songTime = " //
			 * + mPlayer.getCurrentPosition());
			 * 
			 * return time; } else { Log.e(TAG,
			 * "R2D2...client.requestTime failed"); return null; }
			 */
		}

		@Override
		protected void onPostExecute(Long x) {
			super.onPostExecute(x);
			ntpTime = x;

			// Log.e(TAG, TAG + "ntpTime = " + ntpTime);
			// if (mPlayer.isPlaying()) Log.e(TAG, TAG + "songTime = " +
			// mPlayer.getCurrentPosition());
			// long localTime = System.currentTimeMillis();
			// long pos = mPlayer.getCurrentPosition();

			if (isHost && needToSetFirebasePlayTime) {
				long startTime = ntpTime + Constants.START_TIME_DELAY;

				// edit Firebase server
				setFirebasePlayTime(String.valueOf(startTime));

				/*
				 * setAlarm(startTime);
				 */

				setAlarm(client.localESTIMATEDntpTime
						+ Constants.START_TIME_DELAY);

				// reset this flag
				needToSetFirebasePlayTime = false;

				textViewNtpPlayTime.setText("NTP play time: "
						+ (startTime % 1000000));
				textViewSnapshotNtp.setText("Snapshot NTP: " + (x % 1000000));
				textViewRequestTime.setText("Request time: "
						+ (client.myRequestTime % 1000000));
				textViewRoundtripTime.setText("Roundtrip time: "
						+ (client.myRoundtripTime % 1000000));
				textViewSnapshotLocal.setText("Snapshot Local (GUESS): "
						+ (client.localESTIMATEDntpTime % 1000000));
				textViewTimeRemainingFromNtp.setText("Remaining local time: "
						+ Constants.START_TIME_DELAY);

			} else if (!isHost && receivingRelativePlayTime) {

				long diff = timeToPlayAtInMillis - ntpTime;
				// makeLongToast(diff + " millis b/c isHost == " + isHost);

				// Log.e(TAG, "R2D2...millis btw ntptime and play time = " +
				// diff);

				setAlarm(client.localESTIMATEDntpTime + diff);

				Log.e(TAG, "R2D2...setting alarm for "
						+ (client.localESTIMATEDntpTime + diff));

				// reset this flag
				receivingRelativePlayTime = false;

				textViewNtpPlayTime.setText("NTP play time: "
						+ (timeToPlayAtInMillis % 1000000));
				textViewSnapshotNtp.setText("Snapshot NTP: " + (x % 1000000));
				textViewRequestTime.setText("Request time: "
						+ (client.myRequestTime % 1000000));
				textViewRoundtripTime.setText("Roundtrip time: "
						+ (client.myRoundtripTime % 1000000));
				textViewSnapshotLocal.setText("Snapshot Local (GUESS): "
						+ (client.localESTIMATEDntpTime % 1000000));
				textViewTimeRemainingFromNtp.setText("Remaining local time: "
						+ diff);

			}/*
			 * else if (isHost && needToSetFirebaseCalibration) { if
			 * (!mPlayer.isPlaying()) {
			 * makeShortToast("the player isn't playing yet"); }
			 * 
			 * // need to set these values at once Map<String, String> toSet =
			 * new HashMap<String, String>(); toSet.put(Constants.KEY_NTP_TIME,
			 * "" + ntpTime); toSet.put(Constants.KEY_SONG_TIME, "" + pos); //
			 * sessionCalibrateFirebase.setValue(toSet);
			 * 
			 * // reset this flag needToSetFirebaseCalibration = false; }
			 */else if (!isHost && needToCalibrate) {

				// int currentSongTime = mPlayer.getCurrentPosition();

				// subtract old ntp time from new one.
				// long ntpDiff = ntpTime - hostCalibrationNtpTime;

				// add that to the old song time
				// long newSongTime = hostCalibrationSongTime + ntpDiff;
				int offsetForProcessingTime = 100;

				// should be there so skip to it
				mPlayer.seekTo((int) (hostCalibrationSongTime + ntpTime
						- hostCalibrationNtpTime + offsetForProcessingTime));

				makeShortToast("calibrated. isHost = " + isHost);

				needToCalibrate = false;
			}

		}
	}

	class GetNTPTimeAsyncTask extends SafeAsyncTask<Long> {

		@Override
		public Long call() throws Exception {
			// TODO Auto-generated method stub
			SntpClient client = new SntpClient();
			if (client.requestTime(Utils.someCaliNtpServers[0], 10000)) {

				Long time = client.getNtpTime() + SystemClock.elapsedRealtime()
						- client.getNtpTimeReference();
				return time;
			} else {
				makeLongToast("NTP error");
				return null;
			}
		}

		@Override
		protected void onException(Exception e) throws RuntimeException {
			super.onException(e);
			Log.e(TAG, e.getMessage());
		}

		@Override
		protected void onSuccess(Long x) throws Exception {
			ntpTime = x;
			// long localTime = System.currentTimeMillis();
			long pos = mPlayer.getCurrentPosition();

			if (isHost && needToSetFirebasePlayTime) {
				long startTime = ntpTime + Constants.START_TIME_DELAY;

				// edit Firebase server
				setFirebasePlayTime(String.valueOf(startTime));

				setAlarm(startTime);

				// reset this flag
				needToSetFirebasePlayTime = false;

			} else if (!isHost && receivingRelativePlayTime) {

				long diff = timeToPlayAtInMillis - ntpTime;
				makeLongToast(diff + " millis b/c isHost == " + isHost);

				setAlarm(System.currentTimeMillis() + diff);

				// reset this flag
				receivingRelativePlayTime = false;

			} else if (isHost && needToSetFirebaseCalibration) {
				if (!mPlayer.isPlaying()) {
					makeShortToast("the player isn't playing yet");
				}

				// need to set these values at once
				Map<String, String> toSet = new HashMap<String, String>();
				toSet.put(Constants.KEY_NTP_TIME, "" + ntpTime);
				toSet.put(Constants.KEY_SONG_TIME, "" + pos);
				// sessionCalibrateFirebase.setValue(toSet);

				// reset this flag
				needToSetFirebaseCalibration = false;
			} else if (!isHost && needToCalibrate) {

				// int currentSongTime = mPlayer.getCurrentPosition();

				// subtract old ntp time from new one.
				// long ntpDiff = ntpTime - hostCalibrationNtpTime;

				// add that to the old song time
				// long newSongTime = hostCalibrationSongTime + ntpDiff;
				int offsetForProcessingTime = 100;

				// should be there so skip to it
				mPlayer.seekTo((int) (hostCalibrationSongTime + ntpTime
						- hostCalibrationNtpTime + offsetForProcessingTime));

				makeShortToast("calibrated. isHost = " + isHost);

				needToCalibrate = false;
			}
		}

	}

	private void makeSure_isHost_WasPassedIn() {
		// If the isHost bool was never passed in, kill the activity
		if (isHost == false
				&& getIntent().getBooleanExtra(Constants.KEY_IS_HOST, true)) {
			Log.e(TAG, TAG
					+ "There's an issue with the \"isHost\" boolean flag.");
			Toast.makeText(getApplicationContext(),
					"Fatal error: \"isHost\" boolean was not passed in",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onPause() {
		// disconnect from session
		if (isHost) {
			// makeShortToast("destroying jam session: " +
			// sessionFirebase.getName());
			sessionFirebase.getParent().child(sessionName).removeValue();
		} else {
			// makeShortToast("exiting jam session: " +
			// sessionFirebase.getName());
			sessionUsersFirebase.child(mySessionId).removeValue();
		}

		// remove listeners
		sessionFirebase.removeEventListener(sessionListener);
		sessionUsersFirebase.removeEventListener(sessionUsersListener);
		sessionPlayTimeFirebase.removeEventListener(sessionPlayTimeListener);

		// TODO make sure all eventlisteners are added and removed in pause and
		// resume

		mPlayer.stop();

		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		wl.release();
		finish();
	}

	public void makeLongToast(String text) {
		Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
	}

	public void makeShortToast(String text) {
		Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
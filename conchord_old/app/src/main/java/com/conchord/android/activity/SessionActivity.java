package com.conchord.android.activity;

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

import java.util.ArrayList;
import java.util.Calendar;

public class SessionActivity extends Activity {

	// TODO: Need to get connections to Firebases in onResume and need to
	// disconnect all in onPause

	private static final String TAG = SessionActivity.class.getSimpleName();
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


	/* Session information */
	private static String sessionName;
	private static Firebase sessionFirebase;
	private static Firebase sessionUsersFirebase;
	private ArrayList<DataSnapshot> sessionUsersDataSnapshots = new ArrayList<DataSnapshot>();
	private static Firebase sessionPlayTimeFirebase;
	private static Firebase sessionClosedFirebase;

	/* My session id information */
	private String mySessionId;/**/
	private Firebase mySessionUserFirebase;

	private boolean isHost = false;
	/**
	 * After each ntp query we check if the host is trying to set the play time
	 * for the session
	 */
	private boolean needToSetFirebasePlayTime = false;

	private boolean receivingRelativePlayTime = false;

	private TextView textViewNtpPlayTime, textViewSnapshotNtp,
			textViewRequestTime, textViewRoundtripTime, textViewSnapshotLocal,
			textViewTimeRemainingFromNtp;

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
				 * for (DataSnapshot x : arg0.getChildren()) { Log.d(TAG,
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

            // Log.e(TAG, "nanos received playtime = " +
            // System.nanoTime());
            // makeShortToast(arg0.getValue().toString());

            // make sure you're not the host
            // verify that it's a legit play time
            // get ready to play

            timeToPlayAtInMillis = Long.valueOf(arg0.getValue().toString());
            // makeShortToast(timeToPlayAtInMillis + "");
            // Log.e(TAG, "received play time of " +
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
    ValueEventListener sessionHostIdListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot arg0) {
            if (arg0.getValue() != null) {
                String hostId = arg0.getValue().toString();

                if (hostId != null) {
                    if (!hostId.equals(mySessionId) && isHost) {
                        makeShortToast("Oooh, someone just beat you to that name! Try another one.");
                        Log.d(TAG,"sessionFirebase.child(Constants.KEY_HOST_ID) value changed");
                        Log.d(TAG, "arg0.getName() = " + arg0.getName());
                        Log.d(TAG, "hostId = " + hostId + ", mySessionId = " + mySessionId);
                        isHost = false;
                        finish();
                    }
                }
            }
        }

        @Override
        public void onCancelled() {

        }
    };


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

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

	}

    /**
     * This function creates a Firebase for
     */
	private void setupFirebase() {
		// If this is the host, we'll create the session on Firebase
		if (isHost) {
            Log.d(TAG, "Setting up Firebase as host.");
			createSession(Calendar.getInstance().getTime().getMinutes());
		} else {
            Log.d(TAG, "setupFirebase called as guest.");
            makeShortToast("Welcome!");
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

		// Sets the Action Bar for new Android versions
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar ab = getActionBar();
			ab.setTitle(sessionName);
			ab.setSubtitle("conchord");
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
        Log.d(TAG, "Creating Session with songId " + songId);

		// Reference to "sessions" Firebase
		Firebase sessions = new Firebase(Constants.sessionsUrl);

		Session session = new Session(sessionName, songId);

		// Add the session to the Firebase
		sessions.child(session.getName()).setValue(session);

		// Return a reference to the Firebase of this new Session
		return sessions.push();
	}

	@Override
	protected void onResume() {
		super.onResume();

		wl.acquire();

		// Make sure you're the host.
		sessionFirebase.child(Constants.KEY_HOST_ID).addValueEventListener(sessionHostIdListener);

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

		/*switch (android.os.Build.VERSION.SDK_INT) {
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
		}*/
        mPlayer = new ConchordMediaPlayer(getApplicationContext(),
                MediaFiles.facebook_pop);
	}

	private void setAlarm(long time) {
		textViewStartTime.setText("start @ " + time);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
	}

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

            String TAG = NtpAsyncTask.class.getSimpleName() + ".doInBackground()";

			// Create SntpClient
			client = new SntpClient();
			int i = 0;

			while (!client.requestTime(Utils.someCaliNtpServers[0],
					Constants.ROUNDTRIP_TIMEOUT)
					&& i < Constants.NUM_NTP_ATTEMPTS) {
				Log.e(TAG, "client.requestTime failed with " + client.myRoundtripTime);
				client = new SntpClient();
				i++;
			}

			Long time = client.getNtpTime();
			Log.d(TAG, "ntp time is " + time);
			Log.d(TAG, "time = " + System.currentTimeMillis());

			Log.d(TAG, "ntpTime = " + ntpTime);
			Log.d(TAG, "currentthreadtimemillis = " + SystemClock.currentThreadTimeMillis());
			Log.d(TAG, "elapsed real time " + SystemClock.elapsedRealtime());
			if (mPlayer.isPlaying()) Log.d(TAG, "songTime = " + mPlayer.getCurrentPosition());

			return time;
		}

		@Override
		protected void onPostExecute(Long x) {
			super.onPostExecute(x);

            String TAG = NtpAsyncTask.class.getSimpleName() + ".onPostExecute()";

			ntpTime = x;

			Log.d(TAG, "ntpTime = " + ntpTime);
			// if (mPlayer.isPlaying()) Log.e(TAG, "songTime = " +
			// mPlayer.getCurrentPosition());
			// long localTime = System.currentTimeMillis();
			// long pos = mPlayer.getCurrentPosition();

			if (isHost && needToSetFirebasePlayTime) {
				long startTime = ntpTime + Constants.START_TIME_DELAY;

				// edit Firebase server
				setFirebasePlayTime(String.valueOf(startTime));

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

				Log.d(TAG, "millis btw ntptime and play time = " + diff);

				setAlarm(client.localESTIMATEDntpTime + diff);

				Log.d(TAG, "setting alarm for " + (client.localESTIMATEDntpTime + diff));

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
			}
		}
	}

	private void makeSure_isHost_WasPassedIn() {
		// If the isHost bool was never passed in, kill the activity
		if (isHost == false
				&& getIntent().getBooleanExtra(Constants.KEY_IS_HOST, true)) {
			Log.e(TAG, "There's an issue with the \"isHost\" boolean flag.");
			Toast.makeText(getApplicationContext(),
					"Fatal error: \"isHost\" boolean was not passed in",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onPause() {
        // TODO notifications on data removed won't happen if I remove these first
        removeValueEventListeners();
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

    private void removeValueEventListeners() {
        sessionFirebase.removeEventListener(sessionListener);
        sessionUsersFirebase.removeEventListener(sessionUsersListener);
        sessionPlayTimeFirebase.removeEventListener(sessionPlayTimeListener);
        sessionFirebase.child(Constants.KEY_HOST_ID).removeEventListener(sessionHostIdListener);
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
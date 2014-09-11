package com.conchord.android.activity;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.conchord.android.R;
import com.conchord.android.util.ConchordMediaPlayer;
import com.conchord.android.util.Constants;
import com.conchord.android.util.MyAlarmService;
import com.conchord.android.util.Session;
import com.conchord.android.util.SntpClient;
import com.conchord.android.util.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;

/**
 * Created by NATI on 7/15/2014.
 */
public class HostSessionActivity extends Activity {

      //////////////////////////////////////////////
     ///             CLASS VARIABLES            ///
    //////////////////////////////////////////////

    private static final String TAG = HostSessionActivity.class.getSimpleName();

    /* Session information */
    private static String sessionName;
    private static Firebase sessionFirebase;
    private static Firebase sessionUsersFirebase;
    private static Firebase sessionPlayTimeFirebase;
    private String mySessionId;
    private Firebase mySessionUserFirebase;
    private static Firebase sessionClosedFirebase;

    /* GUI */
    private TextView textViewMySessionId;
    private Button buttonPlay;
    private TextView textViewStartTime, textViewNtpPlayTime, textViewSnapshotNtp, textViewRequestTime,
            textViewRoundtripTime, textViewSnapshotLocal, textViewTimeRemainingFromNtp;


    /* Stuff to play music */
    public static ConchordMediaPlayer mPlayer;
    private PendingIntent pIntent;
    private AlarmManager alarmManager;
    private long ntpTime = 0;

    private boolean needToSetFirebasePlayTime = false;

    // Controls screen sleep/wake
    PowerManager pm;
    PowerManager.WakeLock wl;

    ValueEventListener sessionListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot arg0) {
            if (arg0.getValue() == null) {
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
                // Utils.makeShortToast(arg0.getChildren() + " were just added.");
                // Utils.makeShortToast("There are " + arg0.getChildrenCount() +
                // " users.");
            }

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
                    if (!hostId.equals(mySessionId)) {
                        Utils.makeShortToast(getApplicationContext(), "Oooh, someone just beat you to that name! Try another one.");
                        Log.d(TAG,"sessionFirebase.child(Constants.KEY_HOST_ID) value changed");
                        Log.d(TAG, "arg0.getName() = " + arg0.getName());
                        Log.d(TAG, "hostId = " + hostId + ", mySessionId = " + mySessionId);
                        finish();
                    }
                }
            }
        }

        @Override
        public void onCancelled() {

        }
    };

      //////////////////////////////////////////////
     ///                  CODE                  ///
    //////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        // Get the session name
        sessionName = getIntent().getStringExtra(Constants.KEY_SESSION);

        setContentView(R.layout.layout_session_host);

        setupFirebase();

        // Initialize the intent to start alarm service
        Intent myIntent = new Intent(HostSessionActivity.this, MyAlarmService.class);

        // Initialize the pendingIntent to start the Intent
        pIntent = PendingIntent
                .getService(HostSessionActivity.this, 0, myIntent, 0);

        // Initialize the alarmManager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        inflateXML();
        setupGUI();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

        buildMediaPlayers();

    }

    private void setupFirebase() {
        // If this is the host, we'll create the session on Firebase
        Log.d(TAG, "Setting up Firebase as host.");
        createSession(Calendar.getInstance().getTime().getMinutes());


        String sessionFirebaseUrl = Constants.sessionsUrl + sessionName;
        sessionFirebase = new Firebase(sessionFirebaseUrl);
        sessionFirebase.onDisconnect().removeValue();

        // play time
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
        sessionFirebase.child(Constants.KEY_HOST_ID).setValue(mySessionId);

        // this flag says whether the session is open or closed
        String sessionClosedFirebaseUrl = Constants.sessionsUrl + sessionName
                + "/" + Constants.KEY_SESSION_CLOSED;
        sessionClosedFirebase = new Firebase(sessionClosedFirebaseUrl);
        sessionClosedFirebase.setValue(false);

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
                        Utils.MediaFiles.sail_bass_and_drums);
                break;
            case 10:	// 2.3.4
                mPlayer = new ConchordMediaPlayer(getApplicationContext(),
                        Utils.MediaFiles.sail_synth);
                break;
            case 16:	// 4.1.1
                mPlayer = new ConchordMediaPlayer(getApplicationContext(),
                        Utils.MediaFiles.sail_synth);
                break;
            case 19:	// 4.4
                mPlayer = new ConchordMediaPlayer(getApplicationContext(),
                        Utils.MediaFiles.sail_vocals_piano_electric);
                break;
            default:
                mPlayer = new ConchordMediaPlayer(getApplicationContext(),
                        Utils.MediaFiles.sail_bass_and_drums);
                break;
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

    private void inflateXML() {
        textViewMySessionId = (TextView) findViewById(R.id.textViewMySessionID);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        textViewStartTime = (TextView) findViewById(R.id.textViewStartTimeInMillis);
        textViewNtpPlayTime = (TextView) findViewById(R.id.textViewNTPplayTime);
        textViewSnapshotNtp = (TextView) findViewById(R.id.textViewSnapshotNTPtime);
        textViewRequestTime = (TextView) findViewById(R.id.textViewRequesttime);
        textViewRoundtripTime = (TextView) findViewById(R.id.textViewRoundtripTime);
        textViewSnapshotLocal = (TextView) findViewById(R.id.textViewSnapshotLocalTime);
        textViewTimeRemainingFromNtp = (TextView) findViewById(R.id.textViewRemainingLocalTime);

    }

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


        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tell the getNTPTime method that we need to post to
                // Firebase
                needToSetFirebasePlayTime = true;
                getNTPtime();

                // set Alarm for 3 seconds after firebase play time to do
                // the calibration thing

                sessionClosedFirebase.setValue(true);

                v.setEnabled(false);
            }
        });
    }

    private void getNTPtime() {
        // new GetNTPTimeAsyncTask().execute();
        new NtpAsyncTask().execute();
    }

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

            if (needToSetFirebasePlayTime) {
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

            }
        }
    }

    private static void setFirebasePlayTime(String playTime) {
        String sessionPlayTimeUrl = Constants.sessionsUrl + sessionName + "/"
                + Constants.KEY_PLAY_TIME;
        sessionPlayTimeFirebase = new Firebase(sessionPlayTimeUrl);
        sessionPlayTimeFirebase.setValue(playTime);
    }

    private void setAlarm(long time) {
        textViewStartTime.setText("start @ " + time);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
    }

    private void removeValueEventListeners() {
        sessionFirebase.removeEventListener(sessionListener);
        sessionUsersFirebase.removeEventListener(sessionUsersListener);
        sessionFirebase.child(Constants.KEY_HOST_ID).removeEventListener(sessionHostIdListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        wl.acquire();

        sessionFirebase.addValueEventListener(sessionListener);
        sessionUsersFirebase.addValueEventListener(sessionUsersListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        // TODO notifications on data removed won't happen if I remove these first
        removeValueEventListeners();
        sessionFirebase.getParent().child(sessionName).removeValue();
        mPlayer.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
        wl.release();
        finish();
    }

}

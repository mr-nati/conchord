package com.titan22.conchord.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.titan22.conchord.R;
import com.titan22.conchord.util.Constants;
import com.titan22.conchord.util.SntpClient;
import com.titan22.conchord.util.Utils;

public class HomeActivity extends Activity {

    public static String TAG = HomeActivity.class.getSimpleName();

    private boolean mNtpSynced = false;

    private EditText mEditTextSessionToJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // set EditText enter cmd
        mEditTextSessionToJoin = ((EditText) findViewById(R.id.editText_join));
        mEditTextSessionToJoin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // TODO make this actually join the session
                    Toast.makeText(getApplicationContext(),
                            "Now click join.", Toast.LENGTH_SHORT).show();

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditTextSessionToJoin.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    /** Keeps track of the number of session id collisions we've randomly hit.  */
    int numSessionIdCollisions = 0;

    public void buttonClicked(final View v) {
        if (v.getId() == R.id.button_create) {

            // TODO verify ntp sync first
            new VerifyNetworkConnection().execute();

            // get random string
            final String sessionId = Utils.getRandomAlphaNumbericString(3);
            Log.d(TAG, "Got session ID: " + sessionId);

            // check if it's unique
            final Firebase newSessionFirebase = new Firebase(Constants.sessionsUrl + sessionId);
            newSessionFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        // set session to OPEN
                        Firebase sessionClosedFirebase = new Firebase(Constants.sessionsUrl + sessionId + "/" + Constants.KEY_SESSION_CLOSED);
                        sessionClosedFirebase.setValue(false);
                        newSessionFirebase.push();

                        numSessionIdCollisions = 0;
                   } else {
                        Log.d(TAG, "Already taken!");
                        if (numSessionIdCollisions > 5) {
                            Toast.makeText(getApplicationContext(),
                                    "Error connecting to server", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        numSessionIdCollisions++;
                        buttonClicked(v);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) { }
            });

        } else if (v.getId() == R.id.button_join) {

            // TODO verify NTP sync

            final String submittedId = mEditTextSessionToJoin.getText().toString();
            Log.d(TAG, String.format("Found id: \"%s\"", submittedId));

            // sanity check
            if (submittedId.length() == 0) {
                Log.e(TAG, "Entered empty String for session id.");
                Toast.makeText(getApplicationContext(), "Get the id from the host",
                        Toast.LENGTH_SHORT).show();
            }

            // verify it exists
            final Firebase potentialFirebase = new Firebase(Constants.sessionsUrl + submittedId);
            potentialFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        Log.e(TAG, "Session " + submittedId + " doesn't exist.");
                        Toast.makeText(getApplicationContext(),
                                "Couldn't find session \"" + submittedId + "\". Check with the host again.",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        // TODO verify it's accepting members

                        if (!dataSnapshot.child(Constants.KEY_SESSION_CLOSED)
                                .getValue().equals(false)) {

                        }


                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) { }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class VerifyNetworkConnection extends AsyncTask<String, Void, Long> {

        SntpClient client = new SntpClient();
        int i = 0;

        @Override
        protected Long doInBackground(String... params) {

            while (!client.requestTime(Utils.someCaliNtpServers[0], Constants.ROUNDTRIP_TIMEOUT)) {
                Log.e(TAG, "client.requestTime failed with " + client.myRoundtripTime);
                client = new SntpClient();
                i++;
                if (++i >= Constants.NUM_NTP_ATTEMPTS) { return null; }
            }

            Long time = client.getNtpTime();
            Log.d(TAG, "ntp time is " + time);
            Log.d(TAG, "time = " + System.currentTimeMillis());

            return time;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            if (aLong == null) {    // network too slow
                Toast.makeText(getApplicationContext(), "Your connection is too slow. Sorry :(", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "CONNECTION TOO SLOW!");
            } else {
                // we're good
            }
        }
    }

}

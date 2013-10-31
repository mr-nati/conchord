package com.conchord.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.util.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class HomeActivity extends Activity {

	private static final String TAG = HomeActivity.class.getSimpleName();
	private SharedPreferences prefs;

	private Button buttonCreateSession;
	private EditText editTextSessionName;

	private Button buttonJoinSession;
	private EditText editTextJoinSessionName;

	private Button buttonCreateUserID;
	private EditText editTextUserID;
	public static TextView textViewUserID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_home);

		this.prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		setupGUI();

		// Check if we have a user ID for the device **Isn't unique yet**
		if (!userIdExists()) {
			setUserID(android.os.Build.MODEL);
		}
	}

	private void setupGUI() {
		buttonCreateSession = (Button) findViewById(R.id.buttonCreateSession);
		editTextSessionName = (EditText) findViewById(R.id.editTextSessionName);

		buttonCreateSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String sessionName = editTextSessionName.getText()
						.toString();

				// Make sure user has an ID.
		//		if (!textViewUserID.getText().equals("no id")) {

					// Make sure session to create has valid length
					if (sessionName.length() > 0) {

						// Check to see if the session is unique.
						final Firebase firebase = new Firebase(
								Constants.sessionsUrl + sessionName);

						firebase.addValueEventListener(new ValueEventListener() {
							@Override
							public void onDataChange(DataSnapshot arg0) {
								Object value = arg0.getValue();

								if (value == null) {
									editTextSessionName.setText("");

									Intent intent = new Intent(
											getApplicationContext(),
											SessionActivity.class);
									intent.putExtra(Constants.KEY_SESSION,
											sessionName);
									intent.putExtra(Constants.isHostKey, true);
									startActivity(intent);
									// Because we're starting the activity, stop
									// listening.
									firebase.removeEventListener(this);
								} else {
									Toast.makeText(getBaseContext(),
											"session is not unique",
											Toast.LENGTH_SHORT).show();
									firebase.removeEventListener(this);
								}
							}

							@Override
							public void onCancelled() {

							}
						});
					} else {
						// toast saying no length
						Toast.makeText(getBaseContext(),
								"Give a valid length session name",
								Toast.LENGTH_SHORT).show();
					}
					
	//				} else {
	//				// toast saying no id
	//				Toast.makeText(getBaseContext(), "Set your id first",
	//						Toast.LENGTH_SHORT).show();
	//			}
			}
		});

		textViewUserID = (TextView) findViewById(R.id.textViewUserId);
		editTextUserID = (EditText) findViewById(R.id.editTextCreateUserId);
		buttonCreateUserID = (Button) findViewById(R.id.buttonCreateUserID);
		buttonCreateUserID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String userId = editTextUserID.getText().toString();

				if (userId.length() > 0) {
					textViewUserID.setText(userId);
				}

			}
		});

		editTextJoinSessionName = (EditText) findViewById(R.id.editTextJoinSessionName);
		buttonJoinSession = (Button) findViewById(R.id.buttonJoinSession);
		buttonJoinSession.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "click");

				final String sessionName = editTextJoinSessionName.getText()
						.toString();

				// Make sure session to create has valid length
				if (sessionName.length() > 0) {

					// Create the Firebase
					final Firebase firebase = new Firebase(
							Constants.sessionsUrl + sessionName);

					firebase.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot arg0) {
							Object value = arg0.getValue();

							if (value != null) {
								editTextJoinSessionName.setText("");

								// Join jam session
								Intent intent = new Intent(
										getApplicationContext(),
										SessionActivity.class);
								intent.putExtra(Constants.KEY_SESSION,
										sessionName);
								intent.putExtra(Constants.isHostKey, false);
								startActivity(intent);
								firebase.removeEventListener(this);
							} else {
								Toast.makeText(getApplicationContext(),
										"Can't find this session",
										Toast.LENGTH_SHORT).show();
								firebase.removeEventListener(this);
							}
						}

						@Override
						public void onCancelled() {

						}
					});
				} else {
					Toast.makeText(getApplicationContext(),
							"Enter something first!",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	private void setUserID(String userId) {
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString(Constants.KEY_HOST_ID, userId);
		prefsEditor.commit();
	}

	private boolean userIdExists() {
		return (prefs.getString(Constants.KEY_HOST_ID, null) == null);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}

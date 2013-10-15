package com.conchord.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.util.Constants;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class HomeActivity extends Activity {

	private Button buttonCreateSession;
	private EditText editTextSessionName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_home);

		setupGUI();
	}

	private void setupGUI() {
		buttonCreateSession = (Button) findViewById(R.id.buttonCreateSession);
		editTextSessionName = (EditText) findViewById(R.id.editTextSessionName);

		buttonCreateSession.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String sessionName = editTextSessionName.getText().toString();

				if (sessionName.length() > 0) {
					// Check to see if the session is unique.
					Firebase firebase = new Firebase(Constants.sessionsUrl + sessionName);

					firebase.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot arg0) {
							Object value = arg0.getValue();

							if (value == null) {
								editTextSessionName.setText("");
								
								Intent intent = new Intent(getApplicationContext(),
										SessionActivity.class);
								intent.putExtra(Constants.sessionKey, sessionName);
								startActivity(intent);
							} else {
								Toast.makeText(getBaseContext(),
										"session is not unique", Toast.LENGTH_SHORT)
										.show();
							}
						}

						@Override
						public void onCancelled() {

						}
					});

				}

			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}

package com.conchord.android;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class OtherActivity extends Activity {

	private static final String FIREBASE_URL = "https://cfc-firebase.firebaseio.com/";

	EditText editText;
	Button submitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other);

		editText = (EditText) findViewById(R.id.the_edittext);
		submitButton = (Button) findViewById(R.id.submit_button);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Log.d("onClick", "editText says: \"" + editText.getText() + "\"");
				
				if (editText.getText().length() > 0) {
					// Write data to Firebase
					Firebase myFirebase = new Firebase("https://conchord-app.firebaseio.com/");
					myFirebase.setValue(editText.getText());
					Map<String, String> map = new HashMap<String, String>();
					map.put("myName", "myValue");
					myFirebase.setValue(map);
					
					Log.d("OtherActivity", "setValue to Firebase");
				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter text", Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	@Override
	protected void onResume() {
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
		//myFirebase = null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

}

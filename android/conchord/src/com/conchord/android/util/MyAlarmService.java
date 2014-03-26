package com.conchord.android.util;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.conchord.android.R;
import com.conchord.android.activity.SessionActivity;

public class MyAlarmService extends Service {
	@Override
	public void onCreate() {
		// Toast.makeText(this, "MyAlarmService.onCreate()",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {

		// Toast.makeText(this, "MyAlarmService.onBind()",
		// Toast.LENGTH_SHORT).show();

		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// Toast.makeText(getBaseContext(), "onStart() @ " +
		// System.currentTimeMillis() , Toast.LENGTH_LONG).show();
		String manufacturer = android.os.Build.MANUFACTURER;

		if (manufacturer.toLowerCase().equals("Motorola".toLowerCase())) {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					SessionActivity.mPlayer.play();
				}
			}, Constants.MOTOROLA_DELAY);
			Toast.makeText(getBaseContext(), "motorola",
					Toast.LENGTH_SHORT).show();

		} else if (manufacturer.toLowerCase().equals("alps".toLowerCase())) {
			SessionActivity.mPlayer.play();
			Toast.makeText(getBaseContext(), "regular speed",
					Toast.LENGTH_SHORT).show();
		} else if (manufacturer.toLowerCase().equals("LGE".toLowerCase())) {
			SessionActivity.mPlayer.play();
			Toast.makeText(getBaseContext(), "regular speed",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getBaseContext(), "didn't recognize manufacturer: \"" + manufacturer + "\"",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}

}

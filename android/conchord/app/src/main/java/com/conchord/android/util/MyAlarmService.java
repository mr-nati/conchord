package com.conchord.android.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.conchord.android.activity.SessionActivity;

public class MyAlarmService extends Service {

    private static final String TAG = MyAlarmService.class.getSimpleName();

	@Override
	public void onCreate() {
        Log.d(TAG, TAG + ".onCreate()");
	}

	@Override
	public IBinder onBind(Intent intent) {
        Log.d(TAG, TAG + ".onBind()");
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
        Log.d(TAG, TAG + ".onStart()");

		String manufacturer = android.os.Build.MANUFACTURER;
        Log.d(TAG, "Manufacturer = " + manufacturer);

        /*

         */
		if (manufacturer.toLowerCase().equals("Motorola".toLowerCase())) {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					SessionActivity.mPlayer.play();
				}
			}, Constants.MOTOROLA_DELAY);
            Log.d(TAG, "Going to wait " + Constants.MOTOROLA_DELAY + " millis for the other devices.");
		} else if (manufacturer.toLowerCase().equals("alps".toLowerCase())) {
			SessionActivity.mPlayer.play();
		} else if (manufacturer.toLowerCase().equals("LGE".toLowerCase())) {
			SessionActivity.mPlayer.play();
		} else {
            Log.e(TAG, "Didn't recognize manufacturer = " + manufacturer);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
        Log.d(TAG, TAG + ".onUnbind()");
		return super.onUnbind(intent);
	}

}

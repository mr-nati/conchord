package com.conchord.android;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

public class MyAlarmService extends Service {
	@Override
	public void onCreate() {
//		Toast.makeText(this, "MyAlarmService.onCreate()", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		
//		Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_SHORT).show();
		
		return null;
	}
	
	@Override 
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Toast.makeText(getBaseContext(), "onStart()", Toast.LENGTH_LONG).show();
		MainActivity.mPlayer.play();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}

}

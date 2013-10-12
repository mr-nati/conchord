package com.conchord.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MyAlarmService extends Service {
	@Override
	public void onCreate() {
		Toast.makeText(this, "MyAlarmService.onCreate()", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_SHORT).show();
		
		return null;
	}
	
	@Override 
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Toast.makeText(this, "MyAlarmService.onStart()", Toast.LENGTH_SHORT).show();
		MainActivity.clickedPlayButton();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		
		Toast.makeText(this, "MyAlarmService.onUnbind()", Toast.LENGTH_SHORT).show();

		return super.onUnbind(intent);
	}

}

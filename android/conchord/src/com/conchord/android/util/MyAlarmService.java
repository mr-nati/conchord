package com.conchord.android.util;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.conchord.android.R;
import com.conchord.android.activity.SessionActivity;

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
		
//		Toast.makeText(getBaseContext(), "onStart() @ " + System.currentTimeMillis() , Toast.LENGTH_LONG).show();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			 Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			     		SessionActivity.mPlayer.play();
			         } 
			    }, Constants.JELLY_BEAN_MR1_DELAY); 
		} else {
			SessionActivity.mPlayer.play();
		}
		
	}
	
	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}

}

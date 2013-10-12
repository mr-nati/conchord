package com.conchord.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TimeToJam extends BroadcastReceiver {

//	private static final int NOTIFICATION = 3456;


	@Override
	public void onReceive(Context context, Intent intent) {

		Toast.makeText(context, "Time to play", Toast.LENGTH_SHORT).show();
		
		// Got this code 
		/*
		 * final NotificationCompat.Builder builder = new
		 * NotificationCompat.Builder(context);
		 * builder.setSmallIcon(R.drawable.clock_alarm);
		 * builder.setContentTitle("Time is up");
		 * builder.setContentText("SLIMS"); builder.setVibrate(new long[] { 0,
		 * 200, 100, 200 }); final Notification notification = builder.build();
		 * 
		 * mNM.notify(NOTIFICATION, notification);
		 */
	}
}

package com.conchord.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.firebase.client.Firebase;

public class Utils {

	/**
	 * Returns true IFF this device is connected to the internet, either through
	 * WiFi, 3G or 4G.
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();

		return activeNetworkInfo != null;
	}
	
/*	public static long getNTPtime(Context context) {
		SntpClient client = new SntpClient();
		if (client.requestTime(Utils.someCaliNtpServers[0], 10000)) {

			return client.getNtpTime() + SystemClock.elapsedRealtime()
					- client.getNtpTimeReference();
		} else {
			Toast.makeText(context, "NTP error", Toast.LENGTH_LONG).show();
			return 0;
		}
	}*/

	public static final String[] someCaliNtpServers = { 
		"clock.isc.org",
		"ntp-cup.external.hp.com", 
		"clepsydra.dec.com", 
		"clock.via.net",
		"clock.sjc.he.net", 
		"clock.fmt.he.ne", 
		"nist1.symmetricom.com",
		"usno.pa-x.dec.com", 
		"nist1-la.WiTime.net", 
		"time.no-such-agency.net",
		"gps.layer42.net" };
	
	public static final String firebaseUrl = "https://conchord-app.firebaseio.com/";
	public static final String sessionsUrl = firebaseUrl + "sessions/";
	
	public static Firebase createSession(String sessionName, int songId) {
		// Reference to "sessions" Firebase
		Firebase sessions = new Firebase(sessionsUrl);
		Log.w("", "sessionsurl = " + sessionsUrl);
		
		Log.w("createSession()", "name = " + sessions.getName());
		Log.w("createSession()", "parent = " + sessions.getParent().getName());
		Log.w("createSession()", "path = " + sessions.getPath());
		Session session = new Session(sessionsUrl + sessionName, sessionName, songId);
		
		// Url of Firebase for "session" being created
		String url = sessionsUrl + session.getName();
		
		// Add the session to the Firebase
		sessions.child(session.getName()).setValue(session);
		
		sessions.child("hey");
		
		// Return a reference to the Firebase of this new Session
		return sessions.push();
	}
	
}

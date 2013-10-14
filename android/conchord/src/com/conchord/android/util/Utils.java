package com.conchord.android.util;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Toast;

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
	
	public static long getNTPtime(Context context) {
		SntpClient client = new SntpClient();
		if (client.requestTime(Utils.someCaliNtpServers[0], 10000)) {

			return client.getNtpTime() + SystemClock.elapsedRealtime()
					- client.getNtpTimeReference();
		} else {
			Toast.makeText(context, "NTP error", Toast.LENGTH_LONG).show();
			return 0;
		}
	}

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

}

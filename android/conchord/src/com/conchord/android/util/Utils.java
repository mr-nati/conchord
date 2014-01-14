package com.conchord.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.conchord.android.R;
import com.conchord.android.activity.HomeActivity;
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

	public class MediaFiles {
//		public static final int bulls_theme = R.raw.the_alan_parsons_project_sirius;
//		public static final int power_trip = R.raw.lecrae_power_trip;
		public static final int call_me_instrumental = R.raw.conchord__call_me_maybe_instrumental;
		public static final int call_me_acapella = R.raw.conchord__call_me_maybe_acapella;
		public static final int facebook_pop = R.raw.facebook_pop;
	}

	public static final String[] someCaliNtpServers = { "clock.isc.org",
			"ntp-cup.external.hp.com", "clepsydra.dec.com", "clock.via.net",
			"clock.sjc.he.net", "clock.fmt.he.ne", "nist1.symmetricom.com",
			"usno.pa-x.dec.com", "nist1-la.WiTime.net",
			"time.no-such-agency.net", "gps.layer42.net" };

}

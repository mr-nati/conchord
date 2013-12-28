package com.conchord.android.util;

public class Constants {

	
	public static final String firebaseUrl = "https://conchord-app.firebaseio.com/";
	public static final String sessionsUrl = firebaseUrl + "sessions/";
	public static final String usersUrlSuffix = "/users/";
	public static final String destroyFlagSuffix = "/destroy";
	
	public static final String KEY_SESSION = "session";
	public static final String KEY_IS_HOST = "ishost";
	public static final String KEY_HOST_ID = "hostId";
	public static final String KEY_ID = "id";
	public static final String KEY_PLAY_TIME = "playTime";
	public static final String KEY_SESSION_CLOSED = "closed";
	
	public static final String KEY_NTP_TIME = "ntpTime";
	public static final String KEY_SONG_TIME = "songTime";
	
	public static final int FLAG_DESTROY_SESSION_ON = 1;
	public static final int FLAG_DESTROY_SESSION_OFF = 0;
	
	public static final int START_TIME_DELAY = 5000;
	
}

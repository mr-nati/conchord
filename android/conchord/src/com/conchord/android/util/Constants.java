package com.conchord.android.util;

public class Constants {

	public static final String KEY_HOST_ID = "hostId";
	
	public static final String firebaseUrl = "https://conchord-app.firebaseio.com/";
	public static final String sessionsUrl = firebaseUrl + "sessions/";
	public static final String usersUrlSuffix = "/users/";
	public static final String destroyFlagSuffix = "/destroy";
	
	public static final String KEY_SESSION = "session";
	public static final String isHostKey = "ishost";
	
	public static final int FLAG_DESTROY_SESSION_ON = 1;
	public static final int FLAG_DESTROY_SESSION_OFF = 0;
	
}

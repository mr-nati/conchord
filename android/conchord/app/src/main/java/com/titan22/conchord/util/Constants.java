package com.titan22.conchord.util;

public class Constants {

    public static final String firebaseUrl = "https://conchord-app.firebaseio.com/";
    public static final String sessionsUrl = firebaseUrl + "sessions/";
    public static final String usersUrlSuffix = "/users/";
    public static final String destroyFlagSuffix = "/destroy";

    public static final String KEY_SESSION_CLOSED = "session_closed"; //TODO make this OPEN instead

    public static final int ROUNDTRIP_TIMEOUT = 15;
    public static final int NUM_NTP_ATTEMPTS = 30;  //TODO add more
}

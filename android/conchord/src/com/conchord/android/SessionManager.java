package com.conchord.android;

import com.conchord.android.util.Utils;
import com.firebase.client.Firebase;

public class SessionManager {
	
	private static SessionManager sManager = new SessionManager();
	
	private boolean initialized = false;
	private static String sessionName;
	private int songId;
	private Firebase devices;
	
	public SessionManager() { }
	
	// Should only be called once on the session manager to initialize it.
	public void initialize(String sessionName, int songId) {
		if (initialized) return;

		this.devices = new Firebase(Utils.sessionsUrl + sessionName + "/devices");
		this.sessionName = sessionName;
		this.songId = songId;
		initialized = true;
	}
	
	
	
	public static final synchronized SessionManager getInstance() {
		return sManager;
	}
	
	public String getSessionName() {
		return sessionName;
	}
	
	public int getSongID() {
		return songId;
	}
	
	public Firebase getDevicesFirebase() {
		return devices;
	}
	

}

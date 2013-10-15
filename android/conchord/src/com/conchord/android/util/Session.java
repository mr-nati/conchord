package com.conchord.android.util;

public class Session {

	private String name;
	private String hostId;
	private String playTime;
	private int songId;

	public Session(String sessionUrl, String hostId, int songId) {
		this.name = hostId;
		this.hostId = hostId;
		this.songId = songId;
	}

	public String getName() {
		return name;
	}

	public String getHostId() {
		return hostId;
	}

	public String playTime() {
		return playTime;
	}
	
	public int getSongId() {
		return songId;
	}
}

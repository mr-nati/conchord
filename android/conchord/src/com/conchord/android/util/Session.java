package com.conchord.android.util;

public class Session {

	private String name;
	private String hostId;
	private String playTime;
	private int songId;

	public Session(String sessionName, int songId) {
		this.name = sessionName;
		this.songId = songId;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String playTime() {
		return playTime;
	}

	public int getSongId() {
		return songId;
	}
	
	public void setSongId(int x) {
		this.songId = x;
	}
}

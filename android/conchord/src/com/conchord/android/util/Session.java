package com.conchord.android.util;

public class Session {

	
	private String hostId;
	private String somethingElse;
	
	private Session() { }
	
	public Session(String hostId, String somethingElse) {
		this.hostId = hostId;
		this.somethingElse = somethingElse;
	}
	
	public String getHostId() {
		return hostId;
	}
	
	public String getSomethingElse() {
		return somethingElse;
	}
	
	
}

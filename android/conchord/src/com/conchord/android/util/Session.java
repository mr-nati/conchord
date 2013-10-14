package com.conchord.android.util;

import java.util.ArrayList;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

public class Session {

	private String name;
	private String hostId;
	private int songId;
//	public Firebase devices;
	public ArrayList<String> deviceList = new ArrayList<String>();
		
	public Session(String sessionUrl, String hostId, int songId) {
		this.name = hostId;
		this.hostId = hostId;
		this.songId = songId;
//		this.devices = new Firebase(sessionUrl + "/devices");
		
		// Add host to list of devices
		deviceList.add(hostId.toUpperCase());
		
		// Add listener to Firebase devices
//		devices.addChildEventListener(new ChildEventListener() {
//			
//			@Override
//			public void onChildRemoved(DataSnapshot arg0) {
//				// TODO Auto-generated method stub
//				Log.d("childRemoved", arg0.toString());
//			}
//			
//			@Override
//			public void onChildMoved(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				Log.d("childMoved", arg0.toString());
//
//			}
//			
//			@Override
//			public void onChildChanged(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				Log.d("childChanged", arg0.toString());
//			}
//			
//			@Override
//			public void onChildAdded(DataSnapshot arg0, String arg1) {
//				// TODO Auto-generated method stub
//				Log.d("childAdded", arg0.toString());
//			}
//			
//			@Override
//			public void onCancelled() {
//				// TODO Auto-generated method stub
//				
//			}
//		});
	}
	
	public String getName() {
		return name;
	}
	
	public String getHostId() {
		return hostId;
	}
	
	public int getSongId() {
		return songId;
	}
	
//	public Firebase getDeviceListFirebase() {
//		return devices;
//	}
	
	
	
}

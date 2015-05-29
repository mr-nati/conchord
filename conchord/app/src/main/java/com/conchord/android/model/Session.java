package com.conchord.android.model;

import android.content.Context;
import android.util.Log;

import com.conchord.android.connection.datastore.DatastoreConnection;
import com.conchord.android.connection.datastore.FirebaseConnection;

import java.util.ArrayList;

/**
 * Created by ntessema on 5/25/15.
 */
public class Session {

    private static final String TAG = Session.class.getSimpleName();
    private Context mContext;

    private Device mMyDevice;
    private ArrayList<Device> mAllDevices = new ArrayList<Device>();

    DatastoreConnection dsConn;

    /* TODO eventually I should not be asking for a session name at all
    public Session(Context context) {
        this.mContext = context;
        dsConn = new FirebaseConnection(context);
        dsConn.createSession();
    }*/

    public Session(Context context, String sessionName) {
        this.mContext = context;
        dsConn = new FirebaseConnection(context);
        dsConn.createSession(sessionName);
    }

    public void join() {
        // add host to "users"
        mMyDevice = dsConn.joinSession();
        Log.d(TAG, "My ID is " + mMyDevice);

    }

}

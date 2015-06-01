package com.conchord.android.model;

import android.content.Context;
import android.util.Log;

import com.conchord.android.connection.datastore.DatastoreConnection;
import com.conchord.android.connection.datastore.FirebaseConnection;
import com.conchord.android.connection.session.wifi.WifiConnection;

import java.util.ArrayList;

/**
 * Created by ntessema on 5/25/15.
 */
public abstract class Session {

    private static final String TAG = Session.class.getSimpleName();
    private Context mContext;

    // All devices involved
    private Device mMyDevice;
    private ArrayList<Device> mAllDevices = new ArrayList<Device>();

    // The phase of the session
    private PHASE mPhase = PHASE.LOBBY;

    private DatastoreConnection mDsConn;

    public Session(Context context, String sessionName) {
        mContext = context;
        mDsConn = new FirebaseConnection(context);
        mDsConn.createSession(sessionName);
    }

    /**
     * Attempts to join the session. Returns false if it was unsuccessful.
     * @return
     */
    public void join() {

        // attempt to sync
        new WifiConnection(mContext, new WifiConnection.WifiConnectionListener() {
            @Override
            public void onWifiConnectionAttempted(long serverTime) {
                if (serverTime == 0) {
                    Log.e(TAG, "Couldn't get server time");
                } else {
                    Log.d(TAG, "OnWifiConnectionAttempted...serverTime = " + serverTime);
                    mMyDevice = mDsConn.joinSession();
                    Log.d(TAG, "My ID is " + mMyDevice);
                }
            }
        }).execute();

        // add host to "users"
    }

    public PHASE getPhase() {
        return mPhase;
    }

    public enum PHASE {
        LOBBY, PLAY
    }

        /* TODO eventually I should not be asking for a session name at all
    public Session(Context context) {
        this.mContext = context;
        mDsConn = new FirebaseConnection(context);
        mDsConn.createSession();
    }*/

}

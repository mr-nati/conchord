package com.conchord.android.connection.datastore;

import android.content.Context;

import com.conchord.android.model.Device;
import com.firebase.client.Firebase;

import com.conchord.android.utils.Constants;

/**
 * Created by ntessema on 5/25/15.
 */
public class FirebaseConnection implements DatastoreConnection {

    private static final String TAG = FirebaseConnection.class.getSimpleName();

    Firebase sessionFirebase;
    Firebase usersFirebase;
    Firebase myUserFirebase;

    public FirebaseConnection(Context context) {
        Firebase.setAndroidContext(context);
    }

    @Override
    public void createSession(String sessionName) {
        sessionFirebase = new Firebase(Constants.FIREBASE_SESSIONS_URL + sessionName);
        sessionFirebase.child("ayyye").setValue("i'm on it");
    }

    @Override
    public Device joinSession() {

        usersFirebase = new Firebase(Constants.FIREBASE_SESSIONS_URL
                + Constants.FIREBASE_USERS_SUFFIX);
        myUserFirebase = usersFirebase.push();

        myUserFirebase.child("test").setValue("test");

        return new Device(myUserFirebase.getKey());
    }
}

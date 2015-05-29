package com.conchord.android.connection.datastore;

import android.content.Context;

import com.firebase.client.Firebase;

import com.conchord.android.utils.Constants;

/**
 * Created by ntessema on 5/25/15.
 */
public class FirebaseConnection implements DatastoreConnection {

    private static final String TAG = FirebaseConnection.class.getSimpleName();

    Firebase sessionFirebase;

    public FirebaseConnection(Context context) {
        Firebase.setAndroidContext(context);
    }

    @Override
    public void createSession() {
        sessionFirebase = new Firebase(Constants.FIREBASE_SESSIONS_URL + "new");
        sessionFirebase.child("ayyye").setValue("i'm on it");
    }
}

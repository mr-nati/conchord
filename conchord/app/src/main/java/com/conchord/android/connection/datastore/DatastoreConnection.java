package com.conchord.android.connection.datastore;

import android.content.Context;

import com.conchord.android.model.Device;

/**
 * Created by ntessema on 5/25/15.
 */
public interface DatastoreConnection {

    public void createSession(String sessionName);

    public Device joinSession();


}

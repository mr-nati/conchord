package com.conchord.android.model;

/**
 * Created by ntessema on 5/28/15.
 */
public class Device {

    private String mId;

    public Device(String id) {
        setId(id);
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "Device [ID=" + getId() + "]";
    }
}

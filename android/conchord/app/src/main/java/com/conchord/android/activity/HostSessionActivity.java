package com.conchord.android.activity;

import android.app.Activity;
import android.os.Bundle;

import com.conchord.android.util.L;

/**
 * Created by NATI on 7/15/2014.
 */
public class HostSessionActivity extends Activity {

    /*              CLASS VARIABLES             */
    private static final String TAG = HostSessionActivity.class.getSimpleName();




    /*                  CODE                    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(TAG, "onCreate called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.d(TAG, "onStop called");
    }

}

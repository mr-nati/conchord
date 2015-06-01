package com.conchord.android.connection.session.wifi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.conchord.android.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ntessema on 5/25/15.
 */
public class WifiConnection extends AsyncTask<String, Void, Long> {

    private static final String TAG = WifiConnection.class.getSimpleName();

    List<WifiConnectionListener> mListeners = new ArrayList<>();

    private int mMyRoundtripTime = Integer.MAX_VALUE;
    private SntpClient client;

    public WifiConnection(Context context, WifiConnectionListener l) {
        mListeners.add(l);
    }

    @Override
    protected Long doInBackground(String... arg0) {

        String TAG = WifiConnection.class.getSimpleName() + ".doInBackground()";

        // Create SntpClient
        client = new SntpClient();
        int i = 0;

        while (!client.requestTime(Constants.someCaliNtpServers[0],
                Constants.WIFI_ROUNDTRIP_TIMEOUT)
                && i < Constants.WIFI_NUM_NTP_ATTEMPTS) {
            Log.e(TAG, "client.requestTime failed with " + client.myRoundtripTime);
            client = new SntpClient();
            i++;
        }

        if (client.getRoundTripTime() > Constants.WIFI_ROUNDTRIP_TIMEOUT) {
            Log.e(TAG, "couldn't get a good roundtrip (" + client.getRoundTripTime() + " ms)");
            return Long.valueOf(0);
        }

        //TODO apparently System.nanoTime() is more accurate
        Long time = client.getNtpTime();
        Log.d(TAG, "ntp time is " + time);
        Log.d(TAG, "time = " + System.currentTimeMillis());

//        Log.d(TAG, "ntpTime = " + ntpTime);
        Log.d(TAG, "currentthreadtimemillis = " + SystemClock.currentThreadTimeMillis());
        Log.d(TAG, "elapsed real time " + SystemClock.elapsedRealtime());
        //    if (mPlayer.isPlaying()) Log.d(TAG, "songTime = " + mPlayer.getCurrentPosition());

        return time;
    }

    @Override
    protected void onPostExecute(Long x) {
        super.onPostExecute(x);
        Log.d(TAG, "onPostExecute() called with server time " + x);
        notifyListeners(x);
    }

    public interface WifiConnectionListener {
        public void onWifiConnectionAttempted(long serverTime);
    }

    private void notifyListeners(long serverTime) {
        for (WifiConnectionListener l : mListeners) {
            l.onWifiConnectionAttempted(serverTime);
        }
    }

}

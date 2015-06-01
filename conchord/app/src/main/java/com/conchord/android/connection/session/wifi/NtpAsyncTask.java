package com.conchord.android.connection.session.wifi;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.conchord.android.utils.Constants;

/**
 * Created by ntessema on 5/31/15.
 */
class NtpAsyncTask extends AsyncTask<String, Void, Long> {
    SntpClient client;

    public NtpAsyncTask() {

    }

    @Override
    protected Long doInBackground(String... arg0) {

        String TAG = NtpAsyncTask.class.getSimpleName() + ".doInBackground()";

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

//        String TAG = NtpAsyncTask.class.getSimpleName() + ".onPostExecute()";
//
//        ntpTime = x;
//
//        Log.d(TAG, "ntpTime = " + ntpTime);
//        // if (mPlayer.isPlaying()) Log.e(TAG, "songTime = " +
//        // mPlayer.getCurrentPosition());
//        // long localTime = System.currentTimeMillis();
//        // long pos = mPlayer.getCurrentPosition();
//
//        if (isHost && needToSetFirebasePlayTime) {
//            long startTime = ntpTime + Constants.START_TIME_DELAY;
//
//            // edit Firebase server
//            setFirebasePlayTime(String.valueOf(startTime));
//
//            setAlarm(client.localESTIMATEDntpTime
//                    + Constants.START_TIME_DELAY);
//
//            // reset this flag
//            needToSetFirebasePlayTime = false;

//            textViewNtpPlayTime.setText("NTP play time: "
//                    + (startTime % 1000000));
//            textViewSnapshotNtp.setText("Snapshot NTP: " + (x % 1000000));
//            textViewRequestTime.setText("Request time: "
//                    + (client.myRequestTime % 1000000));
//            textViewRoundtripTime.setText("Roundtrip time: "
//                    + (client.myRoundtripTime % 1000000));
//            textViewSnapshotLocaLog.setText("Snapshot Local (GUESS): "
//                    + (client.localESTIMATEDntpTime % 1000000));
//            textViewTimeRemainingFromNtp.setText("Remaining local time: "
//                    + Constants.START_TIME_DELAY);

//        } else if (!isHost && receivingRelativePlayTime) {
//
//            long diff = timeToPlayAtInMillis - ntpTime;
//
//            Log.d(TAG, "millis btw ntptime and play time = " + diff);
//
////            setAlarm(client.localESTIMATEDntpTime + diff);
//
//            Log.d(TAG, "setting alarm for " + (client.localESTIMATEDntpTime + diff));
//
//            // reset this flag
////            receivingRelativePlayTime = false;
//
////            textViewNtpPlayTime.setText("NTP play time: "
////                    + (timeToPlayAtInMillis % 1000000));
////            textViewSnapshotNtp.setText("Snapshot NTP: " + (x % 1000000));
////            textViewRequestTime.setText("Request time: "
////                    + (client.myRequestTime % 1000000));
////            textViewRoundtripTime.setText("Roundtrip time: "
////                    + (client.myRoundtripTime % 1000000));
////            textViewSnapshotLocaLog.setText("Snapshot Local (GUESS): "
////                    + (client.localESTIMATEDntpTime % 1000000));
////            textViewTimeRemainingFromNtp.setText("Remaining local time: "
////                    + diff);
//        }
    }
}
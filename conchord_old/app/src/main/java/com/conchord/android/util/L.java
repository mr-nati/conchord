package com.conchord.android.util;

import android.util.Log;

/**
 * This is a really stupid class but I made it because some
 * devices have stupid Logcat messages that make it difficult
 * to debug my app. Because the Logcat filter only seems to
 * filter from messages I've created this class to Log for me
 * with a string that I can paste into the filter and let me
 * read only log messages that i care about.
 *
 * Created by NATI on 7/15/2014.
 */
public class L {

    private static final String filter_string = "cr7 :";

    public static void d(String TAG, String string) {
        Log.d(TAG, filter_string + string);
    }
    public static void e(String TAG, String string) { Log.e(TAG, filter_string + string); }

}

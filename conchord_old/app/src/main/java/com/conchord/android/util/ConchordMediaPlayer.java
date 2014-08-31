package com.conchord.android.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class ConchordMediaPlayer {

	private MediaPlayer mPlayer;

	public ConchordMediaPlayer(Context context, int fileLocation) {
		mPlayer = MediaPlayer.create(context, fileLocation);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public void play() {
		mPlayer.start();
	}

	public void seek(int numMillis) {
		mPlayer.pause();
		mPlayer.seekTo(mPlayer.getCurrentPosition() + numMillis);
		mPlayer.start();
	}
	
	public void seekTo(int millis) {
		mPlayer.seekTo(millis);
	}
	
	public void seekPaused(int numMillis) {
		mPlayer.pause();
		mPlayer.seekTo(mPlayer.getCurrentPosition() + numMillis);
	}

	public void pause() {
		mPlayer.pause();
	}

	public void stop() {
		mPlayer.stop();
	}
	
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	public int getCurrentPosition() {
		return mPlayer.getCurrentPosition();
	}

}

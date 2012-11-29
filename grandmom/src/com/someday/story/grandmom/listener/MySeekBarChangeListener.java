package com.someday.story.grandmom.listener;

import android.media.MediaPlayer;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MySeekBarChangeListener implements OnSeekBarChangeListener{
	private MediaPlayer mediaPlayer;
	
	public MySeekBarChangeListener(MediaPlayer mediaPlayser){
		this.mediaPlayer = mediaPlayser;
	}

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mediaPlayer.pause();
    } 

    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.seekTo(seekBar.getProgress());
        mediaPlayer.start();
    }
}

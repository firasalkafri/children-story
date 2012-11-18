package com.someday.story.littlestar.util;

import android.media.AudioManager;
import android.util.Log;

public class AudioUtils {
    
    public final static String LOG_TAG = AudioUtils.class.getSimpleName();
    
    public static void volumeUp(AudioManager am) {
        Log.d(LOG_TAG, "Volume Up!!");
        int currentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currentVolumn < maxVolumn) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumn+1, AudioManager.FLAG_PLAY_SOUND);
        }
    }
    
    public static void volumeDown(AudioManager am) {
        Log.d(LOG_TAG, "Volume Down!!");
        int currentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolumn > 0) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumn-1, AudioManager.FLAG_PLAY_SOUND);
        }
    }
}

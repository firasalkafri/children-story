package com.someday.story.grandmom;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.someday.story.grandmom.listener.MainGestureListener;
import com.someday.story.grandmom.listener.MySeekBarChangeListener;
import com.someday.story.grandmom.util.AudioUtils;

public class MainActivity extends Activity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final long[] VIBRATE_FORWARD = new long[]{100,100,100,100};
    private static final long[] VIBRATE_BACKWARD = new long[]{100,100,100,100,100,100};
    private static final long   VIBRATE_PAUSE = 300;
    private static final long   VIBRATE_START = 100;
    
	
    private boolean enableBackButton = false;
    
    private LinearLayout main;
    private TextView console;
    private SeekBar seekBar;

    private GestureDetector gestureDetector;
    
    public static final int CHANGE_BACKGROUND = 1;
    public static final int DISPLAY_ON_REPLAY_BTN = 2;

    private MediaPlayer mediaPlayer;
    protected AudioManager audioManager;
    private Vibrator vibrator;
    
    private static int[] audioTimeIndex;
    private static String[] imageIndex;
    
    protected PowerManager.WakeLock wakeLock;
    
    private Toast toast;
    
    private Handler handler = new BackgroundChangeHandler(this);

    private float mDensity;

    void handleMessage(Message msg) {
        final long currentPosition = mediaPlayer.getCurrentPosition();
        
        seekBar.setProgress((int)currentPosition);
        
        for (int idx = audioTimeIndex.length - 1; idx >= 0; idx--) {
            if (currentPosition > audioTimeIndex[idx]) {
                int resids = getResources().getIdentifier(imageIndex[idx], "drawable", getPackageName());
                main.setBackgroundResource(resids);
                break;
            }
        }
        
        Message newMsg = Message.obtain(msg);
        handler.sendMessageDelayed(newMsg, 200);
    }
    
    static class BackgroundChangeHandler extends Handler {
        private final WeakReference<MainActivity> activity;
        public BackgroundChangeHandler(MainActivity activity) {
            this.activity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity main = activity.get();
            if (main != null) main.handleMessage(msg);
        }
    }
    
    public void replay() {
        mediaPlayer.seekTo(0);
        seekBar.setProgress(0);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(), getString(R.string.activity_main_title), Toast.LENGTH_SHORT).show();
        
        super.onCreate(savedInstanceState);
        
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = createMediaPlayer(R.raw.grandma);
        vibrator = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        audioTimeIndex = getResources().getIntArray(R.array.audio_time_index);
        imageIndex = getResources().getStringArray(R.array.image_index);
        
        Message m = handler.obtainMessage(CHANGE_BACKGROUND, null);
        handler.sendMessageDelayed(m, 200);
        
        gestureDetector = new GestureDetector(new MainGestureListener(this));
        
        main = new LinearLayout(this);
        main.setBackgroundColor(Color.GRAY);
        main.setBackgroundResource(R.drawable.help);
        main.setGravity(Gravity.LEFT | Gravity.TOP);
        main.setOrientation(LinearLayout.VERTICAL);
        
        console = new TextView(this);
        LayoutParams tvParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        tvParams.weight = 1;
        console.setLayoutParams(tvParams);
        console.setBackgroundColor(Color.TRANSPARENT);
        console.setTextColor(Color.GRAY);
        console.setTextSize(30);
        
        seekBar = new SeekBar(this);
        seekBar.setMax(mediaPlayer.getDuration());
        LayoutParams sbParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        sbParams.gravity = Gravity.BOTTOM;
        seekBar.setLayoutParams(sbParams);
        seekBar.setBackgroundColor(Color.TRANSPARENT);
        seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(this.mediaPlayer));
        seekBar.setVisibility(SeekBar.INVISIBLE);
        main.addView(console);
        main.addView(seekBar);
        
        setContentView(main);
        
        toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);
        toast.setText(getString(R.string.text_tab_to_play) + " " + getString(R.string.help_string2));
        toast.show();
        
        toast.setDuration(Toast.LENGTH_SHORT);
        
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, LOG_TAG);
        this.wakeLock.acquire();
        
        mDensity = getResources().getDisplayMetrics().density;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_credit:
                Log.d(LOG_TAG, "Touch Credit menu.");
                startActivity(new Intent(getApplicationContext(), CreditActivity.class));
                break;
            case R.id.menu_help:
                Log.d(LOG_TAG, "Touch Help menu.");
                startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        this.enableBackButton = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        this.wakeLock.release();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private MediaPlayer createMediaPlayer(int audioSource) {
		MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
				audioSource);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return mediaPlayer;
    }

    public MediaPlayer getMediaPlayer(){
    	return this.mediaPlayer;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    
    @Override
    public void onBackPressed() {
        if (enableBackButton) {
            super.onBackPressed();
            return;
        }
        this.enableBackButton = true;
        Toast.makeText(getApplicationContext(), getString(R.string.text_double_back_tab), Toast.LENGTH_SHORT).show();
    }

    public void rewind(int currentPosition, int jumpDistance) {
    	vibrator.vibrate(VIBRATE_BACKWARD,-1);   
    	toast.cancel();
        if (currentPosition >= jumpDistance){
            mediaPlayer.seekTo(currentPosition - jumpDistance);
            seekBar.setProgress(currentPosition - jumpDistance);
        }else{
            mediaPlayer.seekTo(0);
            seekBar.setProgress(0);
        } 
        toast.setText(getString(R.string.text_rewind));
		toast.show();
    }
    
    public void fastForward(int currentPosition, int jumpDistance) {
    	 if (currentPosition + jumpDistance < mediaPlayer.getDuration()) {
    		vibrator.vibrate(VIBRATE_FORWARD,-1);   
        	toast.cancel();
            mediaPlayer.seekTo(currentPosition + jumpDistance);
            seekBar.setProgress(currentPosition + jumpDistance);
            toast.setText(getString(R.string.text_fast_foward));
    		toast.show();
        }
    }
    
    public void volumnUp() {
    	vibrator.vibrate(200);
    	toast.cancel();
        AudioUtils.volumeUp(audioManager);
        toast.setText(getString(R.string.text_volume_up));
        toast.show();
    }
    
    public void volumnDown() {
    	vibrator.vibrate(200);
    	toast.cancel();
        AudioUtils.volumeDown(audioManager);
        toast.setText(getString(R.string.text_volume_down));
        toast.show();
    }

    public void mediaPause(){
    	vibrator.vibrate(VIBRATE_PAUSE);
    	toast.cancel();
    	this.mediaPlayer.pause();
    	console.setText(getString(R.string.text_pause));
    	toast.setText(getString(R.string.text_pause));
    	toast.show();
    }
    
    public void mediaStart(){
    	vibrator.vibrate(VIBRATE_START);
    	toast.cancel();
    	this.mediaPlayer.start();
    	seekBar.setVisibility(SeekBar.VISIBLE);
    	console.setText(getString(R.string.text_play));
      	toast.setText(getString(R.string.text_play));
      	toast.show();
    }
    
    public boolean isPlaying(){
    	return this.mediaPlayer.isPlaying();
    }
}

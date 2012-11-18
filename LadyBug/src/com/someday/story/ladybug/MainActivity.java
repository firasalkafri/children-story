package com.someday.story.ladybug;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.someday.story.ladybug.util.AudioUtils;

public class MainActivity extends Activity implements OnGestureListener {

    private static final String LOG_TAG = MainActivity.class.getName();
    
    private boolean enableBackButton = false;
    
    private LinearLayout main;
    private TextView console;
    private SeekBar sb;

    private GestureDetector gestureDetector;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    private static final int SHORT_JUMP_DISTANCE = 10 * 1000;
    private static final int LONG_JUMP_DISTANCE = 20 * 1000;

    private static final long MIN_ACTION_DELAY = 300; //�곗� 紐�� �����0.3珥�
    private static final long ACCELETE_ACTION_DELAY = 3000; //媛����� �����3珥�
    private static final int MIN_ACTION_X_DISTANCE = 160; //X異�理�� 諛�� 1�몄�
    private static final int MIN_ACTION_Y_DISTANCE = 80; //Y異�理�� 諛�� 0.5�몄�
    
    public static final int CHANGE_BACKGROUND = 1;
    public static final int DISPLAY_ON_REPLAY_BTN = 2;

    private static MediaPlayer mp;
    protected AudioManager am;
    
    private static int[] audioTimeIndex;
    private static String[] imageIndex;
    
    protected PowerManager.WakeLock wakeLock;
    
    private Toast toast;
    
    private Handler handler = new BackgroundChangeHandler(this);
    
    private float firstX;
    private float firstY;
    private long lastActionTime;
    private float mDensity;

    void handleMessage(Message msg) {
        final long currentPosition = mp.getCurrentPosition();
        
        sb.setProgress((int)currentPosition);
        
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
    
    static class MySeekBarChangeListener implements OnSeekBarChangeListener{

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            mp.pause();
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mp.seekTo(seekBar.getProgress());
            mp.start();
        }
        
    }
    
    public void replay() {
        mp.seekTo(0);
        sb.setProgress(0);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        Toast.makeText(getApplicationContext(), getString(R.string.activity_main_title), Toast.LENGTH_SHORT).show();
        
        super.onCreate(savedInstanceState);
        
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mp = getMediaPlayer(R.raw.lady_bug);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        audioTimeIndex = getResources().getIntArray(R.array.audio_time_index);
        imageIndex = getResources().getStringArray(R.array.image_index);
        
        Message m = handler.obtainMessage(CHANGE_BACKGROUND, null);
        handler.sendMessageDelayed(m, 200);
        
        gestureDetector = new GestureDetector(this);
        
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
        
        sb = new SeekBar(this);
        sb.setMax(mp.getDuration());
        LayoutParams sbParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        sbParams.gravity = Gravity.BOTTOM;
        sb.setLayoutParams(sbParams);
        sb.setBackgroundColor(Color.TRANSPARENT);
        sb.setOnSeekBarChangeListener(new MySeekBarChangeListener());
        sb.setVisibility(SeekBar.INVISIBLE);
        main.addView(console);
        main.addView(sb);
        
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
        mp.stop();
        this.wakeLock.release();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private MediaPlayer getMediaPlayer(int audioSource) {
		MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
				audioSource);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return mediaPlayer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                toast.cancel();
                
                if (lastActionTime + MIN_ACTION_DELAY > System.currentTimeMillis()) break;
                int jumpDistance;
                
                if(lastActionTime + ACCELETE_ACTION_DELAY > System.currentTimeMillis()) jumpDistance = SHORT_JUMP_DISTANCE;
                else jumpDistance = LONG_JUMP_DISTANCE;
                
                int currentPosition = mp.getCurrentPosition();
                
                if (event.getX() - firstX > MIN_ACTION_X_DISTANCE) fastForward(currentPosition, jumpDistance);
                else if (event.getX() - firstX < -MIN_ACTION_X_DISTANCE) rewind(currentPosition, jumpDistance);
                else if (event.getY() - firstY < -MIN_ACTION_Y_DISTANCE) volumnUp();
                else if (event.getY() - firstY > MIN_ACTION_Y_DISTANCE) volumnDown();
                
                lastActionTime = System.currentTimeMillis();
                toast.show();
                
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent arg0) {
        return false;
    }
    
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		toast.cancel();
		
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;
		int currentPosition = mp.getCurrentPosition();
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			rewind(currentPosition, SHORT_JUMP_DISTANCE);
		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			fastForward(currentPosition, SHORT_JUMP_DISTANCE);
		else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
			volumnUp();
		else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
			volumnDown();
		
		toast.show();
		return false;
    }

    public void onLongPress(MotionEvent ev) {
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

    public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent ev) {
    }

    public boolean onSingleTapUp(MotionEvent ev) {
        toast.cancel();
        if (mp.isPlaying()) {
            mp.pause();
            console.setText(getString(R.string.text_pause));
            toast.setText(getString(R.string.text_pause));
        } else {
            mp.start();
            sb.setVisibility(SeekBar.VISIBLE);
            console.setText(getString(R.string.text_play));
            toast.setText(getString(R.string.text_play));
        }
        toast.show();
        return false;
    }

    private void rewind(int currentPosition, int jumpDistance) {
        if (currentPosition >= jumpDistance){
            mp.seekTo(currentPosition - jumpDistance);
            sb.setProgress(currentPosition - jumpDistance);
        }else{
            mp.seekTo(0);
            sb.setProgress(0);
        } 
        toast.setText(getString(R.string.text_rewind));
    }
    
    private void fastForward(int currentPosition, int jumpDistance) {
        if (currentPosition + jumpDistance < mp.getDuration()) {
            mp.seekTo(currentPosition + jumpDistance);
            sb.setProgress(currentPosition + jumpDistance);
            toast.setText(getString(R.string.text_fast_foward));
        }
        
    }
    
    private void volumnUp() {
        AudioUtils.volumeUp(am);
        toast.setText(getString(R.string.text_volume_up));
    }
    
    private void volumnDown() {
        AudioUtils.volumeDown(am);
        toast.setText(getString(R.string.text_volume_down));
    }

}

package com.someday.story.grandmom.listener;

import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.someday.story.grandmom.MainActivity;

/**
 * Main Activity의 사용자 모션을 처리하는 클래스
 * @author abh0518
 *
 */
public class MainGestureListener implements OnGestureListener{

	private MainActivity mainActivity;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	 
	private static final int SHORT_JUMP_DISTANCE = 10 * 1000;
	private static final int LONG_JUMP_DISTANCE = 20 * 1000;
	
	private static final long MIN_ACTION_DELAY = 300; //占쎄�占�筌�옙占�占쏙옙占쏙옙占�.3�ο옙
	private static final long ACCELETE_ACTION_DELAY = 3000; //揶�옙占쏙옙占쏙옙 占쏙옙占쏙옙占��ο옙
	private static final int MIN_ACTION_X_DISTANCE = 160; //X�곤옙筌ㅿ옙占�獄�옙占�1占쎈�占�
    private static final int MIN_ACTION_Y_DISTANCE = 80; //Y�곤옙筌ㅿ옙占�獄�옙占�0.5占쎈�占�
    
	
	public MainGestureListener(MainActivity mainActivity){
		this.mainActivity = mainActivity;
	}
	
	@Override
	public boolean onDown(MotionEvent arg0) {
        return false;
    }
     
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;
		int currentPosition = mainActivity.getMediaPlayer().getCurrentPosition();
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			mainActivity.rewind(currentPosition, SHORT_JUMP_DISTANCE);
		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			mainActivity.fastForward(currentPosition, SHORT_JUMP_DISTANCE);
		else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
			mainActivity.volumnUp();
		else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
			mainActivity.volumnDown();		
		return false;
    }

	@Override
	public void onLongPress(MotionEvent ev) {
    }

	@Override
	public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
        return false;
    }

	@Override
	public void onShowPress(MotionEvent ev) {
    }

	@Override
	public boolean onSingleTapUp(MotionEvent ev) {
		if(mainActivity.isPlaying()){
			mainActivity.mediaPause();
		}
		else{
			mainActivity.mediaStart();
		}
        return false;
    }
	
}

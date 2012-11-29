package com.someday.story.grandmom.util;

import android.app.Service;
import android.content.Context;

public class Vibrator {

	public Vibrator(Context context){
		Vibrator vibrator = (Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
		
	}
	
}

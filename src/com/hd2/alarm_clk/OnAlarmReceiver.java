package com.hd2.alarm_clk;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TimePicker;

public class OnAlarmReceiver extends Activity {
	TimePicker tp;
	MediaPlayer mMediaPlayer;
	boolean alarmSet = false;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // We want the alarm window to be shown
        // even when the device is sleeping and is locked 
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        						  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
				  				  WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().setFlags(WindowManager.LayoutParams. FLAG_SHOW_WHEN_LOCKED,
				  				  WindowManager.LayoutParams. FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
				  				  WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams. FLAG_KEEP_SCREEN_ON,
				  				  WindowManager.LayoutParams. FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_alarm_clk);
        tp = (TimePicker)this.findViewById(R.id.timePicker1);
        tp.setIs24HourView(true);
        tp.setVisibility(View.INVISIBLE);
        
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.buzzer);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.start();
        alarmSet = true;
    }

    public void CancelAlarm(View view) {
    	if (alarmSet) {
    		// Stop playing alarm
    		try {
        		if (mMediaPlayer.isPlaying()) {
        			mMediaPlayer.stop();
        			mMediaPlayer.release();            		
            	}
        	} catch (IllegalStateException e) {
                System.out.println("MediaPlayer.stop() error");
        	}
    		
    		// Set alarm_on_boot preference to false
        	SharedPreferences mAlarmPreferences = getSharedPreferences("AlarmPreferences", 0);
        	SharedPreferences.Editor mAlarmEditor = mAlarmPreferences.edit();
        	mAlarmEditor.putBoolean("alarm_on_boot", false);
        	mAlarmEditor.putInt("alarm_time_h", 0);
        	mAlarmEditor.putInt("alarm_time_m", 0);
        	mAlarmEditor.commit();
        	
            finish();
    	}       	
    }
    
    public void SnoozeAlarm(View view) {
    	if (alarmSet) {
	    	// Stop playing alarm
	    	try {
	    		if (mMediaPlayer.isPlaying()) {
	    			mMediaPlayer.stop();
	    			mMediaPlayer.release();            		
	        	}
	    	} catch (IllegalStateException e) {
	            System.out.println("MediaPlayer.stop() error");
	    	}
	    	
	    	// Current time
	    	Time now = new Time();
	    	now.setToNow();
	    	int time_h = now.hour;
	    	int time_m = now.minute;
	    	
	    	// 10 min snoozing 
	    	Calendar cal = Calendar.getInstance();
		   	cal.set(Calendar.HOUR_OF_DAY, time_h);
		   	cal.set(Calendar.MINUTE, time_m + 10);     
		   	cal.set(Calendar.SECOND, 0);
		   	
		   	// Just to be safe in case the device reboots, set alarm_on_boot preference to true
		   	SharedPreferences mAlarmPreferences = getSharedPreferences("AlarmPreferences", 0);
		   	SharedPreferences.Editor mAlarmEditor = mAlarmPreferences.edit();
	    	mAlarmEditor.putBoolean("alarm_on_boot", true);
	    	mAlarmEditor.putInt("alarm_time_h", time_h);
        	mAlarmEditor.putInt("alarm_time_m", (time_m+10));
        	mAlarmEditor.commit();
	        
	    	// Set Alarm
		   	Intent intent = new Intent(this, OnAlarmReceiver.class);
		   	PendingIntent pendingIntent = PendingIntent.getActivity(this,
		           													12345,
		           													intent,
		           													PendingIntent.FLAG_CANCEL_CURRENT);
		    AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
		    
	        finish();
    	}
    }
  
}
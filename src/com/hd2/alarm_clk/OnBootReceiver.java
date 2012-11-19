package com.hd2.alarm_clk;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.Time;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        	// Check if we have set the alarm before booting up
        	SharedPreferences mAlarmPreferences = context.getSharedPreferences("AlarmPreferences", 0);
            boolean AlarmOnBoot = mAlarmPreferences.getBoolean("alarm_on_boot", false);
            if (AlarmOnBoot) {
            	//Get current time
            	Time now = new Time();
    	    	now.setToNow();
    	    	//Get time of alarm
            	int time_h = mAlarmPreferences.getInt("alarm_time_h", now.hour);
            	int time_m = mAlarmPreferences.getInt("alarm_time_m", now.minute);
            	int time_s = now.second + 5;
            	//Check if we passed the alarm time and fix the problem
            	if (now.hour > time_h) {
            		time_h = now.hour;
            	}
            	if (now.minute > time_m) {
            		time_m = now.minute + 1;
            	}
            	if (now.minute == time_m) {
	            	if (now.second > 55) {
	            		time_m = now.minute + 1;
	            		time_s = 0;
	            	}
            	}
            	//Set the alarm to 
            	Calendar cal = Calendar.getInstance();
    		   	cal.set(Calendar.HOUR_OF_DAY, time_h);
    		   	cal.set(Calendar.MINUTE, time_m);     
    		   	cal.set(Calendar.SECOND, time_s);
    		   	Intent i = new Intent(context, OnAlarmReceiver.class);  
			   	PendingIntent pendingIntent = PendingIntent.getActivity(context,
			           													12345,
			           													i,
			           													PendingIntent.FLAG_CANCEL_CURRENT);
			    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        }
    }

}

/*
 * koko:
 * 		This is a simple alarm application built specifically
 * 		for HD2's cLK bootloader mostly as an experiment.
 * 
 * The target was to be able to:
 * 		1.Set the alarm,
 * 		2.Shutdown device**,
 * 		3.Wake up the device and enter Android,
 * 		4.Sound the alarm at startup.
 *
 *		**More like making the device to enter a kind of suspend mode
 *		  for a time equal to (alarm time - current time). For that,
 *		  this application is using the "oem-" prefix in PowerManager's 
 *		  reboot(String reason) method as the bootreason. The side-effects,
 *		  I observed, are that the apk needs to be signed
 *		  and pushed to /system/app in order to work as intended.
 */
package com.hd2.alarm_clk;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmClk extends Activity {
	TimePicker tp;
	PowerManager pm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_alarm_clk);
        tp = (TimePicker)this.findViewById(R.id.timePicker1);
        tp.setIs24HourView(true);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_alarm_clk, menu);
        return true;
    }

    public void SetAlarm(View view) {
    	// Current time
    	Time now = new Time();
    	now.setToNow();
    	int time_h = now.hour;
    	int time_m = now.minute;
    	
    	// Time of alarm
    	int alarm_time_h = tp.getCurrentHour();
    	int alarm_time_m = tp.getCurrentMinute();
    	
    	// Calculate the total time in minutes
    	int mintosusp = (alarm_time_m - time_m) + ((alarm_time_h - time_h) * 60);
    	if (mintosusp == 0) {
    		makeToast("Try setting a different time for the alarm.");
    	} else {
    		if (mintosusp < 0) {
			   	mintosusp = mintosusp + 1440;//zzz
    		}    
    		if (mintosusp > 255) {
    			makeToast("Alarm time for more than 255 min from now\n is not supported.");
    		} else {
	    		// cLK will use the MinutesToSuspend to suspend device for that time
	        	final int MinutesToSuspend = mintosusp;
	    		
	        	// Set alarm_on_boot preference to true
	    		SharedPreferences mAlarmPreferences = getSharedPreferences("AlarmPreferences", 0);
	    		SharedPreferences.Editor mAlarmEditor = mAlarmPreferences.edit();
	        	mAlarmEditor.putBoolean("alarm_on_boot", true);
	        	mAlarmEditor.commit();
	            
	    		// Inform user for the time the device will suspend till it reboots and sounds the alarm
	    		makeToast("Device will enter suspend mode after reset.\nAlarm is set for " + Integer.toString(MinutesToSuspend) + " minute(s) from now.");
	
	        	// Wait for 3 sec before rebooting
	    		final Handler handler = new Handler(); 
	            Timer t = new Timer(); 
	            t.schedule(new TimerTask() { 
	            	@Override
	    			public void run() { 
	            		handler.post(new Runnable() { 
	            			public void run() { 
	            				/*
	            			     * Here we reboot using "oem-" prefix in boot reason
	            			     * This way, the kernel will pass the MinutesToSuspend
	            			     * to bootloader so that it will detect we want to suspend device
	            			     * #############################################################
	            			     * # quoted from arch\arm\mach-msm\pm.c                        #
	            			     * # if (!strncmp(cmd, "oem-", 4)) {                           #
	            				 * # 	unsigned code = simple_strtoul(cmd + 4, 0, 16) & 0xff; #
	            				 * #	restart_reason = 0x6f656d00 | code;                    #
	            				 * # }                                                         #
	            				 * #############################################################
	            				 * 
	            				 * Problem is that we can't detect correctly the MinutesToSuspend
	            				 * if it is larger that 255 min.
	            				 * Workaround would be to add a new reboot_reason
	            				 * in arch\arm\mach-msm\pm.c. Something like this maybe:
	            			     * ###############################################################
	            			     * # if (!strncmp(cmd, "alarm-", 6)) {                           #
	            				 * # 	unsigned code = simple_strtoul(cmd + 6, 0, 16) & 0xfff; #
	            				 * #	restart_reason = 0x6f656000 | code;                      #
	            				 * # }                                                           #
	            				 * ###############################################################
	            				 * and then calling here: 
	            				 * pm.reboot("alarm-" + MinutesToSuspend);
	            			     */
	            				pm.reboot("oem-" + MinutesToSuspend);
	            				finish();
	                        } 
	                    }); 
	                } 
	            }, 3000);
	    		
    		}
    	}
    	
    }
    
    public void SnoozeAlarm(View view) {
    	makeToast("No alarm set.");
    }
    
    public void CancelAlarm(View view) {
    	moveTaskToBack(true);
    }
    
    public void makeToast(String msg) {
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}

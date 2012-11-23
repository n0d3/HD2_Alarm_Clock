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

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;

public class AlarmClk extends Activity {
	private final int ID_MENU_INF = 0;
	private final int ID_MENU_EXIT = 1;
	TimePicker tp;
	PowerManager pm;
	
	boolean clk = false;
	int clk_ver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_alarm_clk);
        RootTools.debugMode = false;
        
        // Root?
        if (RootTools.isRootAvailable()) {
        	if (RootTools.isAccessGiven()) {
        		//Check bootloader
        		clk = false;
        		Command get_bldr = new Command(0, "sed -n 's/clk/&/p' /proc/cmdline")
            	{
        			@Override
        	        public void output(int id, String line)
        	        {
        				if(line.contains("clk=")) {
        					clk = true;
        					String ver = line.substring(line.indexOf("clk=") + 4, line.length()).trim().replace(".", "");
        					if(ver.length() == 2) {
        						ver = ver + "00";
        					}
        					else if(ver.length() == 3) {
        						ver = ver + "0";
        					}
        					clk_ver = Integer.decode(ver);
        				}
        	        }
            	};
        		try {
    				RootTools.getShell(true).add(get_bldr).waitForFinish();
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			} catch (TimeoutException e) {
    				e.printStackTrace();
    			}
        		if(clk) {
        			if(clk_ver < 1514) {
        				AlertDialog.Builder WrongVersion = new AlertDialog.Builder(this);
        				WrongVersion.setMessage("Current version of cLK is outdated!\nAlarmClk is compatible with version higher than 1.5.1.4");
        				WrongVersion.setPositiveButton("Exit",
                								new DialogInterface.OnClickListener() {public void onClick(DialogInterface arg0, int arg1) {finish();}}
                								);
        				WrongVersion.show();
        			}
        		} else {
        			AlertDialog.Builder NotCLK = new AlertDialog.Builder(this);
        			NotCLK.setMessage("AlarmClk is compatible ONLY with cLK bootloader");
        			NotCLK.setPositiveButton("Exit",
            								new DialogInterface.OnClickListener() {public void onClick(DialogInterface arg0, int arg1) {finish();}}
            								);
        			NotCLK.show();
        		}        		
        	} else {
        		AlertDialog.Builder NoRoot = new AlertDialog.Builder(this);
        		NoRoot.setMessage("Root access could not be obtained. Please check whether your device is rooted, or restart the application to try again.");
        		NoRoot.setPositiveButton("Exit",
        								new DialogInterface.OnClickListener() {public void onClick(DialogInterface arg0, int arg1) {finish();}}
        								);
            	NoRoot.show();
        	}
        } else {
        	AlertDialog.Builder NoRoot = new AlertDialog.Builder(this);
    		NoRoot.setMessage("Root access could not be obtained. Please check whether your device is rooted, or restart the application to try again.");
    		NoRoot.setPositiveButton("Exit",
    								new DialogInterface.OnClickListener() {public void onClick(DialogInterface arg0, int arg1) {finish();}}
    								);
        	NoRoot.show();
        }
        
        Time now = new Time();
    	now.setToNow();
    	tp = (TimePicker)this.findViewById(R.id.timePicker1);
        tp.setIs24HourView(true);
        tp.setCurrentHour(Integer.valueOf(now.hour));
        tp.setCurrentMinute(Integer.valueOf(now.minute));
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, ID_MENU_INF, Menu.NONE, R.string.menu_item_0);
    	menu.add(Menu.NONE, ID_MENU_EXIT, Menu.NONE, R.string.menu_item_1);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == ID_MENU_INF)	{
    		final TextView link = new TextView(this);
         	final SpannableString s = new SpannableString("This is an experimental Alarm Clock\n\nYou must have cLK > 1_5_1_4 and\nrunning a patched kernel.\n\nMore info at xda thread:\nforum.xda-developers.com/showthread.php?t=1990111");
        	Linkify.addLinks(s, Linkify.WEB_URLS);
        	link.setText(s);
        	link.setMovementMethod(LinkMovementMethod.getInstance());
        	
    		AlertDialog.Builder info = new AlertDialog.Builder(this);
    		info.setInverseBackgroundForced(true);
    		info.setTitle("Info");
    		info.setIcon(android.R.drawable.ic_dialog_info);
    		info.setView(link);
    		info.setCancelable(false);
    		info.setNegativeButton("Close", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface arg0, int arg1) {
    				arg0.cancel();
    				}
    			}
    		);
        	info.show();
    		return true;
    	}
    	else if(item.getItemId() == ID_MENU_EXIT) {
    		this.finish();
    		return true;
    	}
		return false;    
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
    		if (clk_ver == 1514 && mintosusp > 255) {
    			makeToast("Alarm time for more than 255 min from now\n is not supported.");
    		} else {
	    		// cLK will use the MinutesToSuspend to suspend device for that time
	        	final int MinutesToSuspend = mintosusp;
	    		
	        	// Set alarm_on_boot preference to true
	    		SharedPreferences mAlarmPreferences = getSharedPreferences("AlarmPreferences", 0);
	    		SharedPreferences.Editor mAlarmEditor = mAlarmPreferences.edit();
	        	mAlarmEditor.putBoolean("alarm_on_boot", true);
	        	mAlarmEditor.putInt("alarm_time_h", alarm_time_h);
	        	mAlarmEditor.putInt("alarm_time_m", alarm_time_m);
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
	            				if (clk_ver == 1514) {
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
		            				 */
		            				pm.reboot("oem-" + MinutesToSuspend);
	            				} else {
		            				/*
		            				 * Workaround is to add a new reboot_reason
		            				 * in arch\arm\mach-msm\pm.c:
		            			     * ###############################################################
		            			     * # if (!strncmp(cmd, "S", 1)) {                           	 #
		            				 * # unsigned code = simple_strtoul(cmd + 1, 0, 16) & 0x00ffffff;#
		            				 * #	restart_reason = 0x6f656000 | code;                      #
		            				 * # }                                                           #
		            				 * ###############################################################
		            			     */
		            				pm.reboot("S" + MinutesToSuspend);	            		        	
	            		        }
	            				
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

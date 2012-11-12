package com.hd2.alarm_clk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        	// Check if we have set the alarm before booting up
        	SharedPreferences mAlarmPreferences = context.getSharedPreferences("AlarmPreferences", 0);
            boolean AlarmOnBoot = mAlarmPreferences.getBoolean("alarm_on_boot", false);
            if (AlarmOnBoot) {
	            Intent i = new Intent(context, OnAlarmReceiver.class);  
			   	PendingIntent pendingIntent = PendingIntent.getActivity(context,
			           													12345,
			           													i,
			           													PendingIntent.FLAG_CANCEL_CURRENT);
			    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			    am.set(AlarmManager.RTC_WAKEUP, 1, pendingIntent);
            }
        }
    }

}

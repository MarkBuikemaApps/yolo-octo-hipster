package com.markbuikema.juliana32.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.SettingsActivity;

public class BootReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("JulianaReceiver", "Received boot for Juliana");
		
		SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFERENCES, 0);
		boolean notify = prefs.getBoolean(SettingsActivity.NOTIFICATIONS, false);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, NotificationService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		am.cancel(pi);

		if (notify) {
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
					+ MainActivity.NOTIFICATION_INTERVAL * 6 * 1000, MainActivity.NOTIFICATION_INTERVAL * 60 * 1000, pi);
		}
	}
}

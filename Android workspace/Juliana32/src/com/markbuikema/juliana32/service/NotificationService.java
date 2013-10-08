package com.markbuikema.juliana32.service;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.SettingsActivity;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.model.NieuwsItem;

public class NotificationService extends Service {

	private final static String TAG = "JulianaService";
	public final static String NEWS_ID = "news_id";
	public static final String FROM_NOTIFICATION = "from_notification";
	private static int originalCount;

	private WakeLock mWakeLock;

	/**
	 * Simply return null, since our Service will not be communicating with any
	 * other components. It just does its work silently.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * This is where we initialize. We call this when onStart/onStartCommand is
	 * called by the system. We won't do anything with the intent here, and you
	 * probably won't, either.
	 */
	private void handleIntent(Intent intent) {
		// obtain the wake lock
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();

		// // check the global background data setting
		// ConnectivityManager cm = (ConnectivityManager)
		// getSystemService(CONNECTIVITY_SERVICE);
		// if (!cm.getBackgroundDataSetting()) {
		// stopSelf();
		// return;
		// }

		// do the actual work, in a separate thread
		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFERENCES, 0);
		boolean facebook = prefs.getBoolean(SettingsActivity.FACEBOOK, true);
		boolean website = prefs.getBoolean(SettingsActivity.WEBSITE, true);
		if (!facebook && !website)
			stopSelf();
		new NieuwsRetriever(facebook, website) {

			@Override
			protected void onPostExecute(List<NieuwsItem> result) {
				if (result.size() > originalCount && originalCount > 0) {
					pushNotification(result);
					originalCount = result.size();
				} else
					stopSelf();

			}
		}.execute();
	}

	public void pushNotification(List<NieuwsItem> items) {

		int newItems = items.size() - originalCount;

		Intent cIntent = new Intent(NotificationService.this, MainActivity.class);
		cIntent.putExtra(FROM_NOTIFICATION, true);

		String title = "Juliana '32";
		String text = "";

		if (newItems > 1)
			text = newItems + " nieuwe nieuwsberichten";
		else
			text = items.get(items.size() - 1).getTitle();

		cIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		PendingIntent clickIntent = PendingIntent.getActivity(NotificationService.this, 0, cIntent, 0);
		PendingIntent pIntent = PendingIntent.getBroadcast(NotificationService.this, 0, cIntent, 0);
		NotificationManager nm = (NotificationManager) NotificationService.this.getSystemService(NOTIFICATION_SERVICE);
		Notification not = new Notification();
		not.setLatestEventInfo(NotificationService.this, title, text, pIntent);
		not.icon = R.drawable.ic_launcher;
		not.flags = Notification.FLAG_AUTO_CANCEL;
		not.ledARGB = Color.BLUE;
		not.tickerText = text;
		not.when = items.get(items.size() - 1).getCreatedAt().getTimeInMillis();
		not.vibrate = new long[] {
				50, 50, 50
		};
		not.contentIntent = clickIntent;
		nm.notify(0, not);

		stopSelf();
	}

	/**
	 * This is deprecated, but you have to implement it if you're planning on
	 * supporting devices with an API level lower than 5 (Android 2.0).
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		handleIntent(intent);
		Log.d(TAG, "onStart");

	}

	/**
	 * This is called on 2.0+ (API level 5 or higher). Returning START_NOT_STICKY
	 * tells the system to not restart the service if it is killed because of poor
	 * resource (memory/cpu) conditions.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		Log.d(TAG, "onStartCommand");
		return START_NOT_STICKY;
	}

	/**
	 * In onDestroy() we release our wake lock. This ensures that whenever the
	 * Service stops (killed for resources, stopSelf() called, etc.), the wake
	 * lock will be released.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
	}
}
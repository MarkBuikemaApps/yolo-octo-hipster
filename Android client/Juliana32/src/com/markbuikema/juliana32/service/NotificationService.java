package com.markbuikema.juliana32.service;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;

public class NotificationService extends Service {

	private final static String TAG = "JulianaService";
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

//		// check the global background data setting
//		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//		if (!cm.getBackgroundDataSetting()) {
//			stopSelf();
//			return;
//		}
		

		// do the actual work, in a separate thread
		new PollTask().execute();
	}

	private class PollTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			Log.d(TAG, "Polling...");
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(MainActivity.BASE_SERVER_URL + "/news/count");

			try {
				HttpResponse response = client.execute(get);
				String countText = EntityUtils.toString(response.getEntity());
				int count = Integer.valueOf(countText);
				if (count > originalCount && originalCount > 0) {
					int difference = count - originalCount;
					originalCount = count;
					Log.d(TAG,"Original: " + originalCount + ", New: " + count + ", Difference: " + difference);
					return difference;
				}
				if (originalCount == 0) {
				 originalCount = count;
				}
			} catch (Exception e) {

			}

			return 0;
		}

		/**
		 * In here you should interpret whatever you fetched in doInBackground and
		 * push any notifications you need to the status bar, using the
		 * NotificationManager. I will not cover this here, go check the docs on
		 * NotificationManager.
		 * 
		 * What you HAVE to do is call stopSelf() after you've pushed your
		 * notification(s). This will: 1) Kill the service so it doesn't waste
		 * precious resources 2) Call onDestroy() which will release the wake lock,
		 * so the device can go to sleep again and save precious battery.
		 */
		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {

				new NewNewsRetriever().execute(result);

			} else {
				stopSelf();
			}

		}
	}

	private class NewNewsRetriever extends AsyncTask<Integer, Void, ArrayList<NieuwsItem>> {

		@Override
		protected ArrayList<NieuwsItem> doInBackground(Integer... params) {
			
			Log.d(TAG, "NewNewsRetriever started");

			ArrayList<NieuwsItem> items = new ArrayList<NieuwsItem>();
			int count = params[0];
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(MainActivity.BASE_SERVER_URL + "/news/get");
			try {
				HttpResponse response = client.execute(get);
				String jsonString = EntityUtils.toString(response.getEntity());

				JSONObject json = new JSONObject(jsonString);
				try {
					JSONArray array = json.getJSONArray("newsItem");
					for (int i = array.length() - 1; i > array.length() - 1 - count; i--) {
						processJSONObject(items, array.getJSONObject(i));
					}
				} catch (JSONException e) {
					JSONObject singleObject = json.getJSONObject("newsItem");
					processJSONObject(items, singleObject);
				}

			} catch (Exception e) {

			}
			return items;
		}

		private void processJSONObject(ArrayList<NieuwsItem> items, JSONObject obj) {
			try {
				int id = obj.getInt("id");
				String content = obj.getString("content");
				long createdAt = obj.getLong("createdAt");
				String title = obj.getString("title");
				String subTitle = obj.getString("subTitle");
				String detailUrl = obj.getString("detailUrl");
				NieuwsItem item = new NormalNieuwsItem(id, title, subTitle, content, createdAt, detailUrl);
				items.add(item);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(ArrayList<NieuwsItem> result) {

			Intent intent = new Intent(NotificationService.this, MainActivity.class);

			String title = "";
			String text = "";
			if (result.size() > 1) {
				text = NotificationService.this.getResources().getString(R.string.multiple_notifications);
				title = NotificationService.this.getResources().getString(R.string.app_name);
			} else {
				title = result.get(0).getTitle();
				text = result.get(0).getSubTitle();
			}
			Intent cIntent = new Intent(NotificationService.this, MainActivity.class);
			cIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

			PendingIntent clickIntent = PendingIntent.getActivity(NotificationService.this, 0, cIntent, 0);
			PendingIntent pIntent = PendingIntent.getBroadcast(NotificationService.this, 0, intent, 0);
			NotificationManager nm = (NotificationManager) NotificationService.this.getSystemService(NOTIFICATION_SERVICE);
			Notification not = new Notification();
			not.setLatestEventInfo(NotificationService.this, title, text, pIntent);
			not.icon = R.drawable.ic_launcher;
			not.flags = Notification.FLAG_AUTO_CANCEL;
			not.ledARGB = Color.BLUE;
			not.tickerText = result.size()>1?"Nieuwe nieuwsberichten":"Nieuw nieuwsbericht";
			not.when = ((NormalNieuwsItem)result.get(result.size()-1)).getCreatedAt();
			not.vibrate = new long[] {50,500,50};
			not.contentIntent = clickIntent;
			nm.notify(0, not);

			stopSelf();
		}
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
	public void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
	}
}
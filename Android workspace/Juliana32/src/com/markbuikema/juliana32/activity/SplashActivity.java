package com.markbuikema.juliana32.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper.PhotoGetter;
import com.markbuikema.juliana32.util.Util;

public class SplashActivity extends Activity {

	public static final String TAG = "SplashActivity";
	public static final boolean TIMEOUT_ENABLED = false;
	private DataLoader loader;

	private ImageView logo;

	// public static final boolean OFFLINE_MODE = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		logo = (ImageView) findViewById(R.id.julianaLogo);
		Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.splash, options);

		logo.setImageBitmap(bmp);

		loader = new DataLoader() {
			@Override
			protected void onPostExecute(Boolean result) {
				if (!result.booleanValue())
					Toast.makeText(SplashActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
				else {
					Intent i = new Intent(SplashActivity.this, MainActivity.class);
					startActivity(i);
				}
				finish();
			}
		};
		loader.execute();
	}

	@Override
	public void onBackPressed() {
		loader.cancel(true);
		super.onBackPressed();
	}

	private class DataLoader extends AsyncTask<Void, Void, Boolean> {

		private static final long TIMEOUT = 15000;
		private boolean failure;

		@Override
		protected Boolean doInBackground(Void... params) {

			final DataManager manager = DataManager.getInstance();

			TeamsRetriever teamsRetriever = new TeamsRetriever(SplashActivity.this) {
				@Override
				protected void onPostExecute(List<Season> result) {
					manager.setTeams(result);
				}
			};

			SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFERENCES, 0);
			final boolean facebook = prefs.getBoolean(SettingsActivity.FACEBOOK, true);
			final boolean website = prefs.getBoolean(SettingsActivity.WEBSITE, true);

			NieuwsRetriever nieuwsRetriever = new NieuwsRetriever(facebook, website) {
				@Override
				protected void onPostExecute(List<NieuwsItem> result) {

					if (result.size() == 0 && (facebook || website)) {
						failure = true;
						return;
					}

					manager.setNieuwsItems(result);
					int count = 0;
					for (NieuwsItem item : result)
						if (item.isFromFacebook() && ((FacebookNieuwsItem) item).isPhoto())
							count++;

					final CountCallback callback = new CountCallback(count) {

						@Override
						public void onCall() {
							Log.d("CountCallback", "onCall()");
							Util.linkPhotosToTeam();
						}

					};
					for (NieuwsItem item : result)
						if (item.isFromFacebook()) {
							final FacebookNieuwsItem fbi = ((FacebookNieuwsItem) item);
							if (fbi.isPhoto())
								new PhotoGetter() {
									@Override
									protected void onPostExecute(List<String> result) {
										for (String photo : result)
											fbi.addPhoto(photo);
										callback.onCallback();
									}
								}.execute(fbi);
						}
				}
			};
			nieuwsRetriever.execute();

			teamsRetriever.execute();

			long timeout = System.currentTimeMillis() + TIMEOUT;

			while (manager.requiresData()) {
				if (System.currentTimeMillis() > timeout && TIMEOUT_ENABLED || failure) {
					nieuwsRetriever.cancel(true);
					teamsRetriever.cancel(true);
					Log.e(TAG, "TIMEOUT");
					return false;
				}

				manager.printLoadingStatus();
				Log.d(TAG, "---");

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return true;
		}

	}

	public abstract class CountCallback {
		private int countLeft;
		private static final String TAG = "CountCallback";

		public CountCallback(int count) {
			Log.d(TAG, "CountCallback created with count " + count);
			countLeft = count;
		}

		public final void onCallback() {
			if (countLeft > 1)
				countLeft--;
			else
				onCall();

			Log.d(TAG, "onCallback() called, new count = " + countLeft);
		}

		public abstract void onCall();
	}

}

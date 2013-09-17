package com.markbuikema.juliana32.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.asynctask.TeasersRetriever;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper.PhotoGetter;

public class SplashActivity extends Activity {

	public static final String TAG = "SplashActivity";

	// public static final boolean OFFLINE_MODE = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new DataLoader() {
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
		}.execute();
	}

	private class DataLoader extends AsyncTask<Void, Void, Boolean> {

		private static final long TIMEOUT = 15000;

		@Override
		protected Boolean doInBackground(Void... params) {

			final DataManager manager = DataManager.getInstance();

			TeasersRetriever teasersRetriever = new TeasersRetriever() {
				@Override
				protected void onPostExecute(List<TeaserNieuwsItem> result) {
					manager.setTeaserItems(result);
				}
			};
			teasersRetriever.execute();

			TeamsRetriever teamsRetriever = new TeamsRetriever() {
				@Override
				protected void onPostExecute(List<Season> result) {
					manager.setTeams(result);
				}
			};

			NieuwsRetriever nieuwsRetriever = new NieuwsRetriever() {
				@Override
				protected void onPostExecute(List<NieuwsItem> result) {
					manager.setNieuwsItems(result);
					for (NieuwsItem item : result) {
						if (!item.isFromFacebook())
							continue;
						final FacebookNieuwsItem fbi = ((FacebookNieuwsItem) item);
						if (fbi.isPhoto())
							new PhotoGetter() {
								@Override
								protected void onPostExecute(List<String> result) {
									for (String photo : result)
										fbi.addPhoto(photo);
									Log.d("ADDED_PHOTOS", "title: " + fbi.getTitle() + "count: " + fbi.getPhotoCount());

								}

							}.execute(fbi);
					}

				}

			};
			nieuwsRetriever.execute();

			teamsRetriever.execute();

			long timeout = System.currentTimeMillis() + TIMEOUT;

			while (manager.requiresData()) {
				if (System.currentTimeMillis() > timeout) {
					teasersRetriever.cancel(true);
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
}

package com.markbuikema.juliana32.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.tools.DataManager;
import com.markbuikema.juliana32.tools.NieuwsRetriever;
import com.markbuikema.juliana32.tools.TeamsRetriever;
import com.markbuikema.juliana32.tools.TeasersRetriever;

public class SplashActivity extends Activity {

	public static final String TAG = "SplashActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new DataLoader() {
			protected void onPostExecute(Boolean result) {
				if (!result.booleanValue()) {
					Toast.makeText(SplashActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
				} else {
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

			NieuwsRetriever nieuwsRetriever = new NieuwsRetriever() {
				@Override
				protected void onPostExecute(List<NieuwsItem> result) {
					manager.setNieuwsItems(result);
				}
			};
			nieuwsRetriever.execute();

			TeamsRetriever teamsRetriever = new TeamsRetriever() {
				@Override
				protected void onPostExecute(List<Season> result) {
					manager.setTeams(result);
				}
			};
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

				Log.d(TAG, "data loaded: " + !manager.requiresData());
				Log.d(TAG, "---");

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return true;
		}

	}
}
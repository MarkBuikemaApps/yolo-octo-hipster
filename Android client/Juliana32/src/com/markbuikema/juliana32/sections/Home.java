package com.markbuikema.juliana32.sections;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.tools.FixtureAdapter;

public class Home {

	private static final String TAG = "Home Section";
	private static final int MARGIN = 20;

	private MainActivity activity;

	private ListView fixtures;
	private FixtureAdapter fixtureAdapter;
	private LinearLayout teaserContainer;
	private ArrayList<TeaserNieuwsItem> teasers;

	private int teaserWidth;
	private int teaserHeight;

	public Home(MainActivity act) {
		activity = act;
		View mainView = act.findViewById(R.id.homeView);
		fixtures = (ListView) mainView.findViewById(R.id.fixtures);
		teaserContainer = (LinearLayout) mainView.findViewById(R.id.teaserContainer);

		teasers = new ArrayList<TeaserNieuwsItem>();

		setTeaserDimensions();

		new TeaserRetriever().execute();
	}

	public void populateGames() {
		ArrayList<Game> games = activity.getLatestGames();
		fixtureAdapter = new FixtureAdapter(activity, games);
		fixtures.setAdapter(fixtureAdapter);
	}
	
	private void setTeaserDimensions() {
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		teaserWidth = (d.getWidth() / 3) * 2;
		float height = ((float) teaserWidth / 246f) * 96f;
		teaserHeight = (int) height;
	}

	@SuppressLint("NewApi")
	public void addTeaser(final TeaserNieuwsItem item) {
		Log.d(TAG, item.toString());
		teasers.add(item);
		View view = LayoutInflater.from(activity).inflate(R.layout.listitem_teaseritem, null);
		TextView teaserText = (TextView) view.findViewById(R.id.teaserTitle);
		ImageView teaserImage = (ImageView) view.findViewById(R.id.teaserImage);
		teaserText.setText(item.getTitle());
		BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), item.getImage());
		teaserImage.setImageDrawable(drawable);
		LayoutParams params = new LayoutParams(teaserWidth, teaserHeight);
		params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		params.gravity = teasers.size() % 2 == 1 ? Gravity.LEFT : Gravity.RIGHT;
		view.setLayoutParams(params);

		view.setClickable(true);
		view.setContentDescription(item.getSubTitle());

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.requestNiewsDetailPage(item);
			} 
		});

		teaserContainer.addView(view);
	}

	

	private class TeaserRetriever extends AsyncTask<Void, TeaserNieuwsItem, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d(TAG, "Started retrieving teaser items");
			HttpClient client = new DefaultHttpClient();
			HttpGet teaserGet = new HttpGet(MainActivity.BASE_SERVER_URL + "/teasers/get");
			try {
				HttpResponse response = client.execute(teaserGet);

				String jsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
				Log.d(TAG, jsonString);
				JSONObject obj = null;
				try {
					obj = new JSONObject(jsonString);
					JSONArray array = obj.getJSONArray("teaserNewsItem");
					for (int i = 0; i < array.length(); i++) {
						processTeaserJSONObject(array.getJSONObject(i));
					}
				} catch (JSONException jsone) {
					try {
						JSONObject singleObject = obj.getJSONObject("teaserNewsItem");
						processTeaserJSONObject(singleObject);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void processTeaserJSONObject(JSONObject obj) {
			try {
				int id = obj.getInt("id");
				String title = obj.getString("title");
				String content = obj.getString("content");
				String subTitle = obj.getString("subTitle");
				String detailUrl = obj.getString("detailUrl");
				String imgUrl = obj.getString("imgUrl");
				TeaserNieuwsItem item = new TeaserNieuwsItem(id, title, subTitle, content, imgUrl, detailUrl);
				publishProgress(item);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(TeaserNieuwsItem... values) {
			for (TeaserNieuwsItem item : values) {

				addTeaser(item);

			}
		}
	}
}

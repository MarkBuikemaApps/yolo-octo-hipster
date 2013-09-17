package com.markbuikema.juliana32.asynctask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.util.FacebookHelper;

public class NieuwsRetriever extends AsyncTask<Void, NieuwsItem, List<NieuwsItem>> {

	final static long RETRIEVAL_TIMEOUT = 7000;
	private static final String TAG = "NieuwsRetriever";
	private static final String GET_URL = MainActivity.BASE_SERVER_URL + "/news/get";
	private String statusCode;

	@Override
	protected List<NieuwsItem> doInBackground(Void... params) {
		if (MainActivity.OFFLINE_MODE) {
			List<NieuwsItem> items = new ArrayList<NieuwsItem>();
			FacebookHelper.addFacebookFeed(items);
			return items;
		}
		HttpClient client = new DefaultHttpClient();

		final ArrayList<NieuwsItem> items = new ArrayList<NieuwsItem>();

		HttpGet get = new HttpGet(GET_URL);
		try {
			HttpResponse response = client.execute(get);
			statusCode = Integer.toString(response.getStatusLine().getStatusCode());
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");

			JSONObject object = null;
			try {
				object = new JSONObject(json);
				if (json.length() < 4)
					return items;
				JSONArray array = object.getJSONArray("newsItem");
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					NieuwsItem item = processNieuwsItemJSON(obj);
					if (item != null)
						items.add(item);
				}
			} catch (JSONException e) {
				try {
					JSONObject singleObject = object.getJSONObject("newsItem");
					NieuwsItem item = processNieuwsItemJSON(singleObject);
					if (item != null)
						items.add(item);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		} catch (ClientProtocolException e) {
			Log.e(TAG, "CPE");
		} catch (IOException e) {
			Log.d(TAG, "IOE");
		}

		FacebookHelper.addFacebookFeed(items);

		Collections.sort(items);

		return items;
	}

	private NieuwsItem processNieuwsItemJSON(JSONObject obj) {
		Log.d(TAG, "Processing JSON Object for news item");
		try {
			int id = obj.getInt("id");
			String content = obj.getString("content");
			long createdAt = obj.getLong("createdAt");
			String title = obj.getString("title");
			String subTitle = obj.getString("subTitle");
			String detailUrl = obj.getString("detailUrl");
			NieuwsItem item = new NormalNieuwsItem(id, title, subTitle, content, createdAt, detailUrl);

			try {
				JSONArray photos = obj.getJSONArray("photos");
				for (int i = 0; i < photos.length(); i++) {
					String photo = photos.getString(i);
					item.addPhoto(photo);

					Log.d(TAG, "Photo added to newsitem " + item.getId() + ": " + photo);
				}
			} catch (JSONException e) {
				String photo = obj.getString("photos");
				item.addPhoto(photo);
			}
			return item;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getStatusCode() {
		return statusCode;
	}

}
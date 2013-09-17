package com.markbuikema.juliana32.asynctask;

import java.io.IOException;
import java.util.ArrayList;
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

import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;

public class TeasersRetriever extends AsyncTask<Void, Void, List<TeaserNieuwsItem>> {

	private static final String TAG = "SplashActivity";

	@Override
	protected List<TeaserNieuwsItem> doInBackground(Void... arg0) {
		List<TeaserNieuwsItem> items = new ArrayList<TeaserNieuwsItem>();
		if (MainActivity.OFFLINE_MODE)
			return items;

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(MainActivity.BASE_SERVER_URL + "/teasers/get");
		try {
			HttpResponse response = client.execute(get);

			String jsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
			JSONObject obj = null;
			try {
				obj = new JSONObject(jsonString);
				JSONArray array = obj.getJSONArray("teaserNewsItem");
				for (int i = 0; i < array.length(); i++) {
					TeaserNieuwsItem item = processTeaserJSONObject(array.getJSONObject(i));
					if (item != null)
						items.add(item);
				}
			} catch (JSONException jsone) {
				try {
					JSONObject singleObject = obj.getJSONObject("teaserNewsItem");
					TeaserNieuwsItem item = processTeaserJSONObject(singleObject);
					if (item != null)
						items.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return items;
	}

	private TeaserNieuwsItem processTeaserJSONObject(JSONObject obj) {
		try {
			int id = obj.getInt("id");
			String title = obj.getString("title");
			String content = obj.getString("content");
			String subTitle = obj.getString("subTitle");
			String detailUrl = obj.getString("detailUrl");
			String imgUrl = obj.getString("imgUrl");
			TeaserNieuwsItem item = new TeaserNieuwsItem(id, title, subTitle, content, imgUrl, detailUrl);
			return item;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
}
package com.markbuikema.juliana32.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import android.os.AsyncTask;

public class WebsiteNieuwsItem extends NieuwsItem {

	private String detailUrl;
	private String id;

	public WebsiteNieuwsItem(String id, String title, String subTitle, GregorianCalendar createdAt, String detailUrl) {

		super(title, subTitle, null, createdAt);
		this.detailUrl = detailUrl;
		this.id = id;
	}

	public WebsiteNieuwsItem() {
		super(null, null, null, null);
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isContentLoaded() {
		return content != null;
	}

	@Override
	public void startLoading(OnContentLoadedListener callback) {
		if (!isContentLoaded())
			new ContentLoader(callback).execute();
	}

	private class ContentLoader extends AsyncTask<Void, String, String> {

		private OnContentLoadedListener callback;
		private List<String> photos;

		private ContentLoader(OnContentLoadedListener callback) {
			this.callback = callback;
			photos = new ArrayList<String>();
		}

		@Override
		protected String doInBackground(Void... params) {

			if (detailUrl == null)
				return null;

			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(detailUrl);

			try {
				String output = EntityUtils.toString(client.execute(get).getEntity(), "utf-8");
				Document doc = Jsoup.parse(output);
				Element ele = doc.getElementById("content-text");
				// if (ele == null)
				// return null;

				for (Element img : ele.getElementsByTag("img"))
					publishProgress(img.attr("src"));

				String content = Jsoup.clean(ele.html(), new Whitelist().addTags("br", "a", "b", "i").addAttributes("a", "href"));

				return content;

			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			for (String value : values)
				photos.add(value);
		}

		@Override
		protected void onPostExecute(String result) {
			content = result;
			WebsiteNieuwsItem.this.photos = photos;
			callback.onContentLoaded(result, photos);
		}

	}
}

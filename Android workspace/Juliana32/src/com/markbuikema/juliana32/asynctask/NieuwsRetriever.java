package com.markbuikema.juliana32.asynctask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;

import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.util.FacebookHelper;
import com.markbuikema.juliana32.util.Util;

public class NieuwsRetriever extends AsyncTask<Void, NieuwsItem, List<NieuwsItem>> {

	private static final String TAG = "NieuwsRetriever";
	private static final String GET_URL = "http://www.svjuliana32.nl/nieuws/";
	private String statusCode;
	private boolean retrieveFromFacebook;
	private boolean retrieveFromWebsite;

	public NieuwsRetriever(boolean facebook, boolean website) {
		retrieveFromFacebook = facebook;
		retrieveFromWebsite = website;
	}

	@Override
	protected List<NieuwsItem> doInBackground(Void... params) {
		Log.d("nieuwsloader", "1");
		final ArrayList<NieuwsItem> items = new ArrayList<NieuwsItem>();

		if (retrieveFromWebsite) {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

			HttpGet get = new HttpGet(GET_URL);
			Log.d("nieuwsloader", "2");
			try {
				HttpResponse response = client.execute(get);
				Log.d("nieuwsloader", "3");
				statusCode = Integer.toString(response.getStatusLine().getStatusCode());
				if (!statusCode.startsWith("2"))
					return null;
				String html = null;
				html = EntityUtils.toString(response.getEntity(), "UTF-8");
				Document doc = Jsoup.parse(html);
				Elements eles = doc.getElementsByClass("item-inhoud-home");
				for (Element ele : eles) {
					String title;
					String subTitle;
					GregorianCalendar createdAt;
					String detailUrl;

					Element a = ele.getElementsByClass("title").get(0);
					detailUrl = a.attr("href");
					title = a.attr("title");

					Element sum = ele.getElementsByClass("sum").get(0);
					subTitle = sum.html();
					try {
						createdAt = Util.parseDate(ele.getElementsByClass("datum").get(0).html().split("plaatst op: ")[1]);
					} catch (ArrayIndexOutOfBoundsException e) {
						createdAt = new GregorianCalendar();
					}

					title = title.replace("&eacute;", "'");
					subTitle = subTitle.replace("&eacute;", "�");

					items.add(new NormalNieuwsItem(title, subTitle, createdAt, detailUrl));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (retrieveFromFacebook)
			FacebookHelper.addFacebookFeed(items);

		Collections.sort(items);

		return items;
	}
}
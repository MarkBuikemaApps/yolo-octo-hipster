package com.markbuikema.juliana32.sections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.activities.MainActivity.FailureReason;
import com.markbuikema.juliana32.activities.MainActivity.Page;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;

public class Nieuws {

	private final static String TAG = "Nieuws";

	private ListView nieuwsList;
	private NieuwsAdapter nieuwsAdapter;
	private Bitmap facebookIcon;
	private Bitmap julianaIcon;
	private ImageButton refreshButton;
	private ProgressBar loading;
	private MainActivity activity;

	private NieuwsRetriever nieuwsRetriever;

	private int itemRequestId = -1;

	public Nieuws(final Activity act) {
		activity = (MainActivity) act;
		View mainView = act.findViewById(R.id.nieuwsView);
		nieuwsList = (ListView) mainView.findViewById(R.id.nieuwsList);
		refreshButton = (ImageButton) act.findViewById(R.id.menuRefresh);
		loading = (ProgressBar) act.findViewById(R.id.loading);

		facebookIcon = BitmapFactory.decodeResource(act.getResources(), R.drawable.facebook);
		julianaIcon = BitmapFactory.decodeResource(act.getResources(), R.drawable.ic_launcher);

		nieuwsAdapter = new NieuwsAdapter(act);
		nieuwsList.setAdapter(nieuwsAdapter);

		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		nieuwsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NieuwsItem item = nieuwsAdapter.getItem(arg2);
				activity.requestNiewsDetailPage(item);
			}
		});

		refresh();
	}

	public void showRefreshButton() {
		refreshButton.setVisibility(View.VISIBLE);
	}

	private class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

		private Context context;

		public NieuwsAdapter(Context context) {
			super(context, 0);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NieuwsItem item = getItem(position);

			if (item instanceof NormalNieuwsItem) {

				if (convertView == null || (convertView != null && convertView.findViewById(R.id.titleView) == null)) {
					convertView = LayoutInflater.from(context).inflate(R.layout.listitem_nieuwsitem, null);
				}

				TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
				TextView subTitleView = (TextView) convertView.findViewById(R.id.subTitleView);
				ImageView icon = (ImageView) convertView.findViewById(R.id.nieuwsIconView);
				titleView.setText(item.getTitle());
				subTitleView.setText(item.getSubTitle());
				TextView createdAtView = (TextView) convertView.findViewById(R.id.createdAtView);
				createdAtView.setText(((NormalNieuwsItem) item).getCreatedAtString());
				if (item.isFromFacebook())
					icon.setImageBitmap(facebookIcon);
				else
					icon.setImageBitmap(julianaIcon);

				return convertView;
			} else {
				if (convertView == null || (convertView != null && convertView.findViewById(R.id.teaserImage) == null)) {
					convertView = LayoutInflater.from(context).inflate(R.layout.listitem_teaseritem, null);
				}

				TextView titleView = (TextView) convertView.findViewById(R.id.teaserTitle);
				ImageView imageView = (ImageView) convertView.findViewById(R.id.teaserImage);
				titleView.setText(item.getTitle());
				Bitmap bmp = ((TeaserNieuwsItem) item).getImage();
				if (bmp != null) {
					imageView.setImageBitmap(bmp);
				}

				return convertView;
			}

		}

	}

	public class NieuwsRetriever extends AsyncTask<Void, TeaserNieuwsItem, ArrayList<NieuwsItem>> {

		final static long RETRIEVAL_TIMEOUT = 7000;

		@Override
		protected void onPreExecute() {
			nieuwsAdapter.clear();
			loading.setVisibility(View.VISIBLE);
			refreshButton.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(ArrayList<NieuwsItem> result) {
			loading.setVisibility(View.GONE);
			if (activity.getPage() == Page.NIEUWS && !activity.isNieuwsDetailShown()) refreshButton.setVisibility(View.VISIBLE);
			for (NieuwsItem item : result)
				nieuwsAdapter.add(item);

			if (itemRequestId != -1) {

				NieuwsItem item = getNewsItem(itemRequestId);
				activity.requestNiewsDetailPage(item);

				itemRequestId = -1;
			}
		}

		@Override
		protected ArrayList<NieuwsItem> doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();

			ArrayList<NieuwsItem> items = new ArrayList<NieuwsItem>();

			HttpGet get = new HttpGet(MainActivity.BASE_SERVER_URL + "/news/get");
			try {
				HttpResponse response = client.execute(get);

				String json = EntityUtils.toString(response.getEntity(), "UTF-8");

				JSONObject object = null;
				try {
					object = new JSONObject(json);
					JSONArray array = object.getJSONArray("newsItem");
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						processJSONObject(items, obj);
					}
				} catch (JSONException e) {
					try {
						JSONObject singleObject = object.getJSONObject("newsItem");
						processJSONObject(items, singleObject);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "CPE");
			} catch (IOException e) {
				Log.d(TAG, "IOE");
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

				try {
					JSONArray photos = obj.getJSONArray("photos");
					for (int i = 0; i < photos.length(); i++) {
						String photo = photos.getString(i);
						item.addPhoto(photo);
					}
				} catch (JSONException e) {
					String photo = obj.getString("photos");
					item.addPhoto(photo);
				} finally {
					items.add(item);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(TeaserNieuwsItem... values) {
			for (TeaserNieuwsItem item : values) {
				nieuwsAdapter.add(item);
			}
		}
	}

	public void refresh() {
		nieuwsRetriever = new NieuwsRetriever();
		nieuwsRetriever.execute();
	}

	public NieuwsItem getNewsItem(int newsId) {
		for (int i = 0; i < nieuwsAdapter.getCount(); i++)
			if (nieuwsAdapter.getItem(i).getId() == newsId) return nieuwsAdapter.getItem(i);
		return null;
	}

	public void setItemRequest(int newsId) {
		itemRequestId = newsId;
	}

}

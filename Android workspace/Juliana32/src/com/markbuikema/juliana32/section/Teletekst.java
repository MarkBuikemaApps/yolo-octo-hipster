package com.markbuikema.juliana32.section;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.viewpagerindicator.UnderlinePageIndicator;

public class Teletekst {

	public static final String TAG = "Teletekst";

	private MainActivity activity;
	private ProgressBar progressBar;

	private SharedPreferences prefs;

	private ViewPager ttPager;
	private PagerAdapter pagerAdapter;
	private UnderlinePageIndicator pagerIndicator;
	private String[] imageUrls;

	private TextView swipeHint;

	private int maxIndex = 0;

	public Teletekst(final MainActivity act) {
		activity = act;
		View mainView = activity.findViewById(R.id.teletekstView);
		progressBar = (ProgressBar) mainView.findViewById(R.id.teletekstProgress);
		pagerIndicator = (UnderlinePageIndicator) mainView.findViewById(R.id.teletekstIndicator);
		swipeHint = (TextView) mainView.findViewById(R.id.swipehint);

		ttPager = (ViewPager) mainView.findViewById(R.id.teletekstviewpager);
		ttPager.setOffscreenPageLimit(0);

		prefs = PreferenceManager.getDefaultSharedPreferences(act);

		pagerIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (position > 0) {
					hideHint();
					prefs.edit().putBoolean("showHint", false).commit();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		if (!prefs.getBoolean("showHint", true))
			hideHint();

		new TeletekstRetriever().execute();

	}

	public void hideHint() {
		swipeHint.setVisibility(View.GONE);
	}

	private class TeletekstRetriever extends AsyncTask<Integer, Void, String[]> {

		private final static String URL_BASE = "http://www.rtvoost.nl/teletekst/teletekst.asp?page=465&rotor=";

		private int page;

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String[] doInBackground(Integer... v) {
			if (v.length == 1)
				page = v[0];

			String url = URL_BASE + 1;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			try {
				HttpResponse response = client.execute(get);
				if (maxIndex == 0)
					setMaxIndex(EntityUtils.toString(response.getEntity()));

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			imageUrls = new String[maxIndex];

			for (int i = 1; i <= maxIndex; i++) {
				String html = "";
				url = URL_BASE + i;
				Log.d(TAG, url);
				get = new HttpGet(url);
				try {
					HttpResponse response = client.execute(get);
					html = EntityUtils.toString(response.getEntity());

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					html = html.split("<img width=\"440\" height=\"345\" src=\"")[1].split("\" usemap=\"#page\"")[0];
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}

				String imgUrl = "http://www.rtvoost.nl/teletekst/" + html.replace(" ", "%20");
				imageUrls[i - 1] = imgUrl;

			}

			return imageUrls;
		}

		@Override
		public void onPostExecute(String[] bmps) {
			progressBar.setVisibility(View.GONE);
			pagerAdapter = new TeletekstAdapter(bmps);
			try {
				ttPager.setAdapter(pagerAdapter);
				pagerIndicator.setViewPager(ttPager);

				if (page < maxIndex)
					ttPager.setCurrentItem(page);
			} catch (Exception e) {
			}
		}

		private void setMaxIndex(String html) {

			html = html.split("\"><img src=\"images/lastrotor.jpg\"")[0];
			String[] array = html.split("=");
			maxIndex = Integer.parseInt(array[array.length - 1]);
		}
	}

	public class TeletekstAdapter extends PagerAdapter {

		private String[] imgUrls;

		public TeletekstAdapter(String[] imgUrls) {
			this.imgUrls = imgUrls;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewPager vp = (ViewPager) container;
			ImageView view = (ImageView) LayoutInflater.from(activity).inflate(R.layout.teletekst_item, null);
			UrlImageViewHelper.setUrlDrawable(view, imgUrls[position]);
			vp.addView(view);
			return view;
		}

		@Override
		public int getCount() {
			return maxIndex;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			outState.putInt("teletekstPage", ttPager.getCurrentItem());
	}

	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			new TeletekstRetriever().execute(savedInstanceState.getInt("teletekstPage"));
	}
}

package com.markbuikema.juliana32.sections;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.simonvt.menudrawer.MenuDrawer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.tools.Tools;
import com.viewpagerindicator.UnderlinePageIndicator;

public class Teletekst {

	public static final String TAG = "Teletekst";

	private MainActivity activity;
	private ProgressBar progressBar;
	private FragmentManager fm;

	private ViewPager ttPager;
	private SectionsPagerAdapter pagerAdapter;
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
		pagerIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (position == ViewPager.SCROLL_STATE_SETTLING) hideHint();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		fm = ((FragmentActivity) activity).getSupportFragmentManager();

		if (act.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			if (!Tools.shouldShowTeletekstHint(act)) hideHint();

		new RetrieveTeletekst().execute();

	}

	public void hideHint() {
		swipeHint.setVisibility(View.GONE);
	}

	private class RetrieveTeletekst extends AsyncTask<Integer, Void, Bitmap[]> {

		private final static String URL_BASE = "http://www.rtvoost.nl/teletekst/teletekst.asp?page=465&rotor=";

		private int page;

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Bitmap[] doInBackground(Integer... v) {
			if (v.length == 1) page = v[0];

			String url = URL_BASE + 1;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			try {
				HttpResponse response = client.execute(get);
				if (maxIndex == 0) setMaxIndex(EntityUtils.toString(response.getEntity()));

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Bitmap[] images = new Bitmap[maxIndex];
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
				html = html.split("<img width=\"440\" height=\"345\" src=\"")[1].split("\" usemap=\"#page\"")[0];

				String imgUrl = "http://www.rtvoost.nl/teletekst/" + html.replace(" ", "%20");
				imageUrls[i - 1] = imgUrl;
				URL image;
				try {
					image = new URL(imgUrl);

					URLConnection conn = image.openConnection();
					conn.connect();
					images[i - 1] = BitmapFactory.decodeStream(new BufferedInputStream(conn.getInputStream()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return images;
		}

		@Override
		public void onPostExecute(Bitmap[] bmps) {
			progressBar.setVisibility(View.GONE);
			Tools.putTeletekst(bmps);
			pagerAdapter = new SectionsPagerAdapter(fm);
			try {
				ttPager.setAdapter(pagerAdapter);
				pagerIndicator.setViewPager(ttPager);

				if (page < maxIndex) ttPager.setCurrentItem(page);
			} catch (Exception e) {
			}
		}

		private void setMaxIndex(String html) {

			html = html.split("\"><img src=\"images/lastrotor.jpg\"")[0];
			String[] array = html.split("=");
			maxIndex = Integer.parseInt(array[array.length - 1]);
		}
	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt("index", i);
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public int getCount() {
			return maxIndex;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {

		Bitmap bmp = null;

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			bmp = Tools.getTeletekst(args.getInt("index"));
			ImageView view = (ImageView) inflater.inflate(R.layout.teletekst_item, null);
			view.setImageBitmap(bmp);
			return view;
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			outState.putInt("teletekstPage", ttPager.getCurrentItem());
	}

	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			new RetrieveTeletekst().execute(savedInstanceState.getInt("teletekstPage"));
		}
	}
}

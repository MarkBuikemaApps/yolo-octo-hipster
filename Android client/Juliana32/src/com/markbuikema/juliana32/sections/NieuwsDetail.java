package com.markbuikema.juliana32.sections;

import net.simonvt.menudrawer.MenuDrawer;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.tools.PictureRetriever;
import com.viewpagerindicator.UnderlinePageIndicator;

public class NieuwsDetail {

	private static final String TAG = "NieuwsDetail";

	private static final String NEW_LINE = "NEWLINEREFERENCE1337";

	private static final String IMAGE = "IMAGEREFERENCE1337";

	private MainActivity activity;
	private NieuwsItem item;

	private TextView title;
	private TextView subTitle;
	private TextView date;

	private TextView content;

	private ViewPager photoPager;
	private UnderlinePageIndicator photoIndicator;
	
	private boolean teaser;

	public NieuwsDetail(final MainActivity act, NieuwsItem item) {
		this.activity = act;
		this.item = item;

		teaser = item instanceof TeaserNieuwsItem;

		View mainView = act.findViewById(R.id.nieuwsDetailView);

		title = (TextView) mainView.findViewById(R.id.nieuwsDetailTitle);
		subTitle = (TextView) mainView.findViewById(R.id.nieuwsDetailSubtitle);
		content = (TextView) mainView.findViewById(R.id.nieuwsContent);
		date = (TextView) mainView.findViewById(R.id.nieuwsDetailDate);
		photoPager = (ViewPager) mainView.findViewById(R.id.newsPhotoPager);
		photoIndicator = (UnderlinePageIndicator) mainView.findViewById(R.id.newsPhotoIndicator);

		photoPager.setVisibility(item.getPhotoCount() < 1 ? View.GONE : View.VISIBLE);
		photoIndicator.setVisibility(item.getPhotoCount() < 1 ? View.GONE : View.VISIBLE);

		title.setText(item.getTitle());
		subTitle.setText(Html.fromHtml("<i>" + item.getSubTitle() + "</i>"));
		String contentString = item.getContent();
		contentString = contentString.replaceAll(NEW_LINE, "\n");
		contentString = contentString.replaceAll(Character.toString((char) 65532), "");
		
		content.setText(contentString);
		
		if (item instanceof NormalNieuwsItem) {
			date.setText(((NormalNieuwsItem) item).getCreatedAtString());
		}
		photoPager.setAdapter(new PhotoPagerAdapter(act.getSupportFragmentManager()));
		photoIndicator.setViewPager(photoPager);

	}

	public String getTitle() {
		return item.getTitle();
	}

	public String getDetailUrl() {
		return item.getDetailUrl();
	}

	public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

		public PhotoPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new PhotoSectionFragment();
			Bundle args = new Bundle();
			args.putString("url", item.getPhoto(i));
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return item.getPhotoCount();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}
	}

	public static class PhotoSectionFragment extends Fragment {

		public PhotoSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			View mainView = inflater.inflate(R.layout.photo_item, null);
			final ImageView view = (ImageView) mainView.findViewById(R.id.photoView);
			final String url = args.getString("url");

			Log.d(TAG, url);
			new PictureRetriever() {
				protected void onPostExecute(Bitmap result) {
					if (result == null) return;
					view.setImageBitmap(result);
					Log.d(TAG, "imagebitmap set, bitmap == null: " + (result == null));
				};
			}.execute(url);

			return mainView;
		}

		
	}

}

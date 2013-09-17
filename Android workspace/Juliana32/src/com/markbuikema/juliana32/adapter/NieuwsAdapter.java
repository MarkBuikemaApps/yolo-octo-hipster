package com.markbuikema.juliana32.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.ui.RecyclingBitmapDrawable;
import com.markbuikema.juliana32.ui.RecyclingImageView;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.DateTimeUtils;
import com.markbuikema.juliana32.util.Tools;

public class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

	private Context context;
	private FragmentManager fm;

	private static final int VIEW_TYPE_NORMAL = 0;
	private static final int VIEW_TYPE_TEASER = 1;
	private static final int VIEW_TYPE_FACEBOOK_PHOTO = 2;
	private static final int VIEW_TYPE_FACEBOOK = 3;

	public NieuwsAdapter(Context context) {
		super(context, 0);
		this.context = context;
		fm = ((FragmentActivity) context).getSupportFragmentManager();
	}

	@Override
	public NieuwsItem getItem(int position) {
		return DataManager.getInstance().getNieuwsItems().get(position);
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getItemViewType(int position) {
		NieuwsItem item = getItem(position);
		if (item instanceof NormalNieuwsItem)
			return VIEW_TYPE_NORMAL;
		else
			if (item instanceof TeaserNieuwsItem)
				return VIEW_TYPE_TEASER;
			else
				return ((FacebookNieuwsItem) item).isPhoto() ? VIEW_TYPE_FACEBOOK_PHOTO : VIEW_TYPE_FACEBOOK;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NieuwsItem item = getItem(position);

		switch (getItemViewType(position)) {
		case 0:
			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_nieuwsitem, null);

			TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
			TextView subTitleView = (TextView) convertView.findViewById(R.id.subTitleView);
			titleView.setText(item.getTitle());
			subTitleView.setText(item.getSubTitle());
			TextView createdAtView = (TextView) convertView.findViewById(R.id.createdAtView);
			createdAtView.setText(Tools.getDateString(((NormalNieuwsItem) item).getCreatedAt()));

			convertView.requestLayout();
			return convertView;
		case 1:
			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_teaseritem, null);

			TextView teaserTitleView = (TextView) convertView.findViewById(R.id.teaserTitle);
			ImageView teaserImageView = (ImageView) convertView.findViewById(R.id.teaserImage);
			teaserTitleView.setText(item.getTitle());
			Bitmap bmp = ((TeaserNieuwsItem) item).getImage();
			if (bmp != null)
				teaserImageView.setImageBitmap(bmp);

			convertView.requestLayout();
			return convertView;
		case 2:
			FacebookNieuwsItem fbPhotoItem = (FacebookNieuwsItem) item;

			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem_photo, null);

			ViewPager pager = (ViewPager) convertView.findViewById(R.id.facebookPhotoPager);
			TextView dummy = (TextView) convertView.findViewById(R.id.dummyTextView);
			dummy.setText(fbPhotoItem.getTitle());
			pager.setAdapter(new PhotoPagerAdapter(fm, fbPhotoItem));
			pager.setPageMargin(-40);
			pager.setHorizontalFadingEdgeEnabled(true);
			pager.setFadingEdgeLength(30);
			pager.setCurrentItem(0, true);
			pager.setOffscreenPageLimit(5);

			return convertView;
		case 3:
			FacebookNieuwsItem fbItem = (FacebookNieuwsItem) item;

			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem, null);
			TextView content = (TextView) convertView.findViewById(R.id.facebook_content);
			TextView likeCount = (TextView) convertView.findViewById(R.id.facebook_likecount);
			TextView commentCount = (TextView) convertView.findViewById(R.id.facebook_commentcount);
			TextView createdAt = (TextView) convertView.findViewById(R.id.facebook_date);

			if (fbItem.isPhoto())
				content.setText(fbItem.getTitle());
			else
				content.setText(fbItem.getContent());
			likeCount.setText(Integer.toString(fbItem.getLikeCount()));
			commentCount.setText(Integer.toString(fbItem.getCommentCount()));
			createdAt.setText(Tools.getDateString(fbItem.getCreatedAt()));

			convertView.requestLayout();
			return convertView;
		default:
			return null;
		}

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

		private static final String PHOTO_URL_PREFIX = "http://graph.facebook.com/";
		private static final String PHOTO_URL_SUFFIX = "/picture";
		private FacebookNieuwsItem item;

		public PhotoPagerAdapter(FragmentManager fm, FacebookNieuwsItem item) {
			super(fm);
			this.item = item;

		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment;
			if (i == 0) {
				fragment = new NormalSectionFragment();
				Bundle args = new Bundle();
				args.putString("title", item.getTitle());
				args.putString("createdAt",
						DateTimeUtils.getInstance(getContext()).getTimeDiffString(item.getCreatedAt().getTimeInMillis()));
				args.putInt("commentCount", item.getCommentCount());
				args.putInt("likeCount", item.getLikeCount());
				args.putString("fbId", item.getFbId());
				fragment.setArguments(args);
			} else {
				fragment = new PhotoSectionFragment();
				Bundle args = new Bundle();
				args.putString("picture_url", PHOTO_URL_PREFIX + item.getPhoto(i - 1) + PHOTO_URL_SUFFIX);
				fragment.setArguments(args);
			}
			return fragment;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}

		@Override
		public int getCount() {
			return item.getPhotoCount() + 1;
		}

	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class PhotoSectionFragment extends Fragment {

		public PhotoSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			String pictureUrl = args.getString("picture_url");
			View view = inflater.inflate(R.layout.fb_photo_item, null);
			final RecyclingImageView image = (RecyclingImageView) view.findViewById(R.id.facebookPhoto);
			new AsyncTask<String, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(String... arg0) {
					try {
						return Tools.getPictureFromUrl(arg0[0]);
					} catch (IndexOutOfBoundsException e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					image.setImageDrawable(new RecyclingBitmapDrawable(getResources(), result));
				}
			}.execute(pictureUrl);
			Log.d("FBPAGER", "done creating: " + pictureUrl);
			return view;
		}
	}

	public static class NormalSectionFragment extends Fragment {

		public NormalSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			View view = inflater.inflate(R.layout.listitem_facebookitem_viewpager, null);
			TextView content = (TextView) view.findViewById(R.id.facebook_content);
			TextView likeCount = (TextView) view.findViewById(R.id.facebook_likecount);
			TextView commentCount = (TextView) view.findViewById(R.id.facebook_commentcount);
			TextView createdAt = (TextView) view.findViewById(R.id.facebook_date);

			content.setText(args.getString("title"));
			likeCount.setText(Integer.toString(args.getInt("likeCount")));
			commentCount.setText(Integer.toString(args.getInt("commentCount")));
			createdAt.setText(args.getString("createdAt"));

			Log.d("FBPAGER", "done creating text item for " + args.getString("title"));

			view.setTag(args.getString("fbId"));

			return view;
		}
	}
}
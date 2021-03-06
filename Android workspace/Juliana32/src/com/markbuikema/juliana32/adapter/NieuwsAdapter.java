package com.markbuikema.juliana32.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ViewPager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.model.WebsiteNieuwsItem;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.Util;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

	private List<NieuwsItem> items;
	private int columnCount;
	private boolean scrollingDown = true;
	private boolean anim;

	private static final int VIEW_TYPE_HEADER = 0;
	private static final int VIEW_TYPE_NORMAL = 1;
	private static final int VIEW_TYPE_FACEBOOK_PHOTO = 2;
	private static final int VIEW_TYPE_FACEBOOK = 3;

	public NieuwsAdapter(Context context) {
		super(context, 0);
		columnCount = context.getResources().getInteger(R.integer.columnCount);
		items = new ArrayList<NieuwsItem>(DataManager.getInstance()
				.getNieuwsItems());
	}

	public void setScrollDirection(boolean down) {
		scrollingDown = down;
	}

	@Override
	public NieuwsItem getItem(int position) {
		return position < columnCount ? null : items
				.get(position - columnCount);
	}

	@Override
	public int getCount() {
		return items.size() + columnCount;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * DO NOT USE. DOES NOT WORK.
	 * 
	 * @deprecated use update() after adding all items to
	 *             DataManager.getInstance().getNieuwsItems();
	 */
	@Deprecated
	@Override
	public void add(NieuwsItem object) {
	}

	public void update() {
		items = new ArrayList<NieuwsItem>(DataManager.getInstance()
				.getNieuwsItems());
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if (position < columnCount)
			return VIEW_TYPE_HEADER;
		NieuwsItem item = getItem(position);
		if (item instanceof WebsiteNieuwsItem)
			return VIEW_TYPE_NORMAL;
		else
			return ((FacebookNieuwsItem) item).isPhoto() ? VIEW_TYPE_FACEBOOK_PHOTO
					: VIEW_TYPE_FACEBOOK;
	}

	public int getColumnCount() {
		return columnCount;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// if top row, return space
		if (position < columnCount) {
			View view = new View(getContext());
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					getContext().getResources().getDimensionPixelSize(
							R.dimen.nieuws_header_margin)));
			return view;
		}

		// else construct the appropriate view
		final NieuwsItem item = getItem(position);

		switch (getItemViewType(position)) {
		case VIEW_TYPE_NORMAL:
			convertView = constructWebsiteView(getContext(), item, convertView);

			item.startLoading(new OnContentLoadedListener() {

				@Override
				public void onContentLoaded(String content, List<String> photos) {
				}
			});

			break;
		case VIEW_TYPE_FACEBOOK_PHOTO:
			FacebookNieuwsItem fbPhotoItem = (FacebookNieuwsItem) item;

			convertView = constructFacebookPhotoView(getContext(), fbPhotoItem,
					convertView);

			break;
		case VIEW_TYPE_FACEBOOK:
			FacebookNieuwsItem fbItem = (FacebookNieuwsItem) item;

			convertView = constructFacebookView(getContext(), fbItem,
					convertView);

			break;
		default:
			break;
		}

		final View animatedView = convertView;

		if (anim) {
			ViewHelper.setScaleX(animatedView, .9f);
			ViewHelper.setScaleY(animatedView, .9f);
			ViewPropertyAnimator.animate(animatedView).setDuration(250)
					.scaleX(1).scaleY(1).start();
		}

		animatedView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					animatedView.setBackgroundColor(getContext().getResources()
							.getColor(R.color.touched));
					break;
				case MotionEvent.ACTION_UP:
					if (arg0 == animatedView)
						((MainActivity) getContext()).requestNieuwsDetailPage(
								item, animatedView);
				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_CANCEL:
					animatedView.setBackgroundColor(getContext().getResources()
							.getColor(R.color.white));
					break;
				}
				
				return true;
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			animatedView.setOnHoverListener(new OnHoverListener() {

				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					switch (arg1.getAction()) {
					case MotionEvent.ACTION_HOVER_ENTER:
						animatedView.setBackgroundColor(getContext()
								.getResources().getColor(R.color.focused));
						break;
					case MotionEvent.ACTION_HOVER_EXIT:
						animatedView.setBackgroundColor(getContext()
								.getResources().getColor(R.color.white));
						break;
					}
					return true;
				}
			});
		return convertView;

	}

	@Override
	public long getItemId(int position) {
		return position < columnCount ? 0 : position - columnCount;
	}

	public void setSearchword(String s) {
		items.clear();
		s = s.toLowerCase(Locale.US);
		for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
			if ((item.getTitle() != null && item.getTitle()
					.toLowerCase(Locale.US).contains(s))
					|| (item.getSubTitle() != null && item.getSubTitle()
							.toLowerCase(Locale.US).contains(s))
					|| (item.getContent() != null && item.getContent()
							.toLowerCase(Locale.US).contains(s)))
				items.add(item);

		notifyDataSetChanged();
	}

	public void clearSearchword() {
		items = new ArrayList<NieuwsItem>(DataManager.getInstance()
				.getNieuwsItems());
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		Log.d("nieuwsadapter", "notifyDataSetChanged() called");
		super.notifyDataSetChanged();
	}

	public static View constructWebsiteView(Context context, NieuwsItem item,
			View convertView) {
		if (convertView == null)
			convertView = LayoutInflater.from(context).inflate(
					R.layout.listitem_website_nieuws_item, null);

		TextView titleView = (TextView) convertView
				.findViewById(R.id.nieuwsitem_title);
		TextView subTitleView = (TextView) convertView
				.findViewById(R.id.nieuwsitem_subtitle);
		TextView createdAtView = (TextView) convertView
				.findViewById(R.id.nieuwsitem_date);

		titleView.setText(item.getTitle());
		subTitleView.setText(item.getSubTitle());
		createdAtView.setText(Util.getDateString(context,
				((WebsiteNieuwsItem) item).getCreatedAt()));

		titleView.setTypeface(Util.getRobotoCondensed(context));
		subTitleView.setTypeface(Util.getRobotoSlabLight(context));
		createdAtView.setTypeface(Util.getRobotoCondensed(context));

		Log.d("constructview", "text:" + subTitleView.getText().toString());

		return convertView;
	}

	public static View constructFacebookView(Context context,
			FacebookNieuwsItem fbItem, View convertView) {
		if (convertView == null)
			convertView = LayoutInflater.from(context).inflate(
					R.layout.listitem_facebook_nieuws_item, null);
		TextView title = (TextView) convertView
				.findViewById(R.id.facebook_content);
		TextView content = (TextView) convertView
				.findViewById(R.id.nieuwsitem_subtitle);
		TextView likeCount = (TextView) convertView
				.findViewById(R.id.facebook_likecount);
		TextView commentCount = (TextView) convertView
				.findViewById(R.id.facebook_commentcount);
		TextView createdAt = (TextView) convertView
				.findViewById(R.id.facebook_date);

		title.setText(fbItem.getTitle());
		content.setText(fbItem.getContent());
		likeCount.setText(Integer.toString(fbItem.getLikeCount()));
		commentCount.setText(Integer.toString(fbItem.getComments().size()));
		createdAt.setText(Util.getDateString(context, fbItem.getCreatedAt()));

		title.setTypeface(Util.getRobotoCondensed(context));
		createdAt.setTypeface(Util.getRobotoCondensed(context));
		content.setTypeface(Util.getRobotoSlabLight(context));
		likeCount.setTypeface(Util.getRobotoLight(context));
		commentCount.setTypeface(Util.getRobotoLight(context));

		return convertView;
	}

	public static View constructFacebookPhotoView(Context context,
			FacebookNieuwsItem fbPhotoItem, View convertView) {
		if (convertView == null)
			convertView = LayoutInflater.from(context).inflate(
					R.layout.listitem_facebook_nieuws_item_photo, null);
		final ViewPager pager = (ViewPager) convertView
				.findViewById(R.id.facebookPhotoPager);
		final ImageView singlePhotoView = (ImageView) convertView
				.findViewById(R.id.facebookSinglePhoto);

		boolean singlePhoto = fbPhotoItem.getAlbumId() == null
				&& fbPhotoItem.getDefaultPhoto() != null;

		singlePhotoView.setVisibility(singlePhoto ? View.VISIBLE : View.GONE);
		pager.setVisibility(singlePhoto ? View.GONE : View.VISIBLE);

		if (!singlePhoto) {
			final PhotoPagerAdapter adapter = new PhotoPagerAdapter(context,
					fbPhotoItem, new OnPhotoPagerDialogPageChangedListener() {

						@Override
						public void onPhotoPagerDialogPageChanged(int pageIndex) {
							pager.setCurrentItem(pageIndex);
						}

					});
			pager.setCurrentItem(0, true);

			pager.setOffscreenPageLimit(0);
			pager.setAdapter(adapter);

			if (fbPhotoItem.getPhotoCount() == 0)
				fbPhotoItem.startLoading(new OnContentLoadedListener() {

					@Override
					public void onContentLoaded(String content,
							List<String> photos) {
						adapter.notifyDataSetChanged();
					}
				});
		} else
			UrlImageViewHelper.setUrlDrawable(singlePhotoView,
					Util.PHOTO_URL_PREFIX + fbPhotoItem.getDefaultPhoto()
							+ Util.PHOTO_URL_SUFFIX, R.drawable.silhouette);
		TextView fbContent = (TextView) convertView
				.findViewById(R.id.facebook_content);
		TextView fbLikeCount = (TextView) convertView
				.findViewById(R.id.facebook_likecount);
		TextView fbCommentCount = (TextView) convertView
				.findViewById(R.id.facebook_commentcount);
		TextView fbCreatedAt = (TextView) convertView
				.findViewById(R.id.facebook_date);
		TextView fbSubTitle = (TextView) convertView
				.findViewById(R.id.nieuwsitem_subtitle);

		fbContent.setText(fbPhotoItem.getTitle());
		fbSubTitle.setText(fbPhotoItem.getContent());
		fbLikeCount.setText(Integer.toString(fbPhotoItem.getLikeCount()));
		fbCommentCount.setText(Integer.toString(fbPhotoItem.getComments()
				.size()));
		fbCreatedAt.setText(Util.getDateString(context,
				fbPhotoItem.getCreatedAt()));

		fbCreatedAt.setTypeface(Util.getRobotoCondensed(context));
		fbContent.setTypeface(Util.getRobotoCondensed(context));
		fbLikeCount.setTypeface(Util.getRobotoLight(context));
		fbCommentCount.setTypeface(Util.getRobotoLight(context));
		fbSubTitle.setTypeface(Util.getRobotoSlabLight(context));

		return convertView;
	}

	public void setAnimationsEnabled(boolean b) {
		anim = b;
	}

}
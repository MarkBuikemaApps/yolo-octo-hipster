package com.markbuikema.juliana32.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ViewPager;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.Util;

public class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

	private Context context;
	private List<NieuwsItem> items;

	private static final int VIEW_TYPE_NORMAL = 0;
	private static final int VIEW_TYPE_FACEBOOK_PHOTO = 1;
	private static final int VIEW_TYPE_FACEBOOK = 2;

	public NieuwsAdapter(Context context) {
		super(context, 0);
		this.context = context;
		items = new ArrayList<NieuwsItem>(DataManager.getInstance().getNieuwsItems());
	}

	@Override
	public NieuwsItem getItem(int position) {
		return items.get(position);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public int getViewTypeCount() {
		return 3;
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
		items = new ArrayList<NieuwsItem>(DataManager.getInstance().getNieuwsItems());
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		NieuwsItem item = getItem(position);
		if (item instanceof NormalNieuwsItem)
			return VIEW_TYPE_NORMAL;
		else
			return ((FacebookNieuwsItem) item).isPhoto() ? VIEW_TYPE_FACEBOOK_PHOTO : VIEW_TYPE_FACEBOOK;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final NieuwsItem item = getItem(position);

		switch (getItemViewType(position)) {
		case VIEW_TYPE_NORMAL:
			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_nieuwsitem, null);

			TextView titleView = (TextView) convertView.findViewById(R.id.nieuwsitem_content);
			TextView subTitleView = (TextView) convertView.findViewById(R.id.nieuwsitem_subtitle);
			titleView.setText(item.getTitle());
			subTitleView.setText(item.getSubTitle());
			TextView createdAtView = (TextView) convertView.findViewById(R.id.nieuwsitem_date);
			createdAtView.setText(Util.getDateString(getContext(), ((NormalNieuwsItem) item).getCreatedAt()));
			titleView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf"));

			((NormalNieuwsItem) item).startLoading(new OnContentLoadedListener() {

				@Override
				public void onContentLoaded(String content, List<String> photos) {
				}
			});

			break;
		case VIEW_TYPE_FACEBOOK_PHOTO:
			FacebookNieuwsItem fbPhotoItem = (FacebookNieuwsItem) item;

			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem_photo, null);
			final ViewPager pager = (ViewPager) convertView.findViewById(R.id.facebookPhotoPager);
			PhotoPagerAdapter adapter = new PhotoPagerAdapter(getContext(), fbPhotoItem,
					new OnPhotoPagerDialogPageChangedListener() {

						@Override
						public void onPhotoPagerDialogPageChanged(int pageIndex) {
							pager.setCurrentItem(pageIndex + 1);
						}

					});
			pager.setPageMargin((int) Util.pxToDp(-20));
			pager.setCurrentItem(0, true);

			pager.setOffscreenPageLimit(0);
			pager.setAdapter(adapter);

			break;
		case VIEW_TYPE_FACEBOOK:
			FacebookNieuwsItem fbItem = (FacebookNieuwsItem) item;

			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem, null);
			TextView content = (TextView) convertView.findViewById(R.id.facebook_content);
			TextView likeCount = (TextView) convertView.findViewById(R.id.facebook_likecount);
			TextView commentCount = (TextView) convertView.findViewById(R.id.facebook_commentcount);
			TextView createdAt = (TextView) convertView.findViewById(R.id.facebook_date);

			content.setText(fbItem.getContent());
			likeCount.setText(Integer.toString(fbItem.getLikeCount()));
			commentCount.setText(Integer.toString(fbItem.getCommentCount()));
			createdAt.setText(Util.getDateString(getContext(), fbItem.getCreatedAt()));

			break;
		default:
			break;
		}

		// final ViewPropertyAnimator animator =
		// ViewPropertyAnimator.animate(convertView);
		// animator.setDuration(0).translationY(64).setListener(new
		// AnimatorListener() {
		//
		// @Override
		// public void onAnimationStart(Animator animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animator animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// animator.setDuration(250).translationY(0).setListener(null).start();
		// }
		//
		// @Override
		// public void onAnimationCancel(Animator animation) {
		// }
		// }).start();
		return convertView;

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setSearchword(String s) {
		items.clear();
		s = s.toLowerCase(Locale.US);
		for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
			if ((item.getTitle() != null && item.getTitle().toLowerCase(Locale.US).contains(s))
					|| (item.getSubTitle() != null && item.getSubTitle().toLowerCase(Locale.US).contains(s))
					|| (item.getContent() != null && item.getContent().toLowerCase(Locale.US).contains(s)))
				items.add(item);

		notifyDataSetChanged();
	}

	public void clearSearchword() {
		items = new ArrayList<NieuwsItem>(DataManager.getInstance().getNieuwsItems());
		notifyDataSetChanged();
	}

}
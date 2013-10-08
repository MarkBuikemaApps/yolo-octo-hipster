package com.markbuikema.juliana32.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
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
import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.Util;

public class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

	private Context context;

	private static final int VIEW_TYPE_NORMAL = 0;
	private static final int VIEW_TYPE_TEASER = 1;
	private static final int VIEW_TYPE_FACEBOOK_PHOTO = 2;
	private static final int VIEW_TYPE_FACEBOOK = 3;

	public NieuwsAdapter(Context context) {
		super(context, 0);
		this.context = context;
	}

	@Override
	public NieuwsItem getItem(int position) {
		return DataManager.getInstance().getNieuwsItems().get(position);
	}

	@Override
	public int getCount() {
		return DataManager.getInstance().getNieuwsItems().size();
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

			((NormalNieuwsItem) item).startLoading(new OnContentLoadedListener() {

				@Override
				public void onContentLoaded(String content, List<String> photos) {
				}
			});

			return convertView;
		case VIEW_TYPE_TEASER:
			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_teaseritem, null);

			TextView teaserTitleView = (TextView) convertView.findViewById(R.id.teaserTitle);
			ImageView teaserImageView = (ImageView) convertView.findViewById(R.id.teaserImage);
			teaserTitleView.setText(item.getTitle());
			Bitmap bmp = ((TeaserNieuwsItem) item).getImage();
			if (bmp != null)
				teaserImageView.setImageBitmap(bmp);

			return convertView;
		case VIEW_TYPE_FACEBOOK_PHOTO:
			FacebookNieuwsItem fbPhotoItem = (FacebookNieuwsItem) item;

			if (convertView == null)
				convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem_photo, null);
			ViewPager pager = (ViewPager) convertView.findViewById(R.id.facebookPhotoPager);
			PhotoPagerAdapter adapter = new PhotoPagerAdapter(getContext(), fbPhotoItem);
			pager.setPageMargin((int) Util.pxToDp(-20));
			pager.setCurrentItem(0, true);

			pager.setOffscreenPageLimit(2);
			pager.setAdapter(adapter);

			return convertView;
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

			return convertView;
		default:
			return convertView;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
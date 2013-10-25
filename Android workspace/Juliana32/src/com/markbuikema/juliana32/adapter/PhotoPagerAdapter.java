package com.markbuikema.juliana32.adapter;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.util.DateTimeUtils;
import com.markbuikema.juliana32.util.Util;

public class PhotoPagerAdapter extends PagerAdapter implements Observer {

	private FacebookNieuwsItem item;

	public PhotoPagerAdapter(Context context, FacebookNieuwsItem item) {
		super();
		item.addObserver(this);
		this.item = item;
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		if (position == 0) {
			final View view = LayoutInflater.from(container.getContext()).inflate(R.layout.listitem_facebookitem_viewpager, null);
			TextView content = (TextView) view.findViewById(R.id.facebook_content);
			TextView likeCount = (TextView) view.findViewById(R.id.facebook_likecount);
			TextView commentCount = (TextView) view.findViewById(R.id.facebook_commentcount);
			TextView createdAt = (TextView) view.findViewById(R.id.facebook_date);
			TextView subTitle = (TextView) view.findViewById(R.id.facebook_subtitle);

			content.setText(item.getTitle());
			subTitle.setText(item.getContent());
			likeCount.setText(Integer.toString(item.getLikeCount()));
			commentCount.setText(Integer.toString(item.getCommentCount()));
			createdAt.setText(DateTimeUtils.getInstance(container.getContext()).getTimeDiffString(
					item.getCreatedAt().getTimeInMillis()));

			view.setClickable(true);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((MainActivity) view.getContext()).requestNieuwsDetailPage(item);
				}
			});

			((ViewPager) container).addView(view);

			return view;
		} else {

			View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fb_photo_item, null);
			final ImageView image = (ImageView) view.findViewById(R.id.facebookPhoto);

			UrlImageViewHelper.setUrlDrawable(image, Util.PHOTO_URL_PREFIX + item.getPhoto(position - 1) + Util.PHOTO_URL_SUFFIX);

			view.setClickable(true);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					MainActivity act = (MainActivity) container.getContext();
					act.showPhotoDialog(item.getPhotos(), position - 1, null);
				}
			});

			((ViewPager) container).addView(view);

			return view;
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return item.getPhotoCount() + 1;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		notifyDataSetChanged();
	}

}
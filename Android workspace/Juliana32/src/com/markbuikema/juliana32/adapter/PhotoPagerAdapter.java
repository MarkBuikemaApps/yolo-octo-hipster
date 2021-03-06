package com.markbuikema.juliana32.adapter;

import java.util.Observable;
import java.util.Observer;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ViewPager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.util.Util;

public class PhotoPagerAdapter extends PagerAdapter implements Observer {

	private FacebookNieuwsItem item;
	private OnPhotoPagerDialogPageChangedListener pagerCallback;

	public PhotoPagerAdapter(Context context, FacebookNieuwsItem item, OnPhotoPagerDialogPageChangedListener callback) {
		super();
		item.addObserver(this);
		pagerCallback = callback;
		this.item = item;
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {

		View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fb_photo_item, null);
		final ImageView image = (ImageView) view.findViewById(R.id.facebookPhoto);

		UrlImageViewHelper.setUrlDrawable(image, Util.PHOTO_URL_PREFIX + item.getPhoto(position) + Util.PHOTO_URL_SUFFIX);

		view.setClickable(true);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity act = (MainActivity) container.getContext();
				act.showPhotoDialog(image, item.getPhotos(), position, pagerCallback);
			}
		});

		((ViewPager) container).addView(view);

		return view;

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return item.getPhotoCount();
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
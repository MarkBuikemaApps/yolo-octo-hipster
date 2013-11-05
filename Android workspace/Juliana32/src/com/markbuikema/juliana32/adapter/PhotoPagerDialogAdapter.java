package com.markbuikema.juliana32.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.util.Util;

public class PhotoPagerDialogAdapter extends PagerAdapter {

	private String[] urls;
	private Context context;

	public PhotoPagerDialogAdapter(Context context, String[] urls) {
		super();
		this.urls = urls;
		this.context = context;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		View view = LayoutInflater.from(context).inflate(R.layout.photo_item_dialog, null);
		final ImageView image = (ImageView) view.findViewById(R.id.facebookPhoto);
		final String url;
		if (!urls[position].startsWith("http"))
			url = Util.PHOTO_URL_PREFIX + urls[position] + Util.PHOTO_URL_SUFFIX;
		else
			url = urls[position];

		UrlImageViewHelper.setUrlDrawable(image, url);

		((ViewPager) container).addView(view);

		return view;

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return urls.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
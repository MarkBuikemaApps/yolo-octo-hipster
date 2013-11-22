package com.markbuikema.juliana32.adapter;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ViewPager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.util.Util;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewPropertyAnimator;

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

		UrlImageViewHelper.setUrlDrawable(image, url, new UrlImageViewCallback() {

			@Override
			public void onLoaded(final ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
				ViewPropertyAnimator.animate(imageView).alpha(0f).setDuration(1).setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						ViewPropertyAnimator.animate(imageView).alpha(1f).setDuration(1000);
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				});

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
		return urls.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
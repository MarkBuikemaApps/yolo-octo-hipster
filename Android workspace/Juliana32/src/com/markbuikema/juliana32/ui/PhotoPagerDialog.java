package com.markbuikema.juliana32.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.PhotoPagerDialogAdapter;

public class PhotoPagerDialog {

	private Context context;
	private ViewPager photoPager;
	private String[] urls;

	public PhotoPagerDialog(Context context, String[] urls, final OnPhotoPagerDialogPageChangedListener callback) {
		this.context = context;
		this.urls = urls;

		photoPager = (ViewPager) ((MainActivity) context).findViewById(R.id.photoDialogPager);

		if (callback != null)
			photoPager.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					callback.onPhotoPagerDialogPageChanged(arg0);
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});

		PhotoPagerDialogAdapter ppda = new PhotoPagerDialogAdapter(context, urls);
		photoPager.setAdapter(ppda);

	}

	public void show(int position) {
		((MainActivity) context).setDrawersEnabled(false);
		photoPager.setCurrentItem(position, true);
		photoPager.setVisibility(View.VISIBLE);
	}

	public void destroy() {
		((MainActivity) context).setDrawersEnabled(true);
		photoPager.setVisibility(View.GONE);
		photoPager.setAdapter(null);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putStringArray("photoDialogUrls", urls);
		outState.putInt("photoDialogPage", photoPager.getCurrentItem());
	}

	public interface OnPhotoPagerDialogPageChangedListener {
		public void onPhotoPagerDialogPageChanged(int pageIndex);
	}

}

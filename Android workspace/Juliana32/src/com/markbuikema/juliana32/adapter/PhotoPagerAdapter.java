package com.markbuikema.juliana32.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.asynctask.PictureChanger;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.util.DateTimeUtils;
import com.markbuikema.juliana32.util.Util;

public class PhotoPagerAdapter extends PagerAdapter {

	private FacebookNieuwsItem item;
	private Context context;

	public PhotoPagerAdapter(Context context, FacebookNieuwsItem item) {
		super();
		this.item = item;
		this.context = context;
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		if (position == 0) {
			View view = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem_viewpager, null);
			TextView content = (TextView) view.findViewById(R.id.facebook_content);
			TextView likeCount = (TextView) view.findViewById(R.id.facebook_likecount);
			TextView commentCount = (TextView) view.findViewById(R.id.facebook_commentcount);
			TextView createdAt = (TextView) view.findViewById(R.id.facebook_date);
			TextView subTitle = (TextView) view.findViewById(R.id.facebook_subtitle);

			content.setText(item.getTitle());
			subTitle.setText(item.getContent());
			likeCount.setText(Integer.toString(item.getLikeCount()));
			commentCount.setText(Integer.toString(item.getCommentCount()));
			createdAt.setText(DateTimeUtils.getInstance(context).getTimeDiffString(item.getCreatedAt().getTimeInMillis()));

			view.setClickable(true);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((MainActivity) context).requestNieuwsDetailPage(item);
				}
			});

			((ViewPager) container).addView(view);

			return view;
		} else {

			View view = LayoutInflater.from(context).inflate(R.layout.fb_photo_item, null);
			final ImageView image = (ImageView) view.findViewById(R.id.facebookPhoto);

			new PictureChanger() {
				@Override
				protected void onPostExecute(Bitmap result) {

					Log.d("resized", "picture loaded: " + result);
					image.setImageBitmap(result);
				};
			}.execute(Util.PHOTO_URL_PREFIX + item.getPhoto(position - 1) + Util.PHOTO_URL_SUFFIX);

			view.setClickable(true);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					MainActivity act = (MainActivity) context;
					act.showPhotoDialog(item.getPhotos(), null, position - 1);
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

}
package com.markbuikema.juliana32.adapter;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ViewPager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;

public class WeekPagerAdapter extends PagerAdapter {

	private Context context;

	public WeekPagerAdapter(Context context) {
		this.context = context;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ListView weekList = (ListView) LayoutInflater.from(context).inflate(R.layout.agenda_week, null);

		((ViewPager) container).addView(weekList);
		return weekList;
	}

	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

}

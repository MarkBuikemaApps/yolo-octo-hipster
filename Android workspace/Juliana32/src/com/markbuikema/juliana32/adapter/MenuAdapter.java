package com.markbuikema.juliana32.adapter;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity.Page;

public class MenuAdapter extends ArrayAdapter<String> {

	private Typeface font;
	private Page page;

	public MenuAdapter(Context context) {
		super(context, 0, context.getResources().getStringArray(R.array.menu_items));
		font = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
	}

	public void setPage(Page page) {
		this.page = page;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_menu, null);

		TextView text = (TextView) convertView.findViewById(R.id.menuItemTitle);
		View indicator = convertView.findViewById(R.id.menuItemIndicator);
		text.setText(getItem(position));
		indicator.setBackgroundResource(page == Page.values()[position] ? R.drawable.listitem_arrow
				: R.drawable.listitem_background);

		text.setTypeface(font);
		return convertView;
	}

}
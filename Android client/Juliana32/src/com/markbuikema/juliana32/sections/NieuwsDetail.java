package com.markbuikema.juliana32.sections;

import android.view.View;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;

public class NieuwsDetail {

	MainActivity act;
	NieuwsItem item;
	
	TextView title;
	TextView subTitle;
	TextView content;
	TextView date;

	boolean teaser;

	public NieuwsDetail(MainActivity act, NieuwsItem item) {
		this.act = act;
		this.item = item;
		
		teaser = item instanceof TeaserNieuwsItem;
		
		View mainView = act.findViewById(R.id.nieuwsDetailView);
		
		title = (TextView) mainView.findViewById(R.id.nieuwsDetailTitle);
		subTitle = (TextView) mainView.findViewById(R.id.nieuwsDetailSubtitle);
		content = (TextView) mainView.findViewById(R.id.nieuwsDetailContent);
		date = (TextView) mainView.findViewById(R.id.nieuwsDetailDate);
		
		title.setText(item.getTitle());
		subTitle.setText(item.getSubTitle());
		content.setText(item.getContent());
		
		if (item instanceof NormalNieuwsItem) {
			date.setText(((NormalNieuwsItem) item).getCreatedAtString());
		}
	}
	
	public String getTitle() {
		return item.getTitle();
	}
	
	public String getDetailUrl() {
		return item.getDetailUrl();
	}
}

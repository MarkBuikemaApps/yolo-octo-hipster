package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

public class NormalNieuwsItem extends NieuwsItem {
	
	private GregorianCalendar createdAt;

	public NormalNieuwsItem(int id, String title, String subTitle, String content, long createdAt, String detailUrl) {
		
		super(id, android.text.Html.fromHtml(title).toString(), android.text.Html.fromHtml(subTitle).toString(), android.text.Html.fromHtml(content).toString(), detailUrl);
		this.createdAt = new GregorianCalendar();
		this.createdAt.setTimeInMillis(createdAt);
	}
	
	public String getCreatedAtString() {
		String string = createdAt.get(GregorianCalendar.DAY_OF_MONTH) + "/"
				+ (createdAt.get(GregorianCalendar.MONTH)+1) + "/"
				+ createdAt.get(GregorianCalendar.YEAR);
		return string;
	}
	
	public long getCreatedAt() {
		return createdAt.getTimeInMillis();
	}
}

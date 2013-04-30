package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

public class NormalNieuwsItem extends NieuwsItem implements Comparable<NormalNieuwsItem>{
	
	private GregorianCalendar createdAt;

	public NormalNieuwsItem(int id, String title, String subTitle, String content, long createdAt, String detailUrl) {
		
		super(id, android.text.Html.fromHtml(title).toString(), android.text.Html.fromHtml(subTitle).toString(), android.text.Html.fromHtml(content).toString(), detailUrl);
		this.createdAt = new GregorianCalendar();
		this.createdAt.setTimeInMillis(createdAt);
	}
	
	public String getCreatedAtString() {
		String string = createdAt.get(GregorianCalendar.DAY_OF_MONTH) + "/"
				+ (createdAt.get(GregorianCalendar.MONTH)+1) + "/"
				+ Integer.toString(createdAt.get(GregorianCalendar.YEAR)).substring(2);
		return string;
	}
	
	public long getCreatedAt() {
		return createdAt.getTimeInMillis();
	}

	@Override
	public int compareTo(NormalNieuwsItem another) {
		if (!(another instanceof NormalNieuwsItem)) return 0;
		NormalNieuwsItem item = (NormalNieuwsItem) another;
		return createdAt.getTimeInMillis()>item.createdAt.getTimeInMillis()?1:-1;
	}
}

package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

public class NormalNieuwsItem extends NieuwsItem{
	
	private GregorianCalendar createdAt;
	private String detailUrl;

	public NormalNieuwsItem(int id, String title, String subTitle, String content, long createdAt, String detailUrl) {
		
		super(id, title, subTitle, content);
		this.createdAt = new GregorianCalendar();
		this.createdAt.setTimeInMillis(createdAt);
		this.detailUrl = detailUrl;
	}
	
	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}

	
	
	public String getDetailUrl() {
		return detailUrl;
	}
}
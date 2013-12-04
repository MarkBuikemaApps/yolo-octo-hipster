package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

import com.markbuikema.juliana32.util.FacebookHelper;

public class Comment extends Like {

	private String text;
	private GregorianCalendar createdAt;
	private String userId;

	public Comment(String id, String name, String userId, String text, String dateString) {
		super(id, name);

		this.text = text;
		this.userId = userId;

		createdAt = FacebookHelper.toDate(dateString);
		// Log.d("comment_date", createdAt.toString());
	}

	public String getText() {
		return text;
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}

	public String getUserId() {
		return userId;
	}

	public String getImgUrl() {
		return "http://graph.facebook.com/" + userId + "/picture?type=square";
	}
}

package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

import com.markbuikema.juliana32.util.FacebookHelper;

public class Comment extends Like {

	private String text;
	private GregorianCalendar createdAt;
	private String id;

	public Comment(String id, String name, String userId, String text, String dateString) {
		super(userId, name);

		this.text = text;
		this.id = id;

		createdAt = FacebookHelper.toDate(dateString);
		// Log.d("comment_date", createdAt.toString());
	}

	public String getText() {
		return text;
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}

	public String getId() {
		return id;
	}

}

package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.util.Log;

public class Comment extends Like {

	private String text;
	private String imgUrl;
	private GregorianCalendar createdAt;
	private String userId;

	public Comment(String id, String name, String imgUrl, String userId, String text, String dateString) {
		super(id, name);

		this.text = text;
		this.imgUrl = imgUrl;
		this.userId = userId;

		String yearString = dateString.substring(0, 4);
		String monthString = dateString.substring(5, 7);
		String dayString = dateString.substring(8, 10);
		String hourString = dateString.substring(11, 13);
		String minuteString = dateString.substring(14, 16);
		String secondString = dateString.substring(17, 19);
		createdAt = new GregorianCalendar();
		createdAt.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
		createdAt.set(Integer.parseInt(yearString), Integer.parseInt(monthString) - 1, Integer.parseInt(dayString),
				Integer.parseInt(hourString) + 2, Integer.parseInt(minuteString), Integer.parseInt(secondString));
		Log.d("comment_date", createdAt.toString());
	}

	public String getText() {
		return text;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}

	public String getUserId() {
		return userId;
	}
}

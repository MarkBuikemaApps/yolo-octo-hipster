package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

import android.graphics.Bitmap;

public class Comment extends Like {

	private String text;
	private GregorianCalendar createdAt;

	public Comment(String id, String name, Bitmap image, String userId, String text, String dateString) {
		super(id, name, image, userId);

		this.text = text;

		String yearString = dateString.substring(0, 4);
		String monthString = dateString.substring(5, 7);
		String dayString = dateString.substring(8, 10);
		createdAt = new GregorianCalendar(Integer.parseInt(yearString), Integer.parseInt(monthString),
				Integer.parseInt(dayString));
	}

	public String getText() {
		return text;
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}
}
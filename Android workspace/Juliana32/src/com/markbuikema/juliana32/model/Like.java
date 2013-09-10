package com.markbuikema.juliana32.model;

import android.graphics.Bitmap;

public class Like {

	private String id;
	private String name;
	private Bitmap image;
	private String userId;

	public Like(String id, String name, Bitmap image, String userId) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public Bitmap getImage() {
		return image;
	}

	public String getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

}
package com.markbuikema.juliana32.model;

public class Like {

	private String name;
	private String userId;

	public Like(String userId, String name) {
		this.userId = userId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getImgUrl() {
		return "http://graph.facebook.com/" + userId + "/picture?type=square";
	}

	public String getUserId() {
		return userId;
	}
}

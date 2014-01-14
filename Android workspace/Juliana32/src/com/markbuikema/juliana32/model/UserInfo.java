package com.markbuikema.juliana32.model;

public class UserInfo {

	private String userName;
	private String id;

	public UserInfo( String userName, String id ) {
		this.userName = userName;
		this.id = id;
	}

	public String getImgUrl() {
		return "http://graph.facebook.com/" + id + "/picture?type=square";
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return userName;
	}
}

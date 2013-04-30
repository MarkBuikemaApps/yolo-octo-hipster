package com.markbuikema.juliana32.model;


public class Photo {

	private int id;
	private String url;
	
	public Photo(int id, String url) {
		this.id = id;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}
	
	
}

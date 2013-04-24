package com.markbuikema.juliana32.model;

import com.markbuikema.juliana32.tools.Tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;

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

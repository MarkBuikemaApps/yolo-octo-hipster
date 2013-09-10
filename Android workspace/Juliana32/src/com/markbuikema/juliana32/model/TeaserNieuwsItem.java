package com.markbuikema.juliana32.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TeaserNieuwsItem extends NieuwsItem {
	private String imgUrl;
	private String detailUrl;
	private Bitmap image;

	public TeaserNieuwsItem(int id, String title, String subTitle, String content, String imgUrl, String detailUrl) {

		super(id, title, subTitle, content);

		this.imgUrl = imgUrl;
		this.detailUrl = detailUrl;
		
		URL image;
		try {
			image = new URL(imgUrl.replaceAll("&amp;", "&"));

			URLConnection conn = image.openConnection();
			conn.connect();
			this.image = BitmapFactory.decodeStream(new BufferedInputStream(conn.getInputStream()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Bitmap getImage() {
		return image;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "ID: " + id + "\nTITLE: " + title + "\nSUBTITLE: " + subTitle + "\nCONTENT: " + (content==null?"NULL":"NOT NULL") + "\nIMGURL: " + imgUrl + "\nDETAILURL: " + detailUrl+"\n---\n";
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public String getDetailUrl() {
		return detailUrl;
	}
	

}
package com.markbuikema.juliana32.model;

import java.util.ArrayList;

public abstract class NieuwsItem implements Comparable<NieuwsItem> {

	protected int id;
	protected String title;
	protected String subTitle;
	protected String content;
	protected ArrayList<String> photos;

	public NieuwsItem(int id, String title, String subTitle, String content) {
		this.id = id;
		this.title = title;
		this.subTitle = subTitle;
		this.content = content;

		photos = new ArrayList<String>();
	}

	public void addPhoto(String url) {
		photos.add(url);
	}

	public int getPhotoCount() {
		return photos.size();
	}

	public String getPhoto(int index) {
		return photos.get(index);
	}

	public boolean isFromFacebook() {
		return this instanceof FacebookNieuwsItem && id == -1;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return id + ": " + title + "," + subTitle + "," + content + ".";
	}

	@Override
	public int compareTo(NieuwsItem another) {
		if (another instanceof TeaserNieuwsItem)
			return 0;
		long thisDate;
		long otherDate;

		if (another instanceof NormalNieuwsItem)
			otherDate = ((NormalNieuwsItem) another).getCreatedAt().getTimeInMillis();
		else
			otherDate = ((FacebookNieuwsItem) another).getCreatedAt().getTimeInMillis();

		if (this instanceof NormalNieuwsItem)
			thisDate = ((NormalNieuwsItem) this).getCreatedAt().getTimeInMillis();
		else
			thisDate = ((FacebookNieuwsItem) this).getCreatedAt().getTimeInMillis();

		return thisDate <= otherDate ? 1 : -1;
	}

}
package com.markbuikema.juliana32.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;

public abstract class NieuwsItem extends Observable implements Comparable<NieuwsItem> {

	private static int nextId = 0;

	protected int id;
	protected String title;
	protected String subTitle;
	protected List<String> photos;
	protected String content;
	protected GregorianCalendar createdAt;

	public NieuwsItem(String title, String subTitle, String content, GregorianCalendar createdAt) {
		id = nextId++;
		this.title = title;
		this.subTitle = subTitle;
		this.content = content;
		this.createdAt = createdAt;

		photos = new ArrayList<String>();
	}

	public void addPhoto(String url) {
		photos.add(url);
		setChanged();
		notifyObservers();
	}

	public int getPhotoCount() {
		return photos.size();
	}

	public String getPhoto(int index) {
		return photos.get(index);
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
	}

	public String getContent() {
		return content;
	}

	public boolean isFromFacebook() {
		return this instanceof FacebookNieuwsItem;
	}

	public boolean isTeaser() {
		return this instanceof TeaserNieuwsItem && createdAt == null;
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

	@Override
	public String toString() {
		return id + ": " + title + "," + subTitle + ".";
	}

	@Override
	public int compareTo(NieuwsItem another) {
		if (another instanceof TeaserNieuwsItem)
			return 0;
		long thisDate;
		long otherDate;

		otherDate = another.getCreatedAt().getTimeInMillis();

		thisDate = createdAt.getTimeInMillis();

		return thisDate <= otherDate ? 1 : -1;
	}

	public String[] getPhotos() {

		return photos.toArray(new String[photos.size()]);
	}

}

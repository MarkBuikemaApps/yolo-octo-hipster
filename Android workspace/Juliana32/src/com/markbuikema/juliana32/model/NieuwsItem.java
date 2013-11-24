package com.markbuikema.juliana32.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;

public abstract class NieuwsItem extends Observable implements Comparable<NieuwsItem> {

	protected String title;
	protected String subTitle;
	protected List<String> photos;
	protected String content;
	protected GregorianCalendar createdAt;

	public NieuwsItem(String title, String subTitle, String content, GregorianCalendar createdAt) {
		this.title = title == null ? null : Jsoup.clean(title, Whitelist.none()).replace("&eacute;", "é");
		this.subTitle = subTitle == null ? null : Jsoup.clean(subTitle, Whitelist.none()).replace("&eacute;", "é");
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

	public boolean isPhoto() {
		return photos.size() > 0;
	}

	public boolean isFromFacebook() {
		return this instanceof FacebookNieuwsItem;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	@Override
	public int compareTo(NieuwsItem another) {
		long thisDate;
		long otherDate;

		otherDate = another.getCreatedAt().getTimeInMillis();

		thisDate = createdAt.getTimeInMillis();

		return thisDate <= otherDate ? 1 : -1;
	}

	public String[] getPhotos() {

		return photos.toArray(new String[photos.size()]);
	}

	public abstract String getId();

	public abstract void startLoading(final OnContentLoadedListener callback);

	public abstract boolean isContentLoaded();
}

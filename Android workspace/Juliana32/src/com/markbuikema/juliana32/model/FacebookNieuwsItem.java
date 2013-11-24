package com.markbuikema.juliana32.model;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.util.FacebookHelper.PhotoGetter;

public class FacebookNieuwsItem extends NieuwsItem {

	private String fbId;
	private String link;
	private int likeCount;// FIXME
	private int commentCount;
	private String imgUrl;
	private String albumId;
	private List<Like> likes;
	private boolean liked;
	private String defaultPhotoId;

	public FacebookNieuwsItem(String fbId, String title, String content, GregorianCalendar createdAt, String link,
			String imgUrl, List<Like> likes, int commentCount, String albumId, String defaultPhotoId) {
		super(title, null, content, createdAt);

		this.albumId = albumId;
		this.fbId = fbId;
		this.likes = likes;
		this.link = link;
		this.imgUrl = imgUrl;
		likeCount = likes.size();
		this.commentCount = commentCount;
		this.defaultPhotoId = defaultPhotoId;
	}

	public String getFbId() {
		return fbId;
	}

	public String getLink() {
		return link;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	@Override
	public boolean isPhoto() {
		return albumId != null;
	}

	public String getAlbumId() {
		return albumId;
	}

	public List<Like> getLikes() {
		return Collections.unmodifiableList(likes);
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public boolean isLiked() {
		return liked;
	}

	public void comment() {
		commentCount++;
	}

	@Override
	public String getId() {
		return fbId;
	}

	@Override
	public void addPhoto(String url) {
		if (url.equals(defaultPhotoId))
			photos.add(0, url);
		else
			photos.add(url);

		setChanged();
		notifyObservers();
	}

	@Override
	public void startLoading(final OnContentLoadedListener callback) {
		new PhotoGetter() {
			@Override
			protected void onPostExecute(List<String> result) {
				photos.clear();
				for (String photo : result)
					addPhoto(photo);
				callback.onContentLoaded(null, result);
			}
		}.execute(this);
	}

	@Override
	public boolean isContentLoaded() {
		if (isPhoto())
			return getPhotoCount() > 0;
		else
			return true;
	}

}
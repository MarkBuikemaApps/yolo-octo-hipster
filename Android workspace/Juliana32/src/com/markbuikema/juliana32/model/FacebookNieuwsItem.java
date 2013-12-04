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
	private String imgUrl;
	private String albumId;
	private List<Like> likes;
	private boolean liked;
	private String defaultPhotoId;
	private List<Comment> comments;

	public FacebookNieuwsItem(String fbId, String title, String content, GregorianCalendar createdAt, String link,
			String imgUrl, List<Like> likes, List<Comment> comments, String albumId, String defaultPhotoId) {
		super(title, null, content, createdAt);

		this.albumId = albumId;
		this.fbId = fbId;
		this.likes = likes;
		this.comments = comments;
		this.link = link;
		this.imgUrl = imgUrl;
		likeCount = likes.size();
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
			protected void onPostExecute(final List<String> result) {
				photos.clear();
				for (String photo : result)
					addPhoto(photo);
				callback.onContentLoaded(null, result);

				// new CommentLoader() {
				//
				// @Override
				// protected void onPreExecute() {
				// comments.clear();
				// }
				//
				// @Override
				// protected void onProgressUpdate(Comment... values) {
				// for (int i = 0; i < values.length; i++)
				// comments.add(values[i]);
				// }
				//
				// @Override
				// protected void onPostExecute(Void v) {
				// commentCount = comments.size();
				// }
				// }.execute(fbId);
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

	public List<Comment> getComments() {
		return Collections.unmodifiableList(comments);
	}

}
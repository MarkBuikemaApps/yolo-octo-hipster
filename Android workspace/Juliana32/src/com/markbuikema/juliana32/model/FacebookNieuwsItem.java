package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

public class FacebookNieuwsItem extends NieuwsItem {

	private String fbId;
	private String link;
	private int likeCount;
	private int commentCount;
	private GregorianCalendar createdAt;
	private String imgUrl;

	public FacebookNieuwsItem(String id, String content, String dateString, String link, String imgUrl, int likeCount,
			int commentCount) {
		super(-1, "Facebook", null, content);

		fbId = id;
		String yearString = dateString.substring(0, 4);
		String monthString = dateString.substring(5, 7);
		String dayString = dateString.substring(8, 10);
		createdAt = new GregorianCalendar(Integer.parseInt(yearString), Integer.parseInt(monthString) - 1,
				Integer.parseInt(dayString));
		this.link = link;
		this.imgUrl = imgUrl;
		this.likeCount = likeCount;
		this.commentCount = commentCount;

	}

	public String getFbId() {
		return fbId;
	}

	public String getLink() {
		return link;
	}

	public GregorianCalendar getCreatedAt() {
		return createdAt;
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
}
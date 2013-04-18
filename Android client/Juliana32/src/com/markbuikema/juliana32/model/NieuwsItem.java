package com.markbuikema.juliana32.model;


public abstract class NieuwsItem {
	private int id;
	private String title;
	private String subTitle;
	private String content;
	private String detailUrl;

	public NieuwsItem(int id, String title, String subTitle, String content, String detailUrl) {
		this.id = id;
		this.title = title;
		this.subTitle = subTitle;
		this.content = content;
		this.detailUrl = detailUrl;

		
		

	}
	
	

	public boolean isFromFacebook() {
		try {
			Integer.parseInt(detailUrl);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
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

	

	public String detailUrl() {
		return detailUrl;
	}
	
	@Override
	public String toString() {
		return id+ ": " + title + "," + subTitle + "," + content + "," + detailUrl;
	}

	

}
package com.markbuikema.juliana32.server.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.markbuikema.juliana32.server.singletons.NewsItems;
import com.markbuikema.juliana32.server.tools.Tools;

@XmlRootElement
public class TeaserNewsItem {

	@XmlElement
	private int id;
	@XmlElement
	private String title;
	@XmlElement
	private String subTitle;
	@XmlElement
	private String content;
	@XmlElement
	private String imgUrl;
	@XmlElement
	private String detailUrl;

	public TeaserNewsItem() {
		id = NewsItems.getLatestUnusedTeaserId();
	}

	public TeaserNewsItem(String title, String subTitle, String imgUrl,
			String detailUrl) {
		this.title = title;
		this.subTitle = subTitle;
		this.imgUrl = imgUrl;
		this.detailUrl = detailUrl;

		this.content = Tools.getContent(detailUrl);
		this.id = NewsItems.getLatestUnusedTeaserId();

		// System.out.println(toString());

		try {
			content = content.split("<div id='content-text'>")[1];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			content = content.split("<div id='terug'>")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			content = content.split("<div id='crumbtail'>")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		content = Jsoup.parse(content).text();
		content = Jsoup.clean(content, Whitelist.basic());
		content = content.replaceAll("&nbsp;", " ");
		content = content.replaceAll("&acirc;??", "'");
		content = content.replaceAll(";??","");
	}

	@Override
	public String toString() {
		return "ID: " + id + "\nTITLE: " + title + "\nSUBTITLE: " + subTitle
				+ "\nCONTENT: " + content + "\nIMGURL: " + imgUrl + "\nDETAILURL: " + detailUrl
				+ "\n---\n";
	}

	public void replace(TeaserNewsItem i) {
		id = i.id;
		title = i.title;
		subTitle = i.subTitle;
		content = i.content;
		detailUrl = i.detailUrl;
		imgUrl = i.imgUrl;
	}

	public int getId() {
		return id;
	}
	
	public String getDetailUrl() {
		return detailUrl;
	}
}
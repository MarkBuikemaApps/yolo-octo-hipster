package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.markbuikema.juliana32.server.singletons.NewsItems;
import com.markbuikema.juliana32.server.tools.Tools;

@XmlRootElement
public class NewsItem implements Comparable<NewsItem> {
	@XmlElement
	private int id;
	@XmlElement
	private String title;
	@XmlElement
	private String subTitle;
	@XmlElement
	private String content;
	@XmlElement
	private long createdAt;
	@XmlElement
	private String detailUrl;
	@XmlElement
	private ArrayList<String> photos;

	public NewsItem() {
		id = NewsItems.getLatestUnusedId();
		photos = new ArrayList<>();
	}

	public NewsItem(String title, String subTitle, String detailUrl, long createdAt) {
		id = NewsItems.getLatestUnusedId();
		this.title = title;
		this.subTitle = subTitle;
		this.detailUrl = detailUrl;
		this.createdAt = createdAt;
		this.content = Tools.getContent(detailUrl);

		photos = new ArrayList<>();

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

		content = content.replaceAll("<br>", "\n");
		content = content.replaceAll("<BR>", "\n");
		
		
//		content = Jsoup.parse(content).text();
//		content = Jsoup.clean(content, Whitelist.basic());
		content = content.replaceAll("&nbsp;", " ");
		content = content.replaceAll("&acirc;??", "'");
		content = content.replaceAll(";??", "");

	}

	public String getDetailUrl() {
		return detailUrl;
	}

	@Override
	public String toString() {
		return id + ": " + title + "," + subTitle + "," + content + "," + createdAt + "," + detailUrl;
	}

	public void replace(NewsItem i) {
		id = i.id;
		title = i.title;
		subTitle = i.subTitle;
		content = i.content;
		createdAt = i.createdAt;
		detailUrl = i.detailUrl;
	}

	public int getId() {
		return id;
	}

	@Override
	public int compareTo(NewsItem item) {
		int difference = (int) (createdAt - item.createdAt);

		return difference == 0 ? 0 : (difference / difference);

	}

}

package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.markbuikema.juliana32.server.singletons.NewsItems;
import com.markbuikema.juliana32.server.tools.Tools;

@XmlRootElement
public class TeaserNewsItem {

	private static final String NEW_LINE = "NEWLINEREFERENCE1337";
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
	@XmlElement
	private ArrayList<String> photos;

	public TeaserNewsItem() {
		id = NewsItems.getLatestUnusedTeaserId();
		photos = new ArrayList<>();

	}

	public TeaserNewsItem(String title, String subTitle, String imgUrl, String detailUrl) {
		this.title = title;
		this.subTitle = subTitle;
		this.imgUrl = imgUrl;
		this.detailUrl = detailUrl;

		this.content = Tools.getContent(detailUrl);
		this.id = NewsItems.getLatestUnusedTeaserId();
		photos = new ArrayList<>();

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
		content = Jsoup.clean(content, Whitelist.basicWithImages().addTags("a").addAttributes("a", "href"));

		content = content.replaceAll("<br>", "<br/>");
		content = content.replaceAll("<BR>", "<br/>");
		content = content.replaceAll("<br/>", NEW_LINE);
		content = content.replaceAll("<br />", NEW_LINE);
		content = content.replaceAll("<p>", NEW_LINE);
		content = content.replaceAll("</p>", NEW_LINE);
		content = content.replaceAll("\n", "");
		content = content.replaceAll("&nbsp;", " ");

		while (content.startsWith(NEW_LINE)) {
			content = content.replaceFirst(NEW_LINE, "");
		}

		content = content.replaceAll(NEW_LINE + NEW_LINE + NEW_LINE, NEW_LINE + NEW_LINE);
		content = content.trim();

		generatePhotosFromHtml(content);
		content = Jsoup.clean(content, Whitelist.basic().addTags("a").addAttributes("a", "href"));

		System.out.println(id + ": " + content);
	}

	private void generatePhotosFromHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements elements = doc.getElementsByTag("img");
		for (int i = 0; i < elements.size(); i++) {
			photos.add(elements.get(i).absUrl("src"));
		}
		System.out.println("NewsItem " + id + " has " + photos.size() + " photos: ");
		for (String photo : photos) {
			System.out.println(photo);
		}
	}

	@Override
	public String toString() {
		return "ID: " + id + "\nTITLE: " + title + "\nSUBTITLE: " + subTitle + "\nCONTENT: " + content + "\nIMGURL: "
				+ imgUrl + "\nDETAILURL: " + detailUrl + "\n---\n";
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

package com.markbuikema.juliana32.server.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.markbuikema.juliana32.server.model.NewsItem;
import com.markbuikema.juliana32.server.model.TeaserNewsItem;
import com.markbuikema.juliana32.server.singletons.NewsItems;
import com.markbuikema.juliana32.server.tools.Tools;

public class NewsCrawler extends Thread {

	public final static int MINUTES = 1; //Interval in minutes
	
	public final static long DELAY = MINUTES*60*1000; 
	public final static String HOME_URL = "http://www.svjuliana32.nl";
	public final static String NEWS_URL = "http://www.svjuliana32.nl/nieuws/";

	public enum Month {
		JANUARI, FEBRUARI, MAART, APRIL, MEI, JUNI, JULI, AUGUSTUS, SEPTEMBER, OKTOBER, NOVEMBER, DECEMBER
	}
	
	@Override
	public void run() {

		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {

				String homeHtml = Tools.getContent(HOME_URL);
				String newsHtml = Tools.getContent(NEWS_URL);
				
				if (homeHtml != null)
				processTeaserContent(homeHtml);

				if (newsHtml != null)
				processNewsContent(newsHtml);
				
				
				
			}
		}, 100, DELAY);

	}
	
	private void processNewsContent(String content) {
		if (!content.isEmpty()) {
			System.out.println("Started retrieving news items...");
			long start = System.currentTimeMillis();
			
			String news = content.split("<div id='content-text' class='nieuws'>")[1].split("<div id='crumbtail'>")[0];
			String[] newsHTMLs = news.split("<div class=\"item-inhoud-home\">");
			ArrayList<NewsItem> items = new ArrayList<NewsItem>();
			for (int i = 0; i < newsHTMLs.length; i++) {
				newsHTMLs[i] = newsHTMLs[i].split("</div>")[0];
				NewsItem item = processNewsItem(newsHTMLs[i]);
				if (item != null) items.add(item);
			}
			NewsItems.get().pushNewsItems(items);
			
			long totalTime = System.currentTimeMillis() - start;
			System.out.println("Done retrieving news items and it took " + totalTime + " ms.");
		}
	}

	private void processTeaserContent(String content) {
		if (!content.isEmpty()) {
			System.out.println("Started retrieving teasers...");
			long start = System.currentTimeMillis();

			String teasers = content.split("div id='teasers'>")[1]
					.split("<div id='content-news-home'>")[0];

			String[] teaserHTMLs = teasers.split("<div class=\"teaser-pic\">");
			for (int i = 0; i < teaserHTMLs.length; i++) {
				teaserHTMLs[i] = teaserHTMLs[i].split("</div>")[0];
			}


			ArrayList<TeaserNewsItem> newItems = new ArrayList<TeaserNewsItem>();
			for (String teaser : teaserHTMLs) {
				TeaserNewsItem item = processTeaserNewsItem(teaser);
				if (item != null) newItems.add(item);
			}
			
			NewsItems.get().pushTeasers(newItems);
			
			long totalTime = System.currentTimeMillis() - start;
			System.out.println("Done retrieving teasers and it took " + totalTime + " ms.");

		}

	}

	public TeaserNewsItem processTeaserNewsItem(String html) {
		if (html.length()<10) {
			return null;
		}
		try {
			String imgUrl = html.split("img src='")[1].split("' alt=")[0].replace("&amp;", "&");
			String detailUrl = html.split("<a href=\"")[1].split("\" title=")[0];
			String subTitle = html.replaceAll("<P>", "<p>")
					.replaceAll("</P>", "</p>").split("<p>")[1].split("</p>")[0];
			String title = html.split("title='")[1].split("' class=\"title\"")[0];

			title = Jsoup.parse(title).text();
			subTitle = Jsoup.parse(subTitle).text();
			
			TeaserNewsItem item = new TeaserNewsItem(title, subTitle, imgUrl,
					detailUrl);

			return item;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}
	
	private NewsItem processNewsItem(String html) {
		if (html.length() < 30) return null;
		try {
			String title = html.split("title='")[1].split("' class=\"title\">")[0];
			String subTitle = html.split("<span class=\"sum\">")[1].split("</span>")[0];
			String date = html.split("<span class=\"datum\">Geplaatst op: ")[1].split("</span")[0];
			String detailUrl = html.split("<div class=\"lees-meer\"><a href=\"")[1].split("\" title=\"Lees meer\">")[0];
			
			int dateDay = Integer.parseInt(date.substring(0, 2));
			int dateYear = Integer.parseInt(date.substring(date.length()-4,date.length()));
			int dateMonth = 0;
			Month month = Month.valueOf(date.split(" ")[1].toUpperCase());
			switch(month) {
			case FEBRUARI: dateMonth = 1; break;
			case MAART: dateMonth = 2; break;
			case APRIL: dateMonth = 3; break;
			case MEI: dateMonth = 4; break;
			case JUNI: dateMonth = 5; break;
			case JULI: dateMonth = 6; break;
			case AUGUSTUS: dateMonth = 7; break;
			case SEPTEMBER: dateMonth = 8; break;
			case OKTOBER: dateMonth = 9; break;
			case NOVEMBER: dateMonth = 10; break;
			case DECEMBER: dateMonth = 11; break;
			default: dateMonth = 0; break;
			}
			
			GregorianCalendar cal = new GregorianCalendar(dateYear, dateMonth, dateDay);
			long createdAt = cal.getTimeInMillis();
			
			NewsItem item = new NewsItem(title, subTitle, detailUrl, createdAt);
			return item;
			
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return null;
	}

}

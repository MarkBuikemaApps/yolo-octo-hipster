package com.markbuikema.juliana32.server.singletons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.markbuikema.juliana32.server.crawler.NewsCrawler;
import com.markbuikema.juliana32.server.model.NewsItem;
import com.markbuikema.juliana32.server.model.TeaserNewsItem;

public class NewsItems {

	private static NewsItems instance;
	
	private NewsCrawler crawler;

	private static int latestUnusedId = 0;
	private static int latestUnusedTeasedId = 0;

	private ArrayList<NewsItem> newsItems;
	private ArrayList<TeaserNewsItem> teaserItems;

	private NewsItems() {
		newsItems = new ArrayList<NewsItem>();
		teaserItems = new ArrayList<TeaserNewsItem>();
	}

	public static NewsItems get() {
		if (instance == null) {
			instance = new NewsItems();
		}
		return instance;
	}

	public static int getLatestUnusedId() {
		return latestUnusedId++;
	}

	public NewsItem get(int id) {
		for (NewsItem item : newsItems) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}

	public TeaserNewsItem getTeaser(int id) {
		for (TeaserNewsItem item : teaserItems) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}

	public void add(NewsItem item) {
		newsItems.add(item);
	}

	public void addTeaser(TeaserNewsItem item) {
		teaserItems.add(item);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<NewsItem> getNewsItems() {
		return (ArrayList<NewsItem>) newsItems.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<TeaserNewsItem> getTeaserItems() {
		return (ArrayList<TeaserNewsItem>) teaserItems.clone();
	}

	public void replace(int id, NewsItem item) {
		get(id).replace(item);
	}

	public void replaceTeaser(int id, TeaserNewsItem item) {
		getTeaser(id).replace(item);
	}

	public void remove(int id) {
		newsItems.remove(listIndexOf(id));
	}

	public void removeTeaser(int id) {
		teaserItems.remove(listIndexOfTeaser(id));
	}

	private int listIndexOf(int id) {
		for (int i = 0; i < newsItems.size(); i++) {
			if (newsItems.get(i).getId() == id)
				return i;
		}
		return -1;
	}

	private int listIndexOfTeaser(int id) {
		for (int i = 0; i < teaserItems.size(); i++) {
			if (teaserItems.get(i).getId() == id)
				return i;
		}
		return -1;
	}

	public static int getLatestUnusedTeaserId() {
		return latestUnusedTeasedId++;
	}
	
	public void startCrawling() {
		if (crawler == null) {
			crawler = new NewsCrawler();
			crawler.start();
		}
	}
	
	public void pushTeasers(List<TeaserNewsItem> items) {
		ArrayList<TeaserNewsItem> newList = new ArrayList<TeaserNewsItem>();
		for (TeaserNewsItem pushedItem: items) {
			TeaserNewsItem oldReplacement = pushedItem;
			for (TeaserNewsItem oldItem: teaserItems) {
				if (pushedItem.getDetailUrl().equals(oldItem.getDetailUrl())) {
					oldReplacement = oldItem;
				}
			}
			newList.add(oldReplacement);
		}
		
		teaserItems.clear();
		teaserItems.addAll(newList);
		
	}
	
	public void pushNewsItems(List<NewsItem> items) {
		ArrayList<NewsItem> newList = new ArrayList<NewsItem>();
		for (NewsItem pushedItem: items) {
			NewsItem oldReplacement = pushedItem;
			for (NewsItem oldItem: newsItems) {
				if (pushedItem.getDetailUrl().equals(oldItem.getDetailUrl())) {
					oldReplacement = oldItem;
				}
			}
			newList.add(oldReplacement);
		}
		
		newsItems.clear();
		newsItems.addAll(newList);
		
		sortItems();
		
	}
	
	@SuppressWarnings("unchecked")
	public void sortItems() {
		Collections.sort(newsItems);
	}
}

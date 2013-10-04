package com.markbuikema.juliana32.util;

import java.util.List;

import android.util.Log;

import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;

public class DataManager {

	private static DataManager instance;
	private List<NieuwsItem> nieuwsItems;
	private List<TeaserNieuwsItem> teaserItems;
	private List<Season> teams;

	private DataManager() {
	}

	public static DataManager getInstance() {
		if (instance == null)
			instance = new DataManager();
		return instance;
	}

	public void clearData() {
		teams = null;
		nieuwsItems = null;
		teaserItems = null;
	}

	public List<NieuwsItem> getNieuwsItems() {
		return nieuwsItems;
	}

	public List<TeaserNieuwsItem> getTeaserItems() {
		return teaserItems;
	}

	public List<Season> getTeams() {
		return teams;
	}

	public void setNieuwsItems(List<NieuwsItem> nieuwsItems) {
		this.nieuwsItems = nieuwsItems;
	}

	public void setTeaserItems(List<TeaserNieuwsItem> teaserItems) {
		this.teaserItems = teaserItems;
	}

	public void setTeams(List<Season> teams) {
		this.teams = teams;
	}

	public boolean requiresData() {
		return (teams == null || teaserItems == null || nieuwsItems == null);
	}

	public void printLoadingStatus() {
		Log.d("DataManager", "Teams loaded: " + (teams != null));
		Log.d("DataManager", "Teasers loaded: " + (teaserItems != null));
		Log.d("DataManager", "Nieuws loaded: " + (nieuwsItems != null));
	}

}
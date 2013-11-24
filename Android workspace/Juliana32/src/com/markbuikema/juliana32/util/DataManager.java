package com.markbuikema.juliana32.util;

import java.util.ArrayList;
import java.util.List;

import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Team;

public class DataManager {

	private static DataManager instance;
	private List<NieuwsItem> nieuwsItems;
	private List<Team> teams;

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
	}

	public List<NieuwsItem> getNieuwsItems() {
		if (nieuwsItems == null)
			return new ArrayList<NieuwsItem>();
		else
			return nieuwsItems;
	}

	public List<Team> getTeams() {
		if (teams == null)
			return new ArrayList<Team>();
		else
			return teams;
	}

	public void setNieuwsItems(List<NieuwsItem> nieuwsItems) {
		this.nieuwsItems = nieuwsItems;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public boolean requiresData() {
		return (teams == null || nieuwsItems == null);
	}

	public void printLoadingStatus() {
		// Log.d("DataManager", "Teams loaded: " + (teams != null));
		// Log.d("DataManager", "Nieuws loaded: " + (nieuwsItems != null));
	}

	public NieuwsItem getNieuwsItemById(String itemRequestId) {
		NieuwsItem item = null;
		for (NieuwsItem i : DataManager.getInstance().getNieuwsItems())
			if (i.getId().equals(itemRequestId))
				return i;
		return null;
	}

}
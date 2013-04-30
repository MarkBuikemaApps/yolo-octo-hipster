package com.markbuikema.juliana32.server.singletons;

import java.util.ArrayList;

import com.markbuikema.juliana32.server.model.Season;

public class Seasons {

	private static int highestTeamId = 0;
	private static int highestGameId = 0;
	private static int highestPhotoId = 0;
	private static int highestTableId = 0;

	public static int getLatestUnusedTeamId() {
		return highestTeamId++;
	}

	public static int getLatestUnusedGameId() {
		return highestGameId++;
	}
	
	public static int getLatestUnusedPhotoId() {
		return highestPhotoId++;
	}
	
	public static int getLatestUnusedTableId() {
		return highestTableId++;
	}

	private static Seasons instance;

	private ArrayList<Season> seasons;

	private Seasons() {
		seasons = new ArrayList<Season>();
	}

	public static Seasons get() {
		if (instance == null)
			instance = new Seasons();
		return instance;
	}

	public boolean exists(int year) {
		for (Season season : seasons)
			if (season.getYear() == year)
				return true;

		return false;
	}

	public Season getSeason(int index) {
		if (index >= 0 && index < seasons.size())
			return seasons.get(index);
		else
			return null;
	}

	public ArrayList<Season> getAllSeasons() {
		return seasons;
	}

	public Season findSeason(int year) {
		for (Season s : seasons) {
			if (s.isSeason(year))
				return s;
		}
		return null;
	}

	public void add(Season s) {
		boolean exists = false;
		for (Season season : seasons) {
			if (season.isSeason(s))
				exists = true;
		}
		if (!exists)
			seasons.add(s);

	}

	
}
package com.markbuikema.juliana32.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

public class Team {

	public enum Category {
		SENIOREN, JUNIOREN, DAMES
	}

	private static final String TAG = "Team model";

	private int id;
	private String name;
	private Category category;
	private ArrayList<Table> tables;
	private ArrayList<Game> games;
	private ArrayList<Photo> photos;
	
	private static Comparator<Table> tableComparator = new Comparator<Table>() {

		@Override
		public int compare(Table lhs, Table rhs) {
			if (lhs.getId() < rhs.getId())
				return -1;
			else if (lhs.getId() > rhs.getId())
				return 1;
			else
				return 0;
		}

	};

	public Team(int id, String name, Category category) {
		this.id = id;
		this.name = name;
		this.category = category;
		games = new ArrayList<Game>();
		tables = new ArrayList<Table>();
		photos = new ArrayList<Photo>();
	}

	// public void addTableRow(TableRow row) {
	// if (row == null) return;
	// tables.add(row);
	// }

	public boolean isCategory(Category cat) {
		return category == cat;
	}

	public void addGame(Game game) {
		if (game == null) return;
		games.add(game);
	}

	public ArrayList<Game> getUitslagen() {
		ArrayList<Game> uitslagen = new ArrayList<Game>();
		for (Game game : games) {
			if (game.isPlayed()) uitslagen.add(game);
		}
		return uitslagen;
	}

	public ArrayList<Game> getProgramma() {
		ArrayList<Game> programma = new ArrayList<Game>();
		for (Game game : games) {
			if (!game.isPlayed()) programma.add(game);
		}
		return programma;
	}

	public int getGameCount() {
		return games.size();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Game> getGames() {
		return (ArrayList<Game>) games.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Table> getTables() {
		return (ArrayList<Table>) tables.clone();
	}

	public void addTable(Table table) {
		tables.add(table);
		Collections.sort(tables, tableComparator);
		
		Log.d(TAG, "table added");
	}

	public void addPhoto(Photo photo) {
		if (photo == null) return;

		Log.d(TAG, "Photo added to team " + name + ", url: " + photo.getUrl());
		photos.add(photo);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Photo> getPhotos() {
		return (ArrayList<Photo>) photos.clone();
	}

	public int getPhotoCount() {
		return photos.size();
	}

	public String getTableName(int position) {
		return tables.get(position).getName();
	}

}
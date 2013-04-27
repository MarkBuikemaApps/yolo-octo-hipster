package com.markbuikema.juliana32.model;

import java.util.ArrayList;

import android.util.Log;

public class Team {
	
	public enum Category {
		SENIOREN, JUNIOREN, DAMES
	}

	private static final String TAG = "Team model";
	
	private int id;
	private String name;
	private Category category;
	private ArrayList<TableRow> table;
	private ArrayList<Game> games;
	private ArrayList<Photo> photos;
	
	public Team(int id, String name, Category category) {
		this.id = id;
		this.name = name;
		this.category = category;
		games = new ArrayList<Game>();
		table = new ArrayList<TableRow>();
		photos = new ArrayList<Photo>();
	}
	
	public void addTableRow(TableRow row) {
		if (row == null) return;
		table.add(row);
	}
	
	public boolean isCategory(Category cat) {
		return category == cat;
	}
	
	public void addGame(Game game) {
		if (game == null) return;
		games.add(game);
	}
	
	public ArrayList<Game> getUitslagen() {
		ArrayList<Game> uitslagen = new ArrayList<Game>();
		for (Game game: games) {
			if (game.isPlayed()) uitslagen.add(game);
		}
		return uitslagen;
	}
	
	public ArrayList<Game> getProgramma() {
		ArrayList<Game> programma = new ArrayList<Game>();
		for (Game game: games) {
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
	
	
	public ArrayList<TableRow> getTable() {
		return table;
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
	
}

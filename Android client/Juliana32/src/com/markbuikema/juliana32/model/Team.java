package com.markbuikema.juliana32.model;

import java.util.ArrayList;

public class Team {
	
	public enum Category {
		SENIOREN, JUNIOREN, DAMES
	}
	
	private int id;
	private String name;
	private Category category;
	private ArrayList<Game> games;
	
	public Team(int id, String name, Category category) {
		this.id = id;
		this.name = name;
		this.category = category;
		games = new ArrayList<Game>();
	}
	
	public boolean isCategory(Category cat) {
		return category == cat;
	}
	
	public void addGame(Game game) {
		games.add(game);
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

	public ArrayList<Game> getGames() {
		return games;
	}
	
	
	
	
}

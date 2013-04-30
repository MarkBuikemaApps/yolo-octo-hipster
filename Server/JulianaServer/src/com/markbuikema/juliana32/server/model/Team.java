package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.markbuikema.juliana32.server.singletons.Seasons;

@XmlRootElement(name = "teams")
public class Team {

	public enum Category {
		DAMES, JUNIOREN, SENIOREN
	}

	@XmlElement
	private int id;

	@XmlElement
	private Category category;

	@XmlElement
	private String name;

	@XmlElement
	private ArrayList<Table> tables;

	@XmlElement
	private ArrayList<Game> games;

	@XmlElement
	private ArrayList<Photo> photos;

	public Team() {
		id = Seasons.getLatestUnusedTeamId();
		games = new ArrayList<Game>();
		photos = new ArrayList<Photo>();
		tables = new ArrayList<Table>();
	}

	public Game getGame(int index) {
		if (index >= 0 && index < games.size())
			return games.get(index);
		else
			return null;
	}

	public boolean isTeam(int id) {
		return this.id == id;
	}

	public void addGame(Game game) {
		games.add(game);
	}

	public String toString() {
		return id + ": " + category.toString() + ", " + name;
	}

	public String getName() {
		return name;
	}

	public Table getTeamTable(int id) {
		for (Table table : tables) {
			if (table.isTable(id)) {
				return table;
			}
		}
		return null;
	}

	public void addTable(Table table) {
		if (table == null) return;
		tables.add(table);
	}

	public void addPhoto(Photo photo) {
		photos.add(photo);
	}

	public int getTableCount() {
		return tables.size();
	}

}
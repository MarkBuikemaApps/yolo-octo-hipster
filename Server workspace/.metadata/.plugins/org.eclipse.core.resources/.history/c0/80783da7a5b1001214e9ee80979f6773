package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "seasons")
public class Season {

	@XmlElement
	private int year;

	public Season() {
		teams = new ArrayList<Team>();
	}

	public Season(int year) {
		teams = new ArrayList<Team>();
		this.year = year;
	}
	
	public int getYear() {
		return year;
	}

	public boolean isSeason(int year) {
		return this.year == year;
	}

	@XmlElement
	public String getName() {
		return Integer.toString(year).substring(2) + "/"
				+ Integer.toString(year + 1).substring(2);
	}

	@XmlElement
	private ArrayList<Team> teams;

	public Team findTeam(int teamId) {
		for (Team t : teams) {
			if (t.isTeam(teamId)) {
				return t;
			}
		}
		return null;
	}

	public boolean hasTeamWithName(String name) {
		boolean has = false;
		for (Team t : teams) {
			if (t.getName().equals(name))
				has = true;
		}
		return has;
	}

	public void addTeam(Team team) {
		teams.add(team);
	}

	public boolean isSeason(Season s) {
		return year == s.year;
	}

	public int getTeamCount() {
		return teams.size();
	}

}

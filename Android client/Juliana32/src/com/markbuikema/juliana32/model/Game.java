package com.markbuikema.juliana32.model;

import java.util.GregorianCalendar;

public class Game {

	private int id;

	private String teamName;
	private String otherTeam;

	private boolean home;
	private boolean played;
	private long date;

	private int teamGoals;
	private int otherGoals;

	public Game(int id, String teamName, String otherTeam, boolean home, long date,
			int teamGoals, int otherGoals) {
		this.id = id;
		this.teamName = teamName;
		this.otherTeam = otherTeam;
		this.home = home;
		this.date = date;

		if (teamGoals < 0) {
			otherGoals = -1;
			played = false;
		}
		if (otherGoals < 0) {
			teamGoals = -1;
			played = false;
		}
		played = (teamGoals >= 0 && otherGoals >= 0);
		
		this.teamGoals = teamGoals;
		this.otherGoals = otherGoals;
	}

	public int getId() {
		return id;
	}

	public String getTeamName() {
		return teamName;
	}

	public String getOtherTeam() {
		return otherTeam;
	}

	public boolean isHome() {
		return home;
	}

	public boolean isPlayed() {
		return played;
	}

	public long getDate() {
		return date;
	}

	public int getTeamGoals() {
		return teamGoals;
	}

	public int getOtherGoals() {
		return otherGoals;
	}
	
	public String toString() {
		if (played) {
			if (home)
				return getDateString() + " " + teamName + " - " + otherTeam
						+ " : " + teamGoals + " - " + otherGoals;
			else
				return getDateString() + " " + otherTeam + " - " + teamName
						+ " : " + otherGoals + " - " + teamGoals;
		} else {
			if (home)
				return getDateString() + " " + teamName + " - " + otherTeam;
			else
				return getDateString() + " " + otherTeam + " - " + teamName;
		}
	}
	
	public String getDateString() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(date);
		return cal.get(GregorianCalendar.DAY_OF_MONTH) + "/"
				+ (cal.get(GregorianCalendar.MONTH) + 1) + "/"
				+ cal.get(GregorianCalendar.YEAR);
	}
	
	
}

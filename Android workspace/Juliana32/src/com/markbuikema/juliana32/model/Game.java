package com.markbuikema.juliana32.model;


public class Game extends Event {

	private String teamName;
	private String otherTeam;

	private boolean home;
	private boolean played;

	private int teamGoals;
	private int otherGoals;

	public Game(int id, String teamName, String otherTeam, boolean home, long date, int teamGoals, int otherGoals) {
		super(id, date);
		this.teamName = teamName;
		this.otherTeam = otherTeam;
		this.home = home;

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

	public int getTeamGoals() {
		return teamGoals;
	}

	public int getOtherGoals() {
		return otherGoals;
	}

	@Override
	public String toString() {
		if (played) {
			if (home)
				return getDateString() + " " + teamName + " - " + otherTeam + " : " + teamGoals + " - " + otherGoals;
			else
				return getDateString() + " " + otherTeam + " - " + teamName + " : " + otherGoals + " - " + teamGoals;
		} else
			if (home)
				return getDateString() + " " + teamName + " - " + otherTeam;
			else
				return getDateString() + " " + otherTeam + " - " + teamName;
	}

}

package com.markbuikema.juliana32.model;

import java.util.ArrayList;

import com.markbuikema.juliana32.model.Team.Category;

public class Season {

	private int year;
	private ArrayList<Team> teams;

	public Season(int year) {
		this.year = year;
		teams = new ArrayList<Team>();
	}

	public int getYear() {
		return year;
	}
	
	public ArrayList<Team> getTeamsOfCategory(Category cat) {
		ArrayList<Team> specific = new ArrayList<Team>();
		for (Team team: teams) {
			if (team.isCategory(cat)) {
				specific.add(team);
			}
		}
		return specific;
	}
	

	public String getName() {
		return Integer.toString(year).substring(2) + "/" + Integer.toString(year + 1).substring(2);
	}

	public void addTeam(Team team) {
		teams.add(team);
	}
	
	public String toString() {
		return getName();
	}
	
	public int getGameCount() {
		int count = 0;
		for (Team t: teams) {
			count += t.getGameCount();
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Team> getTeams() {
		return (ArrayList<Team>) teams.clone();
	}

	public String toDebugString() {
		String string = year + "\n";
		for (Team t : teams) {
			string += t.getName() + ": " + t.getGameCount() + " games\n";
		}
		return string + "\n";
	}

}

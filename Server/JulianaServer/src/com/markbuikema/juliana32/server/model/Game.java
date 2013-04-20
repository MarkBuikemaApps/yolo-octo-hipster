package com.markbuikema.juliana32.server.model;

import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.markbuikema.juliana32.server.singletons.Seasons;

@XmlRootElement(name = "games")
public class Game {

	@XmlElement
	private int id;

	@XmlElement
	private String teamName;

	@XmlElement
	private String otherTeam;

	@XmlElement
	private boolean home;

	@XmlElement
	private long date;

	@XmlElement
	private int teamGoals;

	@XmlElement
	private int otherGoals;

	public Game() {
		id = Seasons.getLatestUnusedGameId();
	}

	public void setTeam(Team team) {
		this.teamName = team.getName();
	}

	public boolean isGame(int id) {
		return this.id == id;
	}

}

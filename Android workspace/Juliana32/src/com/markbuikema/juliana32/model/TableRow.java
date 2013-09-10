package com.markbuikema.juliana32.model;

import java.io.Serializable;

public class TableRow implements Comparable<TableRow>, Serializable {
	private static final long serialVersionUID = -5143404312528797219L;
	private static final int WIN_POINTS = 3;
	private static final int DRAW_POINTS = 1;

	private String teamName;
	private int played;
	private int won;
	private int drawn;
	private int minusPoints;
	private int scored;
	private int conceded;

	public TableRow(String teamName, int played, int won, int drawn, int minusPoints, int scored, int conceded) {
		this.teamName = teamName;
		this.played = played;
		this.won = won;
		this.drawn = drawn;
		this.minusPoints = minusPoints;
		this.scored = scored;
		this.conceded = conceded;
	}

	public TableRow() {
	}

	public String getTeamName() {
		return teamName;
	}

	public int getPlayed() {
		return played;
	}

	public int getWon() {
		return won;
	}

	public int getDrawn() {
		return drawn;
	}

	public int getLost() {
		return (played - won - drawn);
	}

	public int getPoints() {
		return (won * WIN_POINTS + drawn * DRAW_POINTS - minusPoints);
	}

	public int getMinusPoints() {
		return minusPoints;
	}

	public int getScored() {
		return scored;
	}

	public int getConceded() {
		return conceded;
	}

	@Override
	public int compareTo(TableRow another) {

		if (another instanceof TableRow) {
			TableRow other = (TableRow) another;
			if (other.getPoints() > getPoints())
				return 1;
			else
				if (other.getPoints() < getPoints())
					return -1;
				else {
					int goalDiff = scored - conceded;
					int otherGoalDiff = other.scored - other.conceded;
					if (otherGoalDiff > goalDiff)
						return 1;
					else
						if (otherGoalDiff < goalDiff)
							return -1;
						else {
							if (other.scored > scored) {
								return 1;
							} else
								return -1;
						}
				}
		} else
			return 0;
	}
}
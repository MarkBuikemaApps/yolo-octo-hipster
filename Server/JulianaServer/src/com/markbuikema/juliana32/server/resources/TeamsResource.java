package com.markbuikema.juliana32.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.markbuikema.juliana32.server.model.Game;
import com.markbuikema.juliana32.server.model.Season;
import com.markbuikema.juliana32.server.model.Team;
import com.markbuikema.juliana32.server.singletons.Seasons;

@Path("/teams")
public class TeamsResource {

	@GET
	@Path("/get/{year}/{teamId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Team findTeam(@PathParam("year") int season,
			@PathParam("teamId") int teamId) {
		Season s = Seasons.get().findSeason(season);
		if (s == null)
			return null;
		return s.getTeam(teamId);
	}

	@POST
	@Path("/add/{year}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addTeam(@PathParam("year") int season, Team team) {
		Season s = Seasons.get().findSeason(season);
		if (s == null) {
			s = new Season(season);
			Seasons.get().add(s);
		}

		if (!s.hasTeamWithName(team.getName())) {
			s.addTeam(team);
			System.out.println(team.toString());
		}

	}

	@POST
	@Path("/addgame/{year}/{teamId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addGame(@PathParam("year") int season,
			@PathParam("teamId") int teamId, Game game) {

		Season s = Seasons.get().findSeason(season);
		Team t = s.getTeam(teamId);
		game.setTeam(Seasons.get().findSeason(season).getTeam(teamId));
		t.addGame(game);

		System.out.println(game.toString());
	}

}
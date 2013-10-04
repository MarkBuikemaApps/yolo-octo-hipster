package com.markbuikema.juliana32.server.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.markbuikema.juliana32.server.model.Game;
import com.markbuikema.juliana32.server.model.Photo;
import com.markbuikema.juliana32.server.model.Season;
import com.markbuikema.juliana32.server.model.Table;
import com.markbuikema.juliana32.server.model.Team;
import com.markbuikema.juliana32.server.singletons.Seasons;

@Path("/teams")
public class TeamsResource {

	@GET
	@Path("/get/{year}/{teamId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Team findTeam(@PathParam("year") int season, @PathParam("teamId") int teamId) {
		Season s = Seasons.get().findSeason(season);
		if (s == null)
			return null;
		return s.findTeam(teamId);
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
	public void addGame(@PathParam("year") int season, @PathParam("teamId") int teamId, Game game) {

		Season s = Seasons.get().findSeason(season);
		Team t = s.findTeam(teamId);
		game.setTeam(Seasons.get().findSeason(season).findTeam(teamId));
		t.addGame(game);

		System.out.println(game.toString());
	}

	@PUT
	@Path("/putphoto/{year}/{teamId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putPhoto(@PathParam("year") int year, @PathParam("teamId") int teamId, Photo photo) {
		Seasons.get().findSeason(year).findTeam(teamId).addPhoto(photo);
	}

	@POST
	@Path("/posttable/{year}/{teamId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putTable(@PathParam("year") int year, @PathParam("teamId") int teamId, Table table) {
		Team team = Seasons.get().findSeason(year).findTeam(teamId);
		team.addTable(table);
		System.out.println(team.getName() + " has now " + team.getTableCount() + " tables.");
	}

	@GET
	@Path("/table/{year}/{teamId}/{tableId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Table getTable(@PathParam("year") int year, @PathParam("teamId") int teamId, @PathParam("tableId") int tableId) {
		Team team = Seasons.get().findSeason(year).findTeam(teamId);
		return team.getTeamTable(tableId);
	}

}

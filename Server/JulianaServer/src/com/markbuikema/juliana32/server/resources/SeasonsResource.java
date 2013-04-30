package com.markbuikema.juliana32.server.resources;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.markbuikema.juliana32.server.model.Season;
import com.markbuikema.juliana32.server.singletons.Seasons;

@Path("/seasons")
public class SeasonsResource {

	@GET
	@Path("/hello")
	@Produces(MediaType.APPLICATION_JSON)
	public String sayHello() {
		return "Hello Jersey";
	}
	
	@GET
	@Path("/get/all")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Season> getAllSeasons() {
		return Seasons.get().getAllSeasons();
	}
	
	@GET
	@Path("/get/{year}")
	@Produces(MediaType.APPLICATION_JSON)
	public Season getSeason(@PathParam("year") int year) {
		return Seasons.get().findSeason(year);
	}
	
	@POST
	@Path("/add/{year}")
	public void addSeason(@PathParam("year") int year) {
		if (Seasons.get().exists(year)) return;
		Seasons.get().add(new Season(year));
	}	
	
	
}
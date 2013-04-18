package com.markbuikema.juliana32.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/teams")
public class TeamsResource {

	@GET
	@Path("/hello")
	@Produces(MediaType.APPLICATION_JSON)
	public String sayHello() {
		return "Hello Jersey";
	}
	
	

}

package com.markbuikema.juliana32.server.resource;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.markbuikema.juliana32.server.model.TeaserNewsItem;
import com.markbuikema.juliana32.server.singletons.NewsItems;

@Path("/teasers")
public class TeaserResource {

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<TeaserNewsItem> getNewsItems() {

		for (TeaserNewsItem i:NewsItems.get().getTeaserItems()) {
			System.out.println("Answered get request: " + i.toString());
		}
		return NewsItems.get().getTeaserItems();
	}
	
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addItem(TeaserNewsItem item) {
		NewsItems.get().addTeaser(item);
		System.out.println("Added: " + item.toString());
	}
	
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public TeaserNewsItem getSpecificNewsItem(@PathParam("id") int id) {
		return NewsItems.get().getTeaserItems().get(id);
	}
	
	@GET
	@Path("/count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		return Integer.toString(NewsItems.get().getTeaserItems().size());
	}
	
	@PUT
	@Path("/edit/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void edit(@PathParam("id") int id, TeaserNewsItem item) {
		NewsItems.get().replaceTeaser(id, item);
	}
	
	@DELETE
	@Path("/delete/{id}")
	public void delete(@PathParam("id") int id) {
		NewsItems.get().removeTeaser(id);
	}
	
	

}

package com.markbuikema.juliana32.server.resources;

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

import com.markbuikema.juliana32.server.model.NewsItem;
import com.markbuikema.juliana32.server.singletons.NewsItems;

@Path("/news")
public class NewsResource {

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<NewsItem> getNewsItems() {

		for (NewsItem i:NewsItems.get().getNewsItems()) {
			System.out.println("Answered get request: " + i.toString());
		}
		return NewsItems.get().getNewsItems();
	}
	
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addItem(NewsItem item) {
		NewsItems.get().add(item);
		System.out.println("Added: " + item.toString());
	}
	
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public NewsItem getSpecificNewsItem(@PathParam("id") int id) {
		return NewsItems.get().getNewsItems().get(id);
	}
	
	@GET
	@Path("/count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		return Integer.toString(NewsItems.get().getNewsItems().size());
	}
	
	@PUT
	@Path("/edit/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void edit(@PathParam("id") int id, NewsItem item) {
		NewsItems.get().replace(id, item);
	}
	
	@DELETE
	@Path("/delete/{id}")
	public void delete(@PathParam("id") int id) {
		NewsItems.get().remove(id);
	}
	
	@POST
	@Path("/startcrawling")
	public void startCrawling() {
		NewsItems.get().startCrawling();
	}
	
	

}

package com.markbuikema.juliana32.server.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.markbuikema.juliana32.server.singletons.Seasons;

@XmlRootElement(name="photos")
public class Photo {

	@XmlElement()
	private int id;
	@XmlElement
	private String url;
	
	public Photo(){
		id = Seasons.getLatestUnusedPhotoId();
	}
}

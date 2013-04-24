package com.markbuikema.juliana32.server.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rows")
public class TableRow {
	@XmlElement
	private String teamName;
	@XmlElement
	private int played;
	@XmlElement
	private int won;
	@XmlElement
	private int drawn;
	@XmlElement
	private int lost;
	@XmlElement
	private int minusPoints;
	@XmlElement
	private int scored;
	@XmlElement
	private int conceded;
	
	public TableRow(){
		
	}
}

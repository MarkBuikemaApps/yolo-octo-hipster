package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.markbuikema.juliana32.server.singletons.Seasons;

@XmlRootElement(name="tables")
public class Table {
	
	@XmlElement
	private int id;
	
	@XmlElement
	private String name;

	@XmlElement
	private ArrayList<TableRow> rows;

	public Table() {
		id = Seasons.getLatestUnusedTableId();
	}
	
	public void setTable(ArrayList<TableRow> rows) {
		this.rows = rows;
	}
	
	public boolean isTable(int id) {
		return this.id == id;
	}

}

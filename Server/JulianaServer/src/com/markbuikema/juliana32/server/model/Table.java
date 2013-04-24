package com.markbuikema.juliana32.server.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Table {

	@XmlElement
	private ArrayList<TableRow> rows;

	public Table() {
	}
	
	public void setTable(ArrayList<TableRow> rows) {
		this.rows = rows;
	}

}

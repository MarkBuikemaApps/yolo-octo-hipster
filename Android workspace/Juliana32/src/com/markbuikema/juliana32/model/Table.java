package com.markbuikema.juliana32.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Table implements Serializable{

	private static final long serialVersionUID = -2658291608632019520L;
	private int id;
	private ArrayList<TableRow> rows;
	private String name;

	public Table(int id, ArrayList<TableRow> rows, String name) {
		this.rows = rows;
		this.name = name;

		Collections.sort(rows);

	}

	@SuppressWarnings("unchecked")
	public ArrayList<TableRow> getRows() {
		return (ArrayList<TableRow>) rows.clone();
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}

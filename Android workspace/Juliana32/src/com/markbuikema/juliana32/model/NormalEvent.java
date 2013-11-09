package com.markbuikema.juliana32.model;

import com.markbuikema.juliana32.util.FacebookHelper;

public class NormalEvent extends Event {

	private long endDate;
	private String name;
	private String location;

	public NormalEvent(int id, String startDate, String endDate, String name, String location) {
		super(id, FacebookHelper.toDate(startDate).getTimeInMillis());
		this.endDate = endDate == null ? -1 : FacebookHelper.toDate(endDate).getTimeInMillis();
		this.name = name;
		this.location = location;
	}

	public long getEndDate() {
		return endDate;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

}

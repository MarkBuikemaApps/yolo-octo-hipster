package com.markbuikema.juliana32.model;

import java.text.DecimalFormat;
import java.util.GregorianCalendar;

public abstract class Event {
	private int id;
	private long date;

	public Event(int id, long date) {
		this.id = id;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public long getDate() {
		return date;
	}

	public String getDateString() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(date);
		DecimalFormat df = new DecimalFormat("00");
		return cal.get(GregorianCalendar.DAY_OF_MONTH) + "/" + (cal.get(GregorianCalendar.MONTH) + 1) + "/"
				+ cal.get(GregorianCalendar.YEAR) + " " + df.format(cal.get(GregorianCalendar.HOUR_OF_DAY)) + ":"
				+ df.format(cal.get(GregorianCalendar.MINUTE));
	}
}

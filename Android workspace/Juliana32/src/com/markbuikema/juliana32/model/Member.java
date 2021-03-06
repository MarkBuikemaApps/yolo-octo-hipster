package com.markbuikema.juliana32.model;

import android.util.Log;

@Deprecated
public class Member {

	private String name;
	private String function;
	private String phoneNumber;
	private String email;

	public Member(String name, String function, String phoneNumber, String email) {
		this.name = name;
		this.function = function;
		this.phoneNumber = phoneNumber;
		this.email = email;

		Log.d("members", "Created: " + toString());
	}

	public String getName() {
		return name;
	}

	public String getFunction() {
		return function;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return name + " (" + function + ") tel. " + phoneNumber + ", @. " + email;
	}
}

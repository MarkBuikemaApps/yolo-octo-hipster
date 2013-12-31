package com.markbuikema.juliana32.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.ui.TeamGridFragment;

public class TeamsFragmentPagerAdapter extends FragmentPagerAdapter {

	private TeamAdapter[] adapters;

	public TeamsFragmentPagerAdapter( Context context, TeamAdapter... adapters ) {
		super( ( (FragmentActivity) context ).getSupportFragmentManager() );

		this.adapters = adapters;
	}

	@Override
	public Fragment getItem( int position ) {
		Fragment fragment = TeamGridFragment.create( adapters[ position ] );
		return fragment;
	}

	@Override
	public int getCount() {
		return adapters.length;
	}

	@Override
	public CharSequence getPageTitle( int position ) {
		return Category.values()[ position ].getName();

	}

}
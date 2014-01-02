package com.markbuikema.juliana32.section;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.widget.ViewPager;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.TeamAdapter;
import com.markbuikema.juliana32.adapter.TeamsPagerAdapter;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.viewpagerindicator.TabPageIndicator;

public class Teams {

	private MainActivity activity;
	private TeamAdapter[] teamAdapters;
	private static List<Team> teamList;

	private TabPageIndicator tabs;
	private ViewPager pager;
	private TeamsPagerAdapter adapter;

	private boolean loaded = false;

	public Teams( MainActivity act ) {
		activity = act;

		boolean isTablet = act.getResources().getBoolean( R.bool.isTablet );
		if ( isTablet )
			teamAdapters = new TeamAdapter[ 1 ];
		else
			teamAdapters = new TeamAdapter[ Category.values().length ];

		View mainView = activity.findViewById( R.id.teamsView );
		pager = (ViewPager) mainView.findViewById( R.id.teamsPager );

		if ( ! isLoaded() )
			reloadData();

		tabs = (TabPageIndicator) mainView.findViewById( R.id.teamsTabsIndicator );
		tabs.setOnPageChangeListener( new OnPageChangeListener() {

			@Override
			public void onPageSelected( int arg0 ) {
			}

			@Override
			public void onPageScrolled( int arg0, float arg1, int arg2 ) {

			}

			@Override
			public void onPageScrollStateChanged( int arg0 ) {
			}
		} );

		if ( isTablet )
			tabs.setVisibility( View.GONE );
	}

	protected void createAdapters() {
		if ( teamAdapters.length == 1 )
			teamAdapters[ 0 ] = new TeamAdapter( activity, null, teamList );
		else
			for ( int i = 0; i < teamAdapters.length; i++ ) {
				Category cat = Category.values()[ i ];
				teamAdapters[ i ] = new TeamAdapter( activity, cat, getTeamsFromCategory( cat ) );
			}

	}

	public static List<Team> getTeamsFromCategory( Category cat ) {
		if ( cat == null )
			return teamList;

		List<Team> teams = new ArrayList<Team>();
		for ( Team team : teamList )
			if ( team.getCategory() == cat )
				teams.add( team );

		return teams;
	}

	public static Team findTeamById( int teamId ) {
		for ( Team team : teamList )
			if ( team.getId() == teamId )
				return team;
		return null;
	}

	public static List<Team> getTeams() {
		return teamList;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public boolean areAdaptersEmpty() {
		return teamAdapters == null || teamAdapters[ 0 ] == null || teamAdapters[ 0 ].getCount() < 1;
	}

	public void reloadData() {

		loaded = false;

		Log.d( "Teams", "Loading data..." );

		TeamsRetriever retriever = new TeamsRetriever( activity ) {
			@Override
			protected void onPostExecute( List<Team> result ) {
				loaded = true;
				activity.fixActionBar();
				teamList = result;

				createAdapters();

				adapter = new TeamsPagerAdapter( activity, teamAdapters );
				pager.setAdapter( adapter );
				tabs.setViewPager( pager );

				Log.d( "Teams", "Done loading data. " + adapter.getCount() );
			}
		};

		retriever.execute();
	}
}

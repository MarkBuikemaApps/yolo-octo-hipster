package com.markbuikema.juliana32.section;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.widget.ViewPager;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.TeamAdapter;
import com.markbuikema.juliana32.adapter.TeamsPagerAdapter;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.util.Util;
import com.viewpagerindicator.TabPageIndicator;

public class Teams {

	private MainActivity activity;
	private TeamAdapter[] teamAdapters;
	private static List<Team> teamList;

	private TabPageIndicator tabs;
	private ViewPager pager;
	private TeamsPagerAdapter pagerAdapter;

	private boolean loaded = false;

	public Teams( final MainActivity act ) {
		activity = act;

		boolean isLandscape = act.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		boolean isTablet = act.getResources().getBoolean( R.bool.isTablet );
		if ( isTablet )
			teamAdapters = new TeamAdapter[ 1 ];
		else
			teamAdapters = new TeamAdapter[ Category.values().length ];

		View mainView = activity.findViewById( R.id.teamsView );
		pager = (ViewPager) mainView.findViewById( R.id.teamsPager );

		if ( isLandscape || isTablet ) {
			FrameLayout tabsContainer = (FrameLayout) act.findViewById( R.id.tabsContainer );
			tabs = new TabPageIndicator( act );
			tabs.setId( R.id.teamsTabsIndicator );
			LayoutParams lp = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT );
			tabsContainer.addView( tabs, lp );

			act.setTabs( tabs );
		} else {
			tabs = (TabPageIndicator) act.findViewById( R.id.teamsTabsIndicator );
		}

		tabs.setTypeface( Util.getRobotoLight( act ) );

		loaded = false;

		pagerAdapter = new TeamsPagerAdapter();
		pager.setAdapter( pagerAdapter );
		pager.setPageMargin( mainView.getContext().getResources().getDimensionPixelSize( R.dimen.gridSpacing ) );
		tabs.setViewPager( pager );

		if ( teamList == null ) {
			Log.d( "Teams", "Loading data..." );

			TeamsRetriever retriever = new TeamsRetriever( activity ) {
				@Override
				protected void onPostExecute( List<Team> result ) {
					loaded = true;
					activity.fixActionBar();
					teamList = result;

					createAdapters();
					pagerAdapter.setAdapters( teamAdapters );

					tabs.notifyDataSetChanged();

					setMostFreqTab();

					Log.d( "Teams", "Done loading data. " + pagerAdapter.getCount() );
				}
			};

			retriever.execute();
		} else {
			loaded = true;

			createAdapters();
			pagerAdapter.setAdapters( teamAdapters );

			tabs.notifyDataSetChanged();
		}

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

	public void onResume() {
		setMostFreqTab();
	}

	private void setMostFreqTab() {
		if ( teamAdapters.length > 1 ) {
			SharedPreferences sp = activity.getSharedPreferences( "categories", 0 );
			int highestCountCategory = 0;
			int highestCount = 0;
			for ( int i = 0; i < Category.values().length; i++ ) {
				int count = sp.getInt( "cat" + i, 0 );
				if ( count > highestCount ) {
					highestCountCategory = i;
					highestCount = count;
				}
			}

			pager.setCurrentItem( highestCountCategory, false );
		}
	}
}

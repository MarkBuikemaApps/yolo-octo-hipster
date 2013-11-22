package com.markbuikema.juliana32.section;

import java.util.List;

import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.TeamAdapter;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.util.DataManager;

public class Teams {

	private final static String TAG = "Teams";
	private final static long TWO_WEEKS = 1000 * 60 * 60 * 24 * 14;
	private MainActivity activity;
	private ListView teamsList;
	private TeamAdapter teamAdapter;
	private List<Team> teams;
	private boolean finishedLoading = false;
	private ProgressBar loader;
	protected boolean loading;

	public Teams(MainActivity act) {
		activity = act;

		teams = DataManager.getInstance().getTeams();

		loader = (ProgressBar) act.findViewById(R.id.loading);
		View mainView = activity.findViewById(R.id.teamsView);
		teamsList = (ListView) mainView.findViewById(R.id.teams_list);
		teamAdapter = new TeamAdapter(activity);
		teamsList.setAdapter(teamAdapter);
		teamsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Team team = findTeam(teamAdapter.getItem(arg2).getTeamId());
				activity.requestTeamDetailPage(team);
			}
		});
	}

	private Team findTeam(int teamId) {
		for (Team t : teams)
			if (t.getId() == teamId)
				return t;
		return null;
	}

	public boolean isLoaded() {
		return finishedLoading;
	}

	public void onTeamsLoaded() {
		teamAdapter.setTeams(DataManager.getInstance().getTeams());
	}

	public boolean isLoading() {
		return loading;
	}

	public int getAdapterCount() {
		return teamAdapter.getCount();
	}

}

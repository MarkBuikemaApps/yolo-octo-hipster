package com.markbuikema.juliana32.section;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.util.DataManager;

public class Teams {

	private final static String TAG = "Teams";
	private final static long TWO_WEEKS = 1000 * 60 * 60 * 24 * 14;
	private MainActivity activity;
	private ListView teamsList;
	private TeamAdapter teamAdapter;
	private SeasonAdapter seasonAdapter;
	private List<Season> seasons;
	private Spinner seasonSpinner;
	private boolean finishedLoading = false;

	public Teams(MainActivity act) {
		activity = act;
		seasons = DataManager.getInstance().getTeams();

		seasonSpinner = (Spinner) act.findViewById(R.id.menuSeason);
		View mainView = activity.findViewById(R.id.teamsView);
		teamsList = (ListView) mainView.findViewById(R.id.teams_list);
		teamAdapter = new TeamAdapter(activity);
		teamsList.setAdapter(teamAdapter);
		seasonAdapter = new SeasonAdapter(act);
		seasonSpinner.setAdapter(seasonAdapter);
		seasonSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onSeasonSelected(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		teamsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Team team = findTeam(teamAdapter.getItem(arg2).getTeamId());
				activity.requestTeamDetailPage(team);
			}
		});

	}

	private Team findTeam(int teamId) {
		for (Season s : seasons)
			for (Team t : s.getTeams())
				if (t.getId() == teamId)
					return t;
		return null;
	}

	public ArrayList<Game> getLatestGames() {
		long currentTime = System.currentTimeMillis();
		long minTime = currentTime - TWO_WEEKS;
		int index = getNewestSeasonIndex();
		if (index == -1)
			return new ArrayList<Game>();
		Season latest = seasons.get(index);
		ArrayList<Game> games = new ArrayList<Game>();
		for (Team team : latest.getTeams())
			for (Game game : team.getGames())
				if (game.getDate() >= minTime)
					games.add(game);

		for (Game game : games)
			Log.d(TAG, game.toString());
		return games;
	}

	public int getNewestSeasonIndex() {
		if (seasons.size() == 0)
			return -1;
		int highest = 0;
		int index = 0;
		for (int i = 0; i < seasons.size(); i++)
			if (seasons.get(i).getYear() > highest) {
				highest = seasons.get(i).getYear();
				index = i;
			}
		return index;
	}

	public boolean isLoaded() {
		return finishedLoading;
	}

	private void onSeasonSelected(int seasonIndex) {
		teamAdapter.setSeason(seasons.get(seasonIndex));
	}

	public void reload() {
		new TeamsRetriever() {
			@Override
			protected void onPostExecute(java.util.List<Season> result) {

			}
		}.execute();
	}

	private class SeasonAdapter extends ArrayAdapter<Season> {

		public SeasonAdapter(Context context) {
			super(context, R.layout.listitem_season, seasons);
		}
	}

	private class TeamAdapter extends ArrayAdapter<TeamListItem> {

		private Season season;

		public TeamAdapter(Context context) {
			super(context, 0);
		}

		private void addCaption(String text) {
			add(new TeamListItem(getContext(), true, text, -1));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			convertView = getItem(position).getView();

			TextView text = (TextView) convertView.findViewById(R.id.teamText);
			text.setText(getItem(position).getTitle());

			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		private void invalidate() {
			clear();

			ArrayList<Team> senioren = season.getTeamsOfCategory(Category.SENIOREN);
			ArrayList<Team> junioren = season.getTeamsOfCategory(Category.JUNIOREN);
			ArrayList<Team> dames = season.getTeamsOfCategory(Category.DAMES);

			addCaption("Senioren");
			for (Team t : senioren)
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));
			addCaption("Junioren");
			for (Team t : junioren)
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));
			addCaption("Dames");
			for (Team t : dames)
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));

		}

		@Override
		public boolean isEnabled(int position) {
			return !getItem(position).isCaption();
		}

		public void setSeason(Season season) {
			this.season = season;
			invalidate();
		}
	}

	private class TeamListItem {
		private boolean caption;
		private String title;
		private int teamId;
		private View view;

		public TeamListItem(Context context, boolean caption, String title, int teamId) {
			this.caption = caption;
			this.title = title;
			this.teamId = teamId;

			view = LayoutInflater.from(context).inflate(caption ? R.layout.listitem_caption : R.layout.listitem_team, null);

		}

		public int getTeamId() {
			return teamId;
		}

		public String getTitle() {
			return title;
		}

		public View getView() {
			return view;
		}

		public boolean isCaption() {
			return caption;
		}
	}

}
package com.markbuikema.juliana32.sections;

import static com.markbuikema.juliana32.tools.Tools.getHttpContent;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;

public class Teams {

	private final static String TAG = "Teams";

	private final static String INFORMATION_URL = MainActivity.BASE_SERVER_URL + "/seasons/get/all";

	private Activity activity;
	private ListView teamsList;
	private TeamAdapter teamAdapter;
	private SeasonAdapter seasonAdapter;
	private ArrayList<Season> seasons;
	private ProgressBar loader;
	private int selectedSeasonIndex;
	private Spinner seasonSpinner;

	private boolean finishedLoading = false;

	public Teams(Activity act) {
		this.activity = act;
		loader = (ProgressBar) act.findViewById(R.id.loading);
		seasonSpinner = (Spinner) act.findViewById(R.id.menuSeason);
		View mainView = activity.findViewById(R.id.teams);
		teamsList = (ListView) mainView.findViewById(R.id.teams_list);
		teamAdapter = new TeamAdapter(activity);
		teamsList.setAdapter(teamAdapter);
		seasons = new ArrayList<Season>();
		seasonSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onSeasonSelected(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		new InformationRetriever().execute();
	}

	private void onSeasonSelected(int seasonIndex) {
		selectedSeasonIndex = seasonIndex;
		teamAdapter.setSeason(seasons.get(seasonIndex));
	}

	public boolean isLoaded() {
		return finishedLoading;
	}

	private class InformationRetriever extends AsyncTask<Void, Season, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String json = getHttpContent(INFORMATION_URL);
			try {
				JSONObject base = new JSONObject(json);
				try {
					JSONArray seasons = base.getJSONArray("seasons");
					for (int i = 0; i < seasons.length(); i++) {
						publishProgress(processSeasonJSON(seasons.getJSONObject(i)));
					}
				} catch (JSONException e) {
					JSONObject season = base.getJSONObject("seasons");
					publishProgress(processSeasonJSON(season));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		private Season processSeasonJSON(JSONObject seasonJSON) {
			Season season = null;
			try {
				int year = seasonJSON.getInt("year");
				season = new Season(year);

				try {
					JSONArray seasonTeams = seasonJSON.getJSONArray("teams");
					for (int i = 0; i < seasonTeams.length(); i++) {
						season.addTeam(processTeamJSON(seasonTeams.getJSONObject(i)));
					}
				} catch (JSONException e) {
					JSONObject seasonTeam = seasonJSON.getJSONObject("teams");
					season.addTeam(processTeamJSON(seasonTeam));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d(TAG, season.toString());
			return season;

		}

		private Team processTeamJSON(JSONObject teamJSON) {
			Team team = null;
			try {
				int id = teamJSON.getInt("id");
				String name = teamJSON.getString("name");
				Category category = Category.valueOf(teamJSON.getString("category"));

				team = new Team(id, name, category);

				try {
					JSONArray games = teamJSON.getJSONArray("games");
					for (int i = 0; i < games.length(); i++) {
						team.addGame(processGameJSON(games.getJSONObject(i)));
					}
				} catch (JSONException e) {
					JSONObject game = teamJSON.getJSONObject("games");
					team.addGame(processGameJSON(game));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return team;
		}

		private Game processGameJSON(JSONObject gameJSON) {
			Game game = null;
			try {
				int id = gameJSON.getInt("id");
				String teamName = gameJSON.getString("teamName");
				String otherTeam = gameJSON.getString("otherTeam");
				boolean home = gameJSON.getBoolean("home");
				long date = gameJSON.getLong("date");
				int teamGoals = gameJSON.getInt("teamGoals");
				int otherGoals = gameJSON.getInt("otherGoals");

				game = new Game(id, teamName, otherTeam, home, date, teamGoals, otherGoals);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return game;
		}

		@Override
		protected void onProgressUpdate(Season... values) {
			for (Season season : values)
				seasons.add(season);
		}

		@Override
		protected void onPostExecute(Void result) {
			finishedLoading = true;
			loader.setVisibility(View.GONE);
			seasonAdapter = new SeasonAdapter(activity, seasons);
			seasonSpinner.setAdapter(seasonAdapter);

			if (seasons.size() != 0) {
				int highest = 0;
				int index = 0;
				for (int i = 0; i < seasons.size(); i++) {
					if (seasons.get(i).getYear() > highest) {
						highest = seasons.get(i).getYear();
						index = i;
					}
				}
				onSeasonSelected(index);
			}
		}

	}

	private class SeasonAdapter extends ArrayAdapter<Season> {

		public SeasonAdapter(Context context, ArrayList<Season> seasons) {
			super(context, R.layout.listitem_season, seasons);
		}
	}

	private class TeamAdapter extends ArrayAdapter<TeamListItem> {

		private int teamId = 0;
		private Season season;

		public TeamAdapter(Context context) {
			super(context, 0);
		}

		public void setSeason(Season season) {
			this.season = season;
			invalidate();
		}

		private void invalidate() {
			clear();

			ArrayList<Team> senioren = season.getTeamsOfCategory(Category.SENIOREN);
			ArrayList<Team> junioren = season.getTeamsOfCategory(Category.JUNIOREN);
			ArrayList<Team> dames = season.getTeamsOfCategory(Category.DAMES);

			addCaption("Senioren");
			for (Team t : senioren) {
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));
			}
			addCaption("Junioren");
			for (Team t : junioren) {
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));
			}
			addCaption("Dames");
			for (Team t : dames) {
				add(new TeamListItem(getContext(), false, t.getName(), t.getId()));
			}

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
		public boolean isEnabled(int position) {
			return !getItem(position).isCaption();
		}

		@Override
		public int getViewTypeCount() {
			return 2;
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

		public boolean isCaption() {
			return caption;
		}

		public View getView() {
			return view;
		}

		public String getTitle() {
			return title;
		}

		public int getTeamId() {
			return teamId;
		}
	}

}

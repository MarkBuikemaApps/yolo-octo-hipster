package com.markbuikema.juliana32.sections;

import static com.markbuikema.juliana32.tools.Tools.getHttpContent;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.activities.MainActivity.FailureReason;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.Photo;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Table;
import com.markbuikema.juliana32.model.TableRow;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;

public class Teams {

	public enum InformationStatus {
		SUCCESS, FAILURE
	}

	private final static String TAG = "Teams";
	private final static long TWO_WEEKS = 1000 * 60 * 60 * 24 * 14;
	private final static String INFORMATION_URL = MainActivity.BASE_SERVER_URL + "/seasons/get/all";

	private MainActivity activity;
	private ListView teamsList;
	private TeamAdapter teamAdapter;
	private SeasonAdapter seasonAdapter;
	private ArrayList<Season> seasons;
	private ProgressBar loader;
	private Spinner seasonSpinner;

	private boolean finishedLoading = false;

	public Teams(MainActivity act) {
		this.activity = act;
		loader = (ProgressBar) act.findViewById(R.id.loading);
		seasonSpinner = (Spinner) act.findViewById(R.id.menuSeason);
		View mainView = activity.findViewById(R.id.teamsView);
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

		teamsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Team team = findTeam(teamAdapter.getItem(arg2).getTeamId());
				activity.requestTeamDetailPage(team);
			}
		});

		new InformationRetriever().execute();
	}

	private Team findTeam(int teamId) {
		for (Season s : seasons) {
			for (Team t : s.getTeams()) {
				if (t.getId() == teamId) return t;
			}
		}
		return null;
	}

	private void onSeasonSelected(int seasonIndex) {
		teamAdapter.setSeason(seasons.get(seasonIndex));
	}

	public boolean isLoaded() {
		return finishedLoading;
	}

	private class InformationRetriever extends AsyncTask<Void, Season, InformationStatus> {

		@Override
		protected InformationStatus doInBackground(Void... params) {
			
			
			Log.d(TAG, "begin");
			activity.checkNetworkStatus();
			
			String json = getHttpContent(INFORMATION_URL);
			Log.d(TAG, "after");
			
			
			if (json == null) return InformationStatus.FAILURE;
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
				return InformationStatus.FAILURE;
			}
			return InformationStatus.SUCCESS;
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
			try {
				try {
					JSONArray photos = teamJSON.getJSONArray("photos");
					for (int i = 0; i < photos.length(); i++) {
						team.addPhoto(processPhotoJSON(photos.getJSONObject(i)));
					}
				} catch (JSONException e) {
					JSONObject photo = teamJSON.getJSONObject("photos");
					team.addPhoto(processPhotoJSON(photo));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				JSONArray tables = teamJSON.getJSONArray("tables");

				for (int t = 0; t < tables.length(); t++) {

					JSONObject table = tables.getJSONObject(t);

					int tableId = table.getInt("id");
					String tableName = table.getString("name");
					ArrayList<TableRow> rowList = new ArrayList<TableRow>();

					try {
						JSONArray rows = table.getJSONArray("rows");
						for (int i = 0; i < rows.length(); i++) {
							rowList.add(processTableRowJSON(rows.getJSONObject(i)));
						}
					} catch (JSONException e) {
						rowList.add(processTableRowJSON(table.getJSONObject("rows")));
					}

					Table result = new Table(tableId, rowList, tableName);
					team.addTable(result);
				}
			} catch (JSONException e) {
				try {
					JSONObject table = teamJSON.getJSONObject("tables");
					int tableId = table.getInt("id");
					String tableName = table.getString("name");
					ArrayList<TableRow> rowList = new ArrayList<TableRow>();

					try {
						JSONArray rows = table.getJSONArray("rows");
						for (int i = 0; i < rows.length(); i++) {
							rowList.add(processTableRowJSON(rows.getJSONObject(i)));
						}
					} catch (JSONException ex) {
						rowList.add(processTableRowJSON(table.getJSONObject("rows")));
					}

					Table result = new Table(tableId, rowList, tableName);
					team.addTable(result);

				} catch (JSONException e1) {
					e.printStackTrace();
				}
			}
			return team;
		}

		private Photo processPhotoJSON(JSONObject photoJSON) {
			try {
				String url = photoJSON.getString("url");
				int id = photoJSON.getInt("id");
				return new Photo(id, url);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		private TableRow processTableRowJSON(JSONObject rowJSON) {
			try {
				String teamName = rowJSON.getString("teamName");
				int played = rowJSON.getInt("played");
				int won = rowJSON.getInt("won");
				int drawn = rowJSON.getInt("drawn");
				int minusPoints = rowJSON.getInt("minusPoints");
				int scored = rowJSON.getInt("scored");
				int conceded = rowJSON.getInt("conceded");
				return new TableRow(teamName, played, won, drawn, minusPoints, scored, conceded);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
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
		protected void onPreExecute() {
			Log.d(TAG, "started informationretriever");
		}

		@Override
		protected void onPostExecute(InformationStatus status) {
			Log.d(TAG, "Result: " + status);
			finishedLoading = true;
			loader.setVisibility(View.GONE);
			if (status == InformationStatus.SUCCESS) {
				//succesfully loaded information
				
				if (activity.isFailurePageShown()) {
					activity.hideFailurePage();
				}
				seasonAdapter = new SeasonAdapter(activity, seasons);
				seasonSpinner.setAdapter(seasonAdapter);

				if (seasons.size() != 0) {

					onSeasonSelected(getNewestSeasonIndex());
				}
			} else {

				activity.requestFailurePage(FailureReason.SERVER_OFFLINE);
				
			}

			activity.notifyDoneLoadingSeasons();
		}
	}
	
	public void reload() {
		new InformationRetriever().execute();
	}

	public ArrayList<Game> getLatestGames() {
		long currentTime = System.currentTimeMillis();
		long minTime = currentTime - TWO_WEEKS;
		int index = getNewestSeasonIndex();
		if (index == -1) return new ArrayList<Game>();
		Season latest = seasons.get(index);
		ArrayList<Game> games = new ArrayList<Game>();
		for (Team team : latest.getTeams()) {
			for (Game game : team.getGames()) {
				if (game.getDate() >= minTime) games.add(game);
			}
		}

		for (Game game : games) {
			Log.d(TAG, game.toString());
		}
		return games;
	}

	public int getNewestSeasonIndex() {
		if (seasons.size() == 0) return -1;
		int highest = 0;
		int index = 0;
		for (int i = 0; i < seasons.size(); i++) {
			if (seasons.get(i).getYear() > highest) {
				highest = seasons.get(i).getYear();
				index = i;
			}
		}
		return index;
	}

	private class SeasonAdapter extends ArrayAdapter<Season> {

		public SeasonAdapter(Context context, ArrayList<Season> seasons) {
			super(context, R.layout.listitem_season, seasons);
		}
	}

	private class TeamAdapter extends ArrayAdapter<TeamListItem> {

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

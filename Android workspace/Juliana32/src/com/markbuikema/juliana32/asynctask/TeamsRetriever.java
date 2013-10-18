package com.markbuikema.juliana32.asynctask;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Table;
import com.markbuikema.juliana32.model.TableRow;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.util.Util;

//FIXME
public class TeamsRetriever extends AsyncTask<Void, Season, List<Season>> {

	private static final String TAG = "SplashActivity";
	private Context context;

	public TeamsRetriever(Context context) {
		this.context = context;
	}

	@Override
	protected List<Season> doInBackground(Void... params) {

		List<Season> list = new ArrayList<Season>();

		Log.d(TAG, "Started retrieving teams");

		String json = Util.loadJSONFromAsset(context, "sample_teams.json");

		Log.d(TAG, "Teams json: " + json);

		if (json == null) {
			Log.e(TAG, "null json!");
			return list;
		}
		try {
			JSONObject base = new JSONObject(json);
			Log.d(TAG, "1");
			try {
				JSONArray seasons = base.getJSONArray("seasons");
				for (int i = 0; i < seasons.length(); i++) {
					Season season = processSeasonJSON(seasons.getJSONObject(i));
					if (season != null)
						list.add(season);
				}
			} catch (JSONException e) {
				JSONObject season = base.getJSONObject("seasons");
				Season seasonObject = processSeasonJSON(season);
				if (seasonObject != null)
					list.add(seasonObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return list;
		}

		Log.d("TAG", "Done loading teams");
		return list;
	}

	private Season processSeasonJSON(JSONObject seasonJSON) {
		Season season = null;
		try {
			int year = seasonJSON.getInt("year");
			season = new Season(year);

			try {
				JSONArray seasonTeams = seasonJSON.getJSONArray("teams");
				for (int i = 0; i < seasonTeams.length(); i++) {
					Team team = processTeamJSON(seasonTeams.getJSONObject(i));
					if (team != null)
						season.addTeam(team);
				}
			} catch (JSONException e) {
				JSONObject seasonTeam = seasonJSON.getJSONObject("teams");
				Team team = processTeamJSON(seasonTeam);
				if (team != null)
					season.addTeam(team);
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
					Game game = processGameJSON(games.getJSONObject(i));
					if (game != null)
						team.addGame(game);
				}
			} catch (JSONException e) {
				JSONObject game = teamJSON.getJSONObject("games");
				Game gameObject = processGameJSON(game);
				if (gameObject != null)
					team.addGame(gameObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			try {
				JSONArray photos = teamJSON.getJSONArray("photos");
				for (int i = 0; i < photos.length(); i++)
					team.addPhoto(processPhotoJSON(photos.getJSONObject(i)));
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
						TableRow tableRow = processTableRowJSON(rows.getJSONObject(i));
						if (tableRow != null)
							rowList.add(tableRow);
					}
				} catch (JSONException e) {
					TableRow tableRow = processTableRowJSON(table.getJSONObject("rows"));
					if (tableRow != null)
						rowList.add(tableRow);
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
						TableRow row = processTableRowJSON(rows.getJSONObject(i));
						if (row != null)
							rowList.add(row);
					}
				} catch (JSONException ex) {
					TableRow row = processTableRowJSON(table.getJSONObject("rows"));
					if (row != null)
						rowList.add(row);
				}

				Table result = new Table(tableId, rowList, tableName);
				team.addTable(result);

			} catch (JSONException e1) {
				e.printStackTrace();
			}
		}
		return team;
	}

	private String processPhotoJSON(JSONObject photoJSON) {
		try {
			String url = photoJSON.getString("url");
			return url;
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

}

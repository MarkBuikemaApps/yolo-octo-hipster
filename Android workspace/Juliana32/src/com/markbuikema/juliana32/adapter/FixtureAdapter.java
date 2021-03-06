package com.markbuikema.juliana32.adapter;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Game;

public class FixtureAdapter extends ArrayAdapter<Game> {

	public FixtureAdapter(Context context, List<Game> objects) {
		super(context, 0, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_game, null);
		TextView date = (TextView) convertView.findViewById(R.id.game_date);
		TextView homeTeam = (TextView) convertView.findViewById(R.id.game_home_team_name);
		TextView awayTeam = (TextView) convertView.findViewById(R.id.game_away_team_name);
		TextView homeScore = (TextView) convertView.findViewById(R.id.game_home_team_score);
		TextView awayScore = (TextView) convertView.findViewById(R.id.game_away_team_score);

		TextView scoreDivider = (TextView) convertView.findViewById(R.id.game_team_score_divider);

		Game game = getItem(position);

		date.setText(game.getDateString());
		homeTeam.setText(game.isHome() ? game.getTeamName() : game.getOtherTeam());
		awayTeam.setText(game.isHome() ? game.getOtherTeam() : game.getTeamName());
		if (game.isPlayed()) {
			homeScore.setText(Integer.toString(game.isHome() ? game.getTeamGoals() : game.getOtherGoals()));
			awayScore.setText(Integer.toString(game.isHome() ? game.getOtherGoals() : game.getTeamGoals()));
			scoreDivider.setVisibility(View.VISIBLE);
		} else {
			homeScore.setText("");
			awayScore.setText("");
			scoreDivider.setVisibility(View.GONE);

		}

		return convertView;
	}

}
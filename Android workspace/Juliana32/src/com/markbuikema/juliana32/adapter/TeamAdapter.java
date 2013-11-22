package com.markbuikema.juliana32.adapter;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.adapter.TeamAdapter.TeamListItem;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;

public class TeamAdapter extends ArrayAdapter<TeamListItem> {

	private List<Team> teams;

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

		List<Team> senioren = getTeamsOfCategory(Category.SENIOREN);
		List<Team> junioren = getTeamsOfCategory(Category.JUNIOREN);
		List<Team> dames = getTeamsOfCategory(Category.DAMES);

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

	private List<Team> getTeamsOfCategory(Category cat) {
		List<Team> teams = new ArrayList<Team>();
		for (Team t : this.teams)
			if (t.getCategory() == cat)
				teams.add(t);
		return teams;

	}

	@Override
	public boolean isEnabled(int position) {
		return !getItem(position).isCaption();
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
		invalidate();
	}

	public class TeamListItem {
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
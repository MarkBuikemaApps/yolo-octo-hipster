package com.markbuikema.juliana32.sections;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;

public class Teams {

	private Activity activity;
	private ListView teamsList;
	private ArrayAdapter<TeamListItem> adapter;

	public Teams(Activity act) {
		this.activity = act;
		View mainView = activity.findViewById(R.id.teams);
		teamsList = (ListView) mainView.findViewById(R.id.teams_list);
		adapter = new TeamAdapter(activity);
		teamsList.setAdapter(adapter);

	}

	private class TeamAdapter extends ArrayAdapter<TeamListItem> {

		private int teamId = 0;

		public TeamAdapter(Context context) {
			super(context, 0);

			add("Senioren", true);
			add("Juliana 1", false);
			add("Juliana 2", false);
			add("Juliana 3", false);
			add("Juliana 4", false);
			add("Juliana 5", false);
			add("Juliana 6", false);
			add("Juliana 7", false);
			add("Jeugd", true);
			add("Juliana B1", false);
			add("Juliana C1", false);
			add("Juliana C2", false);
			add("Juliana D1", false);
			add("Juliana E1", false);
			add("Juliana E2", false);
			add("Juliana F1", false);
			add("Dames", true);
			add("Juliana DA1", false);

		}

		private void add(String text, boolean isCaption) {
			add(new TeamListItem(getContext(), isCaption,  text, isCaption ? -1 : (teamId++)));
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
			
			view = LayoutInflater.from(context).inflate(caption?R.layout.listitem_caption:R.layout.listitem_team, null);
			
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

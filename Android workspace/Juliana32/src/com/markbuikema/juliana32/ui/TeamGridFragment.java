package com.markbuikema.juliana32.ui;

import org.holoeverywhere.widget.GridView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.adapter.TeamAdapter;

public class TeamGridFragment extends Fragment {

	private TeamAdapter adapter;

	public static TeamGridFragment create( TeamAdapter adapter ) {
		TeamGridFragment fragment = new TeamGridFragment();
		fragment.adapter = adapter;
		return fragment;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		View view = inflater.inflate( R.layout.grid_teams, null );
		GridView grid = (GridView) view.findViewById( R.id.teamsGrid );
		grid.setAdapter( adapter );
		return view;
	}

}

package com.markbuikema.juliana32.sections;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.TableRow;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.tools.FixtureAdapter;
import com.markbuikema.juliana32.tools.TableAdapter;
import com.markbuikema.juliana32.tools.Tools;

public class TeamDetail {

	private Team team;
	private MainActivity activity;

	private Button uitslagenButton;
	private Button programmaButton;

	private ViewPager photoPager;
	private ListView table;

	private TableAdapter tableAdapter;
	
	private FragmentManager fm;
	private SectionsPagerAdapter spa;

	private ListView uitslagen;
	private ListView programma;
	
	private FrameLayout uitslagenContainer;
	private FrameLayout programmaContainer;

	@SuppressWarnings("unchecked")
	public TeamDetail(MainActivity activity, Team team) {
		this.team = team;
		this.activity = activity;

		View mainView = activity.findViewById(R.id.teamDetailView);

		uitslagenContainer = (FrameLayout) mainView.findViewById(R.id.uitslagenContainer);
		programmaContainer = (FrameLayout) mainView.findViewById(R.id.programmaContainer);
		
		uitslagenButton = (Button) mainView.findViewById(R.id.uitslagenButton);
		programmaButton = (Button) mainView.findViewById(R.id.programmaButton);
		
		uitslagenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleUitslagen();
			}
		});

		programmaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleProgramma();
			}
		});

		uitslagen = (ListView) mainView.findViewById(R.id.uitslagenList);
		programma = (ListView) mainView.findViewById(R.id.programmaList);

		photoPager = (ViewPager) mainView.findViewById(R.id.photoPager);
		table = (ListView) mainView.findViewById(R.id.tableListView);

		fm = ((FragmentActivity) activity).getSupportFragmentManager();
		spa = new SectionsPagerAdapter(fm);

		photoPager.setAdapter(spa);
		
		ArrayList<TableRow> tableList = team.getTable();
		Collections.sort(tableList);

		if (tableAdapter == null) tableAdapter = new TableAdapter(activity, tableList);

		table.setAdapter(tableAdapter);
		uitslagen.setAdapter(new FixtureAdapter(activity, team.getUitslagen()));
		programma.setAdapter(new FixtureAdapter(activity, team.getProgramma()));

		uitslagenContainer.setVisibility(View.GONE);
		programmaContainer.setVisibility(View.GONE);

		// table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT));

	}

	public void toggleUitslagen() {
		if (uitslagenContainer.getVisibility() == View.VISIBLE)
			uitslagenContainer.setVisibility(View.GONE);
		else
			uitslagenContainer.setVisibility(View.VISIBLE);
	}

	public void toggleProgramma() {
		if (programmaContainer.getVisibility() == View.VISIBLE)
			programmaContainer.setVisibility(View.GONE);
		else
			programmaContainer.setVisibility(View.VISIBLE);
	}

	public static class DummySectionFragment extends Fragment {

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			final ImageView view = (ImageView) inflater.inflate(R.layout.photo_item, null);
			String url = args.getString("url");
			new Loader() {
				protected void onPostExecute(Bitmap result) {
					if (result == null) return;
					view.setImageBitmap(result);
				};
			}.execute(url);
			return view;
		}
		
		private class Loader extends AsyncTask<String, Void, Bitmap> {
			@Override
			protected Bitmap doInBackground(String... params) {
				return Tools.getPictureFromUrl(params[0]);
			}
		}
	}
	
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putString("url", team.getPhotos().get(i).getUrl());
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return team.getPhotoCount();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}
	}

	
}

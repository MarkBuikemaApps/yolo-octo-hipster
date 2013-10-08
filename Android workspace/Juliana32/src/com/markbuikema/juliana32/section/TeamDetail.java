package com.markbuikema.juliana32.section;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.FixtureAdapter;
import com.markbuikema.juliana32.adapter.TableAdapter;
import com.markbuikema.juliana32.asynctask.PhotoSharer;
import com.markbuikema.juliana32.asynctask.PictureChanger;
import com.markbuikema.juliana32.model.Table;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.util.Util;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class TeamDetail {

	private static final String TAG = "TeamDetail";

	private Team team;
	private MainActivity act;

	private Button uitslagenButton;
	private Button programmaButton;

	private ViewPager photoPager;
	private ViewPager tablePager;

	private FragmentManager fm;
	private PhotoPagerAdapter ppa;
	private TablePagerAdapter tpa;

	private TitlePageIndicator tableTitle;
	private UnderlinePageIndicator photoIndicator;

	private ListView uitslagen;
	private ListView programma;

	private FrameLayout uitslagenContainer;
	private FrameLayout programmaContainer;

	private ArrayList<Table> tableList;

	private TextView noPhotos;

	private TextView noTables;

	public TeamDetail(MainActivity act, Team team) {
		this.team = team;
		this.act = act;

		View mainView = act.findViewById(R.id.teamDetailView);

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

		tableList = team.getTables();

		uitslagen = (ListView) mainView.findViewById(R.id.uitslagenList);
		programma = (ListView) mainView.findViewById(R.id.programmaList);

		photoPager = (ViewPager) mainView.findViewById(R.id.photoPager);
		tablePager = (ViewPager) mainView.findViewById(R.id.tablePager);

		noPhotos = (TextView) mainView.findViewById(R.id.noPhotosFound);
		noTables = (TextView) mainView.findViewById(R.id.noTablesFound);

		fm = ((FragmentActivity) act).getSupportFragmentManager();
		ppa = new PhotoPagerAdapter(act);
		tpa = new TablePagerAdapter(fm);

		photoPager.setAdapter(ppa);
		tablePager.setAdapter(tpa);

		noPhotos.setVisibility(ppa.getCount() < 1 ? View.VISIBLE : View.GONE);
		noTables.setVisibility(tpa.getCount() < 1 ? View.VISIBLE : View.GONE);

		uitslagen.setAdapter(new FixtureAdapter(act, team.getUitslagen()));
		programma.setAdapter(new FixtureAdapter(act, team.getProgramma()));

		closeClouds();

		uitslagenButton.setEnabled(uitslagen.getAdapter().getCount() > 0);
		programmaButton.setEnabled(programma.getAdapter().getCount() > 0);

		tableTitle = (TitlePageIndicator) mainView.findViewById(R.id.tablePagerTitle);
		tableTitle.setViewPager(tablePager);

		photoIndicator = (UnderlinePageIndicator) mainView.findViewById(R.id.photoIndicator);
		photoIndicator.setViewPager(photoPager);

		if (tpa.getCount() < 2)
			tableTitle.setVisibility(View.GONE);

		photoIndicator.invalidate();
		tableTitle.invalidate();

	}

	public boolean isACloudOpened() {
		return (uitslagenContainer.getVisibility() == View.VISIBLE || programmaContainer.getVisibility() == View.VISIBLE);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("teamId", team.getId());
		outState.putBoolean("uitslagenExpanded", uitslagenContainer.getVisibility() == View.VISIBLE);
		outState.putBoolean("programmaExpanded", programmaContainer.getVisibility() == View.VISIBLE);
		outState.putInt("tablePagerIndex", tablePager.getCurrentItem());
		outState.putInt("photoPagerIndex", photoPager.getCurrentItem());
	}

	public void onRestoreInstanceState(Bundle state) {
		uitslagenContainer.setVisibility(state.getBoolean("uitslagenExpanded", false) ? View.VISIBLE : View.GONE);
		programmaContainer.setVisibility(state.getBoolean("programmaExpanded", false) ? View.VISIBLE : View.GONE);
		tablePager.setCurrentItem(state.getInt("tablePagerIndex"));
		photoPager.setCurrentItem(state.getInt("photoPagerIndex"));
	}

	public void closeClouds() {
		uitslagenContainer.setVisibility(View.GONE);
		programmaContainer.setVisibility(View.GONE);

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

	public class PhotoPagerAdapter extends PagerAdapter {

		private Context context;

		public PhotoPagerAdapter(Context context) {
			this.context = context;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View mainView = LayoutInflater.from(context).inflate(R.layout.photo_item, null);
			final ImageView view = (ImageView) mainView.findViewById(R.id.photoView);
			final ImageButton shareButton = (ImageButton) mainView.findViewById(R.id.photoShareButton);
			shareButton.setVisibility(View.GONE);

			view.setClickable(true);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((MainActivity) context).showPhotoDialog(team.getPhotoUrls(), photoPager, position);
				}
			});

			String urlOrId = team.getPhotos().get(position);
			if (!urlOrId.startsWith("http"))
				urlOrId = Util.PHOTO_URL_PREFIX + urlOrId + Util.PHOTO_URL_SUFFIX;

			final String url = urlOrId;

			shareButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new PhotoSharer(context).execute(url, team.getName());
				}
			});
			new PictureChanger() {
				@Override
				protected void onPostExecute(Bitmap result) {
					if (result == null)
						return;
					view.setImageBitmap(result);
					shareButton.setVisibility(View.VISIBLE);
				};
			}.execute(url);

			((ViewPager) container).addView(mainView);

			return mainView;
		}

		@Override
		public int getCount() {
			return team.getPhotoCount();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return Integer.toString(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	public static class TableSectionFragment extends Fragment {

		public TableSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();

			final View mainView = inflater.inflate(R.layout.table_item, null);
			final ListView listView = (ListView) mainView.findViewById(R.id.tableList);

			Serializable t = args.getSerializable("table");
			if (t instanceof Table) {
				Table table = (Table) t;
				TableAdapter tableAdapter = new TableAdapter(getActivity(), table.getRows());
				listView.setAdapter(tableAdapter);
			}

			return mainView;
		}

	}

	public class TablePagerAdapter extends FragmentStatePagerAdapter {

		public TablePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new TableSectionFragment();
			Bundle args = new Bundle();
			args.putSerializable("table", tableList.get(i));

			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return team.getTables().size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return team.getTableName(position);
		}
	}
}

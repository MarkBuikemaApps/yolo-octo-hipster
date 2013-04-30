package com.markbuikema.juliana32.sections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.Table;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.tools.FixtureAdapter;
import com.markbuikema.juliana32.tools.TableAdapter;
import com.markbuikema.juliana32.tools.Tools;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class TeamDetail {

	private static final String TAG = "TeamDetail";

	private Team team;
	private MainActivity activity;

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

	public TeamDetail(MainActivity act, Team team) {
		this.team = team;
		activity = act;

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

		fm = ((FragmentActivity) act).getSupportFragmentManager();
		ppa = new PhotoPagerAdapter(fm);
		tpa = new TablePagerAdapter(fm);

		photoPager.setAdapter(ppa);
		tablePager.setAdapter(tpa);

		uitslagen.setAdapter(new FixtureAdapter(act, team.getUitslagen()));
		programma.setAdapter(new FixtureAdapter(act, team.getProgramma()));

		closeClouds();

		uitslagenButton.setEnabled(uitslagen.getAdapter().getCount() > 0);
		programmaButton.setEnabled(programma.getAdapter().getCount() > 0);

		tableTitle = (TitlePageIndicator) mainView.findViewById(R.id.tablePagerTitle);
		tableTitle.setViewPager(tablePager);

		photoIndicator = (UnderlinePageIndicator) mainView.findViewById(R.id.photoIndicator);
		photoIndicator.setViewPager(photoPager);

		if (tpa.getCount() < 2) tableTitle.setVisibility(View.GONE);

		photoIndicator.invalidate();
		tableTitle.invalidate();

	}

	public boolean isACloudOpened() {
		return (uitslagenContainer.getVisibility() == View.VISIBLE || programmaContainer.getVisibility() == View.VISIBLE);
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

	public static class PhotoSectionFragment extends Fragment {

		public PhotoSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			View mainView = inflater.inflate(R.layout.photo_item, null);
			final ImageView view = (ImageView) mainView.findViewById(R.id.photoView);
			final ImageButton shareButton = (ImageButton) mainView.findViewById(R.id.photoShareButton);
			shareButton.setVisibility(View.GONE);

			final String teamName = args.getString("teamname");
			final String url = args.getString("url");

			shareButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new PhotoSharer().execute(url, teamName);
				}
			});
			new Loader() {
				protected void onPostExecute(Bitmap result) {
					if (result == null) return;
					view.setImageBitmap(result);
					shareButton.setVisibility(View.VISIBLE);
				};
			}.execute(url);
			return mainView;
		}

		private class Loader extends AsyncTask<String, Void, Bitmap> {
			@Override
			protected Bitmap doInBackground(String... params) {
				return Tools.getPictureFromUrl(params[0]);
			}
		}

		private class PhotoSharer extends AsyncTask<String, Void, String> {

			private String teamName;

			@Override
			protected String doInBackground(String... params) {
				teamName = params[1];
				return cacheImage(Tools.getPhotoInputStreamFromUrl(params[0]), teamName);
			}

			@Override
			protected void onPostExecute(String result) {
				Intent share = new Intent(android.content.Intent.ACTION_SEND);
				share.setType("image/*");
				share.putExtra(Intent.EXTRA_SUBJECT, "Foto " + teamName);
				share.putExtra(Intent.EXTRA_TEXT, teamName);
				share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + result));
				try {
					startActivity(Intent.createChooser(share, getResources().getString(R.string.sharePhoto)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			private String cacheImage(InputStream is, String teamName) {
				String path = "";
				try {
					File cacheDir = getActivity().getExternalCacheDir();
					File downloadingMediaFile = new File(cacheDir, "Foto " + teamName + ".jpg");
					byte[] buf = new byte[256];
					FileOutputStream out = new FileOutputStream(downloadingMediaFile);
					while (true) {
						int rd = is.read(buf, 0, 256);
						if (rd == -1 || rd == 0) break;
						out.write(buf, 0, rd);
					}
					is.close();
					out.close();
					return downloadingMediaFile.getPath();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return path;
			}
		}
	}

	public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

		public PhotoPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new PhotoSectionFragment();
			Bundle args = new Bundle();
			args.putString("teamname", team.getName());
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
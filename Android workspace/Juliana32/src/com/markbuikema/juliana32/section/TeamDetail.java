package com.markbuikema.juliana32.section;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.widget.ViewPager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.FixtureAdapter;
import com.markbuikema.juliana32.adapter.TableAdapter;
import com.markbuikema.juliana32.asynctask.PhotoSharer;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.WebsiteNieuwsItem;
import com.markbuikema.juliana32.model.Table;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
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

	private PhotoPagerAdapter ppa;
	private TablePagerAdapter tpa;

	private TitlePageIndicator tableTitle;
	private UnderlinePageIndicator photoIndicator;

	private ListView uitslagen;
	private ListView programma;

	private FrameLayout uitslagenContainer;
	private FrameLayout programmaContainer;

	private List<Table> tableList;

	private TextView noPhotos;

	private TextView noTables;

	public TeamDetail(final MainActivity act, Team team) {
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

		tpa = new TablePagerAdapter();

		tablePager.setAdapter(tpa);

		noTables.setVisibility(tpa.getCount() < 1 ? View.VISIBLE : View.GONE);

		uitslagen.setAdapter(new FixtureAdapter(act, team.getUitslagen()));
		programma.setAdapter(new FixtureAdapter(act, team.getProgramma()));

		uitslagen.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				try {
					WebsiteNieuwsItem item = Util.findWedstrijdVerslag((Game) uitslagen.getAdapter().getItem(arg2));
					if (item == null)
						Toast.makeText(act, "Er is voor deze wedstrijd geen wedstrijdverslag gevonden", Toast.LENGTH_SHORT).show();
					else
						TeamDetail.this.act.requestNieuwsDetailPage(item, null);

				} catch (ClassCastException e) {

				}
			}
		});

		closeClouds();

		uitslagenButton.setEnabled(uitslagen.getAdapter().getCount() > 0);
		programmaButton.setEnabled(programma.getAdapter().getCount() > 0);

		tableTitle = (TitlePageIndicator) mainView.findViewById(R.id.tablePagerTitle);
		tableTitle.setViewPager(tablePager);

		if (tpa.getCount() < 2)
			tableTitle.setVisibility(View.GONE);

		tableTitle.invalidate();

		if (act.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			ppa = new PhotoPagerAdapter(act);
			photoPager.setAdapter(ppa);
			noPhotos.setVisibility(ppa.getCount() < 1 ? View.VISIBLE : View.GONE);
			photoIndicator = (UnderlinePageIndicator) mainView.findViewById(R.id.photoIndicator);
			photoIndicator.setViewPager(photoPager);
			photoIndicator.invalidate();

		}

	}

	public boolean isACloudOpened() {
		return (uitslagenContainer.getVisibility() == View.VISIBLE || programmaContainer.getVisibility() == View.VISIBLE);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("teamId", team.getId());
		outState.putBoolean("uitslagenExpanded", uitslagenContainer.getVisibility() == View.VISIBLE);
		outState.putBoolean("programmaExpanded", programmaContainer.getVisibility() == View.VISIBLE);
		outState.putInt("tablePagerIndex", tablePager.getCurrentItem());
		if (act.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE && photoPager != null)
			outState.putInt("photoPagerIndex", photoPager.getCurrentItem());
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		uitslagenContainer.setVisibility(savedInstanceState.getBoolean("uitslagenExpanded", false) ? View.VISIBLE
				: View.INVISIBLE);
		programmaContainer.setVisibility(savedInstanceState.getBoolean("programmaExpanded", false) ? View.VISIBLE
				: View.INVISIBLE);
		tablePager.setCurrentItem(savedInstanceState.getInt("tablePagerIndex"));
		if (act.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
			photoPager.setCurrentItem(savedInstanceState.getInt("photoPagerIndex"));
	}

	public void closeClouds() {
		uitslagenContainer.setVisibility(View.INVISIBLE);
		programmaContainer.setVisibility(View.INVISIBLE);

	}

	public void toggleUitslagen() {
		if (uitslagenContainer.getVisibility() == View.VISIBLE)
			uitslagenContainer.setVisibility(View.INVISIBLE);
		else
			uitslagenContainer.setVisibility(View.VISIBLE);
	}

	public void toggleProgramma() {
		if (programmaContainer.getVisibility() == View.VISIBLE)
			programmaContainer.setVisibility(View.INVISIBLE);
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
					((MainActivity) context).showPhotoDialog(team.getPhotoUrls(), position,
							new OnPhotoPagerDialogPageChangedListener() {

								@Override
								public void onPhotoPagerDialogPageChanged(int pageIndex) {
									photoPager.setCurrentItem(pageIndex, true);
								}
							});
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

			UrlImageViewHelper.setUrlDrawable(view, url, new UrlImageViewCallback() {

				@Override
				public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
					shareButton.setVisibility(View.VISIBLE);
				}
			});

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

	public class TablePagerAdapter extends PagerAdapter {

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewPager vp = (ViewPager) container;
			final View mainView = LayoutInflater.from(act).inflate(R.layout.table_item, null);
			final ListView listView = (ListView) mainView.findViewById(R.id.tableList);

			Table table = tableList.get(position);
			TableAdapter tableAdapter = new TableAdapter(act, table.getRows());
			listView.setAdapter(tableAdapter);

			vp.addView(mainView);

			return mainView;
		}

		@Override
		public int getCount() {
			return team.getTables().size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return team.getTableName(position);
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
}

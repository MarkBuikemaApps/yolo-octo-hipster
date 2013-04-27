package com.markbuikema.juliana32.activities;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.coboltforge.slidemenu.SlideMenu;
import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.sections.Home;
import com.markbuikema.juliana32.sections.Nieuws;
import com.markbuikema.juliana32.sections.NieuwsDetail;
import com.markbuikema.juliana32.sections.TeamDetail;
import com.markbuikema.juliana32.sections.Teams;
import com.markbuikema.juliana32.sections.Teletekst;
import com.markbuikema.juliana32.service.NotificationService;
import com.markbuikema.juliana32.ui.Button;

public class MainActivity extends FragmentActivity implements OnSlideMenuItemClickListener {

	public static final String BASE_SERVER_URL = "http://192.168.1.254:8080/JulianaServer/api";
	public static final int NOTIFICATION_INTERVAL = 1;// minutes

	private SlideMenu menu;
	private ImageButton menuToggler;
	private ImageButton overflowToggler;
	private ImageButton refreshButton;
	private ImageButton shareButton;
	private Spinner seasonButton;
	private ProgressBar loader;
	private TextView title;

	private View activePageView;
	private View teamDetailView;
	private View nieuwsDetailView;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;

	private Home home;
	private Teletekst teletekst;
	private Nieuws nieuws;
	private Teams teams;

	public Page page;
	private boolean showAbout = false;

	public enum Page {
		HOME, NIEUWS, TEAMS, TELETEKST
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		menu = (SlideMenu) findViewById(R.id.slideMenu1);
		teamDetailView = findViewById(R.id.teamDetailView);
		nieuwsDetailView = findViewById(R.id.nieuwsDetailView);
		menuToggler = (ImageButton) findViewById(R.id.menuToggler);
		overflowToggler = (ImageButton) findViewById(R.id.menuToggler2);
		shareButton = (ImageButton) findViewById(R.id.menuShare);
		title = (TextView) findViewById(R.id.titleText);
		refreshButton = (ImageButton) findViewById(R.id.menuRefresh);
		seasonButton = (Spinner) findViewById(R.id.menuSeason);
		loader = (ProgressBar) findViewById(R.id.loading);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			if (ViewConfiguration.get(this).hasPermanentMenuKey()) overflowToggler.setVisibility(View.GONE);

		
		shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 final Intent intent = new Intent(Intent.ACTION_SEND);

		     intent.setType("text/plain");
		     intent.putExtra(Intent.EXTRA_SUBJECT, nieuwsDetail.getTitle());
		     intent.putExtra(Intent.EXTRA_TEXT, nieuwsDetail.getDetailUrl());

		     startActivity(Intent.createChooser(intent, getString(R.string.share)));
			}
		});
		
		initializePages();

		onPageChanged(Page.HOME);

		menu.init(this, R.menu.slide, this, 333);
		menuToggler.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				menu.show();
			}

		});

		overflowToggler.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});

	}

	public ArrayList<Game> getLatestGames() {
		if (teams != null)
			return teams.getLatestGames();
		else
			return new ArrayList<Game>();
	}

	public Teams getTeams() {
		return teams;
	}

	public void notifyDoneLoadingSeasons() {
		if (home == null) return;
		home.populateGames();
	}

	private void initializePages() {
		for (Page page : Page.values()) {
			onPageChanged(page);
		}
	}

	public void onPageChanged(Page page) {
		this.page = page;

		if (activePageView != null) {
			activePageView.setVisibility(View.GONE);
		}

		String title = getResources().getString(R.string.app_name);
		switch (page) {
		case HOME:
			title = getResources().getString(R.string.app_name);
			activePageView = findViewById(R.id.homeView);
			if (home == null) home = new Home(this);
			break;
		case NIEUWS:
			title = getResources().getString(R.string.menu_nieuws);
			activePageView = findViewById(R.id.nieuwsView);
			if (nieuws == null) nieuws = new Nieuws(this);
			nieuws.showRefreshButton();
			break;
		case TEAMS:
			title = getResources().getString(R.string.menu_teams);
			activePageView = findViewById(R.id.teamsView);
			if (teams == null) teams = new Teams(this);
			if (!teams.isLoaded()) {
				loader.setVisibility(View.VISIBLE);
			}
			break;
		case TELETEKST:
			title = getResources().getString(R.string.menu_teletekst);
			activePageView = findViewById(R.id.teletekstView);
			if (teletekst == null) teletekst = new Teletekst(this);
			break;
		}

		teamDetailView.setVisibility(View.GONE);
		teamDetail = null;

		hideNieuwsDetail();

		loader.setVisibility(View.GONE);

		seasonButton.setVisibility(page == Page.TEAMS ? View.VISIBLE : View.GONE);
		refreshButton.setVisibility(page == Page.NIEUWS ? View.VISIBLE : View.GONE);

		activePageView.setVisibility(View.VISIBLE);

		setTitle(title);

	}

	public void requestTeamDetailPage(Team team) {
		if (page != Page.TEAMS || isTeamDetailShown()) return;

		teamDetail = new TeamDetail(this, team);

		activePageView.setVisibility(View.GONE);
		teamDetailView.setVisibility(View.VISIBLE);

		setTitle(team.getName());

		
	}

	public void requestNiewsDetailPage(NieuwsItem item) {
		if (page != Page.NIEUWS || isNieuwsDetailShown()) return;

		nieuwsDetail = new NieuwsDetail(this, item);

		activePageView.setVisibility(View.GONE);
		nieuwsDetailView.setVisibility(View.VISIBLE);
		
		shareButton.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.GONE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShown())
			menu.hide();
		else if (isTeamDetailShown()) {
			if (teamDetail.isACloudOpened()) {
				teamDetail.closeClouds();
			} else {
				teamDetailView.setVisibility(View.GONE);
				activePageView.setVisibility(View.VISIBLE);
				teamDetail = null;
				setTitle(R.string.menu_teams);
			}
		} else if (isNieuwsDetailShown()) {
			hideNieuwsDetail();
		} else
			super.onBackPressed();
	}

	public void hideNieuwsDetail() {
		nieuwsDetailView.setVisibility(View.GONE);
		activePageView.setVisibility(View.VISIBLE);
		nieuwsDetail = null;
		shareButton.setVisibility(View.GONE);
		if (page == Page.NIEUWS) {
			refreshButton.setVisibility(View.VISIBLE);
		}
	}

	private boolean isTeamDetailShown() {
		return teamDetailView.getVisibility() == View.VISIBLE;
	}

	private boolean isNieuwsDetailShown() {
		return nieuwsDetailView.getVisibility() == View.VISIBLE;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		case R.id.menu_about:
			showAboutDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	private void showAboutDialog() {

		showAbout = true;

		Builder b;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			b = new Builder(this);
		} else {
			b = new Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		}
		View view = LayoutInflater.from(this).inflate(R.layout.about, null);
		Button emailButton = (Button) view.findViewById(R.id.aboutEmail);

		final AlertDialog ad = b.setView(view).create();
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
				String uriString = "mailto:" + getString(R.string.about_email) + "?subject="
						+ getString(R.string.about_email_subject);
				uriString = uriString.replaceAll(" ", "%20");
				emailIntent.setData(Uri.parse(uriString));
				startActivity(Intent.createChooser(emailIntent, "Email versturen"));

				ad.dismiss();

			}
		});
		ad.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				showAbout = false;
			}
		});
		ad.show();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (page == Page.NIEUWS && nieuws != null && !isNieuwsDetailShown()) {
			nieuws.refresh();
		}

		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFERENCES, 0);
		boolean notify = prefs.getBoolean(SettingsActivity.NOTIFICATIONS, false);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, NotificationService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		am.cancel(pi);

		if (notify) {
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + NOTIFICATION_INTERVAL * 6
					* 1000, NOTIFICATION_INTERVAL * 60 * 1000, pi);
			Log.d("JulianaService", "Service started");
		}
	}

	@Override
	public void setTitle(int resid) {
		title.setText(resid);
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title.setText(title);
	}

	@Override
	public void onSlideMenuItemClick(int itemId) {

		switch (itemId) {
		case R.id.menu_home:
			onPageChanged(Page.HOME);
			break;
		case R.id.menu_nieuws:
			onPageChanged(Page.NIEUWS);
			break;
		case R.id.menu_teams:
			onPageChanged(Page.TEAMS);
			break;
		case R.id.menu_teletekst:
			onPageChanged(Page.TELETEKST);
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putString("page", page.toString());
		outState.putBoolean("showAbout", showAbout);

		if (teletekst != null) teletekst.onSaveInstanceState(outState);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		initializePages();

		if (teletekst != null) teletekst.onRestoreInstanceState(savedInstanceState);
		onPageChanged(Page.valueOf(savedInstanceState.getString("page")));
		showAbout = savedInstanceState.getBoolean("showAbout");
		if (showAbout) showAboutDialog();

		super.onRestoreInstanceState(savedInstanceState);
	}

	public static String filter(String input) {
		return android.text.Html.fromHtml(input).toString();
	}

	public Page getPage() {
		return page;
	}
}

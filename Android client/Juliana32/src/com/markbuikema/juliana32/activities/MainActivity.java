package com.markbuikema.juliana32.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.simonvt.menudrawer.MenuDrawer;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends FragmentActivity {

	public static final String BASE_SERVER_URL = "http://192.168.1.213:8080/JulianaServer/api";
	public static final int NOTIFICATION_INTERVAL = 1;// minutes
	private static final String TAG = "JulianaActivity";
	private static final long SECOND_BACK_PRESS_TIMEOUT = 3000;

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
	private View failurePage;

	private TextView failureText;
	private ProgressBar failureLoader;
	private Button failureButton;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;

	private Home home;
	private Teletekst teletekst;
	private Nieuws nieuws;
	private Teams teams;

	private MenuDrawer drawer;
	private ListView menu;
	private MenuAdapter menuAdapter;

	public Page page;
	private boolean showAbout = false;
	private boolean waitingForSecondBackPress = false;

	public enum Page {
		HOME, NIEUWS, TEAMS, TELETEKST
	}

	public enum FailureReason {
		NO_INTERNET, SERVER_OFFLINE, UNKNOWN
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		drawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		drawer.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
		drawer.setContentView(R.layout.activity_main);
		drawer.setMenuView(R.layout.menu_main);

		menu = (ListView) findViewById(R.id.menuDrawer);
		menuAdapter = new MenuAdapter(this);
		menu.setAdapter(menuAdapter);
		menu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onPageChanged(Page.values()[arg2]);
				drawer.toggleMenu();
			}
		});

		teamDetailView = findViewById(R.id.teamDetailView);
		nieuwsDetailView = findViewById(R.id.nieuwsDetailView);
		menuToggler = (ImageButton) findViewById(R.id.menuToggler);
		overflowToggler = (ImageButton) findViewById(R.id.menuToggler2);
		shareButton = (ImageButton) findViewById(R.id.menuShare);
		title = (TextView) findViewById(R.id.titleText);
		refreshButton = (ImageButton) findViewById(R.id.menuRefresh);
		seasonButton = (Spinner) findViewById(R.id.menuSeason);
		loader = (ProgressBar) findViewById(R.id.loading);
		failurePage = findViewById(R.id.failurePage);
		failureLoader = (ProgressBar) failurePage.findViewById(R.id.failureLoader);
		failureText = (TextView) failurePage.findViewById(R.id.failureText);
		failureButton = (Button) failurePage.findViewById(R.id.failureButton);

		menuToggler.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isNieuwsDetailShown())
					hideNieuwsDetail();
				else if (isTeamDetailShown())
					hideNieuwsDetail();
				else
					drawer.toggleMenu();
			}
		});

		failureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				retry();
			}
		});
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

		overflowToggler.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});

		if (getIntent().getBooleanExtra(NotificationService.FROM_NOTIFICATION, false)) {
			onPageChanged(Page.NIEUWS);
		}
		int newsId = getIntent().getIntExtra(NotificationService.NEWS_ID, -1);
		if (newsId != -1) {
			nieuws.setItemRequest(newsId);

		}

	}

	public MenuDrawer getMenu() {
		return drawer;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
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

		activePageView.setVisibility(View.VISIBLE);

		fixActionBar();

		if (isFailurePageShown()) {
			title = getResources().getString(R.string.failure);
		}
		setTitle(title);

		if (menuAdapter != null) menuAdapter.notifyDataSetChanged();
	}

	public void retry() {
		failureButton.setVisibility(View.INVISIBLE);
		failureLoader.setVisibility(View.VISIBLE);
		checkNetworkStatus();
		teams.reload();
	}

	public void checkNetworkStatus() {
		Log.d(TAG, "online: " + isOnline());
		if (!isOnline()) {

			requestFailurePage(FailureReason.NO_INTERNET);

			Log.d(TAG, "NO INTERNET ACCESS");
		} else {
			hideFailurePage();
		}
	}

	public void fixActionBar() {
		if (activePageView.getVisibility() == View.VISIBLE) switch (page) {
		case HOME:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			seasonButton.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			break;
		case NIEUWS:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(isNieuwsDetailShown() ? View.GONE : View.VISIBLE);
			seasonButton.setVisibility(View.GONE);
			shareButton.setVisibility(isNieuwsDetailShown() ? View.VISIBLE : View.GONE);
			break;
		case TEAMS:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			seasonButton.setVisibility(isTeamDetailShown() ? View.GONE : View.VISIBLE);
			break;
		case TELETEKST:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			seasonButton.setVisibility(View.GONE);
			break;
		}
		if (isFailurePageShown()) {
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			seasonButton.setVisibility(View.GONE);
			menuToggler.setEnabled(false);
		} else {
			menuToggler.setEnabled(true);
		}

	}

	public void requestFailurePage(FailureReason reason) {

		failureLoader.setVisibility(View.INVISIBLE);
		failureButton.setVisibility(View.VISIBLE);

		activePageView.setVisibility(View.GONE);
		failurePage.setVisibility(View.VISIBLE);

		switch (reason) {
		case NO_INTERNET:
			failureText.setText(R.string.no_internet);
			break;
		case SERVER_OFFLINE:
			failureText.setText(R.string.server_offline);
			break;
		case UNKNOWN:
			failureText.setText(R.string.failuretext);
		}

		fixActionBar();
	}

	public void hideFailurePage() {
		failureLoader.setVisibility(View.INVISIBLE);
		failureButton.setVisibility(View.VISIBLE);
		activePageView.setVisibility(View.VISIBLE);
		failurePage.setVisibility(View.GONE);

		fixActionBar();
	}

	public boolean isFailurePageShown() {
		return failurePage.getVisibility() == View.VISIBLE;
	}

	public void requestTeamDetailPage(Team team) {
		if (page != Page.TEAMS || isTeamDetailShown()) return;

		teamDetail = new TeamDetail(this, team);

		seasonButton.setVisibility(View.GONE);

		activePageView.setVisibility(View.GONE);
		teamDetailView.setVisibility(View.VISIBLE);

		setTitle(team.getName());

	}

	public void requestNiewsDetailPage(NieuwsItem item) {
		if ((page != Page.NIEUWS && page != Page.HOME) || isNieuwsDetailShown()) return;

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
		if (drawer.isMenuVisible()) {
			drawer.toggleMenu();
			return;
		}
		if (isTeamDetailShown()) {
			if (teamDetail.isACloudOpened()) {
				teamDetail.closeClouds();
			} else {
				hideTeamDetail();
				setTitle(R.string.menu_teams);
			}
		} else if (isNieuwsDetailShown()) {
			hideNieuwsDetail();
		} else if (waitingForSecondBackPress)
			super.onBackPressed();
		else startBackPressTimer();
	}
	
	
	private void startBackPressTimer() {
		waitingForSecondBackPress = true;
		Toast.makeText(this, getResources().getString(R.string.second_back_press), Toast.LENGTH_LONG).show();
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				waitingForSecondBackPress = false;
			}
			
		}, SECOND_BACK_PRESS_TIMEOUT);
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

	public void hideTeamDetail() {
		teamDetailView.setVisibility(View.GONE);
		activePageView.setVisibility(View.VISIBLE);
		teamDetail = null;
		if (page == Page.TEAMS) {
			seasonButton.setVisibility(View.VISIBLE);
		}
	}

	public boolean isTeamDetailShown() {
		return teamDetailView.getVisibility() == View.VISIBLE;
	}

	public boolean isNieuwsDetailShown() {
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

		checkNetworkStatus();

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

	private class MenuAdapter extends ArrayAdapter<String> {

		public MenuAdapter(Context context) {
			super(context, 0, context.getResources().getStringArray(R.array.menu_items));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_menu, null);

			TextView text = (TextView) convertView.findViewById(R.id.menuItemTitle);
			View indicator = convertView.findViewById(R.id.menuItemIndicator);
			text.setText(getItem(position));
			indicator.setBackgroundResource(page == Page.values()[position] ? R.drawable.listitem_arrow
					: R.drawable.listitem_background);

			return convertView;
		}

	}
}

package com.markbuikema.juliana32.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;
import net.simonvt.menudrawer.MenuDrawer.Type;
import net.simonvt.menudrawer.Position;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.section.Nieuws;
import com.markbuikema.juliana32.section.NieuwsDetail;
import com.markbuikema.juliana32.section.TeamDetail;
import com.markbuikema.juliana32.section.Teams;
import com.markbuikema.juliana32.section.Teletekst;
import com.markbuikema.juliana32.ui.Button;
import com.markbuikema.juliana32.ui.PhotoPagerDialog;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper;
import com.markbuikema.juliana32.util.Util;

public class MainActivity extends Activity {

	public static final boolean OFFLINE_MODE = true;

	public static final int NOTIFICATION_INTERVAL = 1;// minutes
	private static final String TAG = "JulianaActivity";
	private static final long SECOND_BACK_PRESS_TIMEOUT = 3000;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private ImageView menuButton;
	private LinearLayout menuToggler;
	private ImageButton refreshButton;
	private ImageButton commentsButton;
	private ImageButton shareButton;
	private ImageButton picturesButton;
	private ImageButton searchButton;
	private Spinner seasonButton;
	private ProgressBar loader;
	private TextView title;

	private LinearLayout actionBarContent;
	private EditText searchInput;

	private View activePageView;
	private View teamDetailView;
	private View nieuwsDetailView;

	private ImageButton facebookButton;
	private ImageButton twitterButton;
	private ImageButton prefsButton;
	private ImageButton helpButton;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;

	private Teletekst teletekst;
	private Nieuws nieuws;
	private Teams teams;

	private PhotoPagerDialog photoDialog;

	private MenuDrawer menuDrawer;
	private ListView menu;
	private MenuAdapter menuAdapter;

	public Page page;
	private boolean showAbout = false;
	private boolean waitingForSecondBackPress = false;

	protected String userName;
	protected String userPicUrl;
	protected String userId;

	private boolean searching;

	public enum Page {
		NIEUWS, TEAMS, TELETEKST
	}

	public enum FailureReason {
		NO_INTERNET, SERVER_OFFLINE, UNKNOWN
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logHashKey();

		Util.onOrientationChanged(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

		menuDrawer = MenuDrawer.attach(this, Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
		menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
		menuDrawer.setContentView(R.layout.activity_main);
		menuDrawer.setMenuView(R.layout.menu_main);

		menuDrawer.setOnDrawerStateChangeListener(new OnDrawerStateChangeListener() {

			@Override
			public void onDrawerStateChange(int oldState, int newState) {
				fixActionBar();
			}

			@Override
			public void onDrawerSlide(float openRatio, int offsetPixels) {
			}
		});

		menu = (ListView) findViewById(R.id.menuDrawer);
		menuAdapter = new MenuAdapter(this);

		Drawable gradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] {
				getResources().getColor(R.color.grey), getResources().getColor(R.color.red)
		});

		menu.setDivider(gradient);
		menu.setDividerHeight(1);
		menu.setSelector(R.drawable.listselector);
		menu.setAdapter(menuAdapter);
		menu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onPageChanged(Page.values()[arg2]);
				menuDrawer.toggleMenu();

				if (arg2 == 1)
					showBetaDialogIfNecessary();
			}
		});

		teamDetailView = findViewById(R.id.teamDetailView);
		nieuwsDetailView = findViewById(R.id.nieuwsDetailView);
		menuButton = (ImageView) findViewById(R.id.menuButton);
		menuToggler = (LinearLayout) findViewById(R.id.menuToggler);
		commentsButton = (ImageButton) findViewById(R.id.menuComments);
		shareButton = (ImageButton) findViewById(R.id.menuShare);
		picturesButton = (ImageButton) findViewById(R.id.menuPictures);
		facebookButton = (ImageButton) findViewById(R.id.menuFacebook);
		twitterButton = (ImageButton) findViewById(R.id.menuTwitter);
		prefsButton = (ImageButton) findViewById(R.id.menuPrefs);
		helpButton = (ImageButton) findViewById(R.id.menuHelp);
		title = (TextView) findViewById(R.id.titleText);
		refreshButton = (ImageButton) findViewById(R.id.menuRefresh);
		seasonButton = (Spinner) findViewById(R.id.menuSeason);
		loader = (ProgressBar) findViewById(R.id.loading);
		actionBarContent = (LinearLayout) findViewById(R.id.actionBarContent);
		searchButton = (ImageButton) findViewById(R.id.menuSearch);
		searchInput = (EditText) findViewById(R.id.searchInput);

		menuToggler.setClickable(true);
		menuToggler.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isTeamDetailShown() || isNieuwsDetailShown() || isSearchBarShown())
					onBackPressed();
				else
					menuDrawer.toggleMenu();

			}
		});

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

		picturesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				nieuwsDetail.showPhotos();
			}
		});

		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestSearchBar();
				searchInput.setText("");
			}
		});

		searchInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				nieuws.search(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		facebookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				menuDrawer.toggleMenu();

				try {
					getPackageManager().getPackageInfo("com.facebook.katana", 0);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/294105307313875")));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/svjuliana32")));
				}
			}
		});
		twitterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				menuDrawer.toggleMenu();

				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=svjuliana32"));
					startActivity(intent);
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/svjuliana32")));
				}
			}
		});

		prefsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menuDrawer.toggleMenu();
				Intent i = new Intent(MainActivity.this, SettingsActivity.class);
				i.putExtra("userName", userName);
				i.putExtra("userPicUrl", userPicUrl);
				startActivity(i);
			}
		});
		helpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menuDrawer.toggleMenu();
				showAboutDialog();
			}
		});

		initializePages();

		onPageChanged(Page.NIEUWS);

		// FACEBOOK STUFF////////////////
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null)
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			if (session == null)
				session = new Session(this);
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED))
				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		}

		updateView();

		// RESTORE ACTIVITY STATE ////////////////////
		if (savedInstanceState != null) {

			if (teletekst != null)
				teletekst.onRestoreInstanceState(savedInstanceState);
			onPageChanged(Page.valueOf(savedInstanceState.getString("page")));
			showAbout = savedInstanceState.getBoolean("showAbout");
			if (showAbout)
				showAboutDialog();

			int teamId = savedInstanceState.getInt("teamId", -1);
			if (teamId > -1) {
				Team team = findTeamById(teamId);
				requestTeamDetailPage(team);
				teamDetail.onRestoreInstanceState(savedInstanceState);
			}

			int nieuwsId = savedInstanceState.getInt("nieuwsId", -1);
			if (nieuwsId > -1)
				for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
					if (item.getId() == nieuwsId) {
						requestNieuwsDetailPage(item);
						break;
					}

			int photoDialogPage = savedInstanceState.getInt("photoDialogPage", -1);
			if (photoDialogPage > -1) {
				String[] urls = savedInstanceState.getStringArray("photoDialogUrls");
				showPhotoDialog(urls, photoDialogPage, null);
			}

			if (savedInstanceState.getBoolean("menuOpened"))
				menuDrawer.toggleMenu();

			searching = savedInstanceState.getBoolean("searching", false);

			if (page == Page.NIEUWS && searching && !isNieuwsDetailShown())
				requestSearchBar();

			if (searching)
				searchInput.setText(savedInstanceState.getString("searchWord"));

		}
	}

	protected boolean isSearchBarShown() {
		return searchInput.getVisibility() == View.VISIBLE;
	}

	private void updateView() {

		Session session = Session.getActiveSession();

		// String text = "Session opened: " + session.isOpened() +
		// "\nAccess token: " + session.getAccessToken() + "\nPERMISSIONS:";
		//
		// for (String string : session.getPermissions())
		// text += "\n" + string;
		//
		// Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

		Log.d("ACCESS TOKEN", "Token: " + session.getAccessToken());

	}

	public MenuDrawer getMenu() {
		return menuDrawer;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
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

	private void initializePages() {
		for (Page page : Page.values())
			onPageChanged(page);
	}

	public void onPageChanged(Page page) {
		if (this.page == Page.NIEUWS && searching) {
			searching = false;
			hideSearchBar();
			fixActionBar();
		}

		this.page = page;

		if (activePageView != null)
			activePageView.setVisibility(View.GONE);

		String title = getResources().getString(R.string.app_name);
		switch (page) {

		case NIEUWS:
			title = getResources().getString(R.string.menu_nieuws);
			activePageView = findViewById(R.id.nieuwsView);
			if (nieuws == null)
				nieuws = new Nieuws(this);
			nieuws.showRefreshButton();
			nieuws.clearSearch();
			break;
		case TEAMS:
			title = getResources().getString(R.string.menu_teams);
			activePageView = findViewById(R.id.teamsView);
			if (teams == null)
				teams = new Teams(this);
			if (!teams.isLoaded())
				loader.setVisibility(View.VISIBLE);
			break;
		case TELETEKST:
			title = getResources().getString(R.string.menu_teletekst);
			activePageView = findViewById(R.id.teletekstView);
			if (teletekst == null)
				teletekst = new Teletekst(this);
			break;
		}

		teamDetailView.setVisibility(View.GONE);
		teamDetail = null;

		if (isNieuwsDetailShown())
			nieuwsDetail.hideComments();

		hideNieuwsDetail();

		loader.setVisibility(View.GONE);

		activePageView.setVisibility(View.VISIBLE);

		setTitle(title);

		if (menuAdapter != null)
			menuAdapter.notifyDataSetChanged();

		fixActionBar();

	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void showBetaDialogIfNecessary() {

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!prefs.getBoolean("showTeamsMessage", true))
			return;

		final Editor edit = prefs.edit();

		View view = LayoutInflater.from(this).inflate(R.layout.tempdialog, null);
		final CheckBox doNotShowAgain = (CheckBox) view.findViewById(R.id.do_not_show_again);
		final Button close = (Button) view.findViewById(R.id.posbutton);

		Builder b;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			b = new Builder(this);
		else
			b = new Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

		final AlertDialog dialog = b.setView(view).create();
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (doNotShowAgain.isChecked()) {
					edit.putBoolean("showTeamsMessage", false);
					edit.commit();
				}
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	public void fixActionBar() {
		switch (page) {

		case NIEUWS:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(isNieuwsDetailShown() ? View.GONE : View.VISIBLE);
			seasonButton.setVisibility(View.GONE);
			shareButton.setVisibility(isNieuwsDetailShown() ? View.VISIBLE : View.GONE);
			picturesButton.setVisibility(isNieuwsDetailShown() && currentNewsItemHasPhotos() ? View.VISIBLE : View.GONE);
			searchButton.setVisibility(isNieuwsDetailShown() ? View.GONE : View.VISIBLE);

			break;
		case TEAMS:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			seasonButton.setVisibility(isTeamDetailShown() ? View.GONE : View.VISIBLE);
			picturesButton.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);

			break;
		case TELETEKST:
			loader.setVisibility(View.GONE);
			refreshButton.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			seasonButton.setVisibility(View.GONE);
			picturesButton.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);

			break;
		}

		if (isNieuwsDetailShown()) {
			commentsButton.setVisibility(currentNewsItemHasComments() ? View.VISIBLE : View.GONE);
			hideSearchBar();
		} else
			commentsButton.setVisibility(View.GONE);

		if (isTeamDetailShown() || isNieuwsDetailShown() || searching)
			menuButton.setImageResource(R.drawable.menu_borderless);
		else
			if (menuDrawer.isMenuVisible())
				menuButton.setImageResource(R.drawable.menu_slid_borderless);
			else
				menuButton.setImageResource(R.drawable.menu_slide_borderless);

		if (page == Page.NIEUWS && searching)
			if (!isNieuwsDetailShown()) {
				requestSearchBar();
				menuButton.setImageResource(R.drawable.menu_borderless);
			}
	}

	private boolean currentNewsItemHasComments() {
		if (nieuwsDetail == null)
			return false;
		return nieuwsDetail.hasComments();
	}

	private boolean currentNewsItemHasPhotos() {
		if (nieuwsDetail == null)
			return false;
		return nieuwsDetail.hasPhotos();
	}

	public void requestTeamDetailPage(Team team) {
		if (page != Page.TEAMS || isTeamDetailShown())
			return;

		teamDetail = new TeamDetail(this, team);

		activePageView.setVisibility(View.GONE);
		teamDetailView.setVisibility(View.VISIBLE);

		setTitle(team.getName());

		fixActionBar();
	}

	public void requestNieuwsDetailPage(final NieuwsItem item) {
		if ((page != Page.NIEUWS && page != Page.TEAMS) || isNieuwsDetailShown())
			return;

		if (item instanceof NormalNieuwsItem) {
			NormalNieuwsItem nni = (NormalNieuwsItem) item;
			if (!nni.isContentLoaded()) {
				nni.startLoading(new OnContentLoadedListener() {

					@Override
					public void onContentLoaded(String content, List<String> photos) {
						requestNieuwsDetailPage(item);
					}
				});
				return;
			}
		}

		nieuwsDetail = new NieuwsDetail(this, item);
		saveGraphUser();

		activePageView.setVisibility(View.GONE);
		nieuwsDetailView.setVisibility(View.VISIBLE);

		hideSearchBar();
		fixActionBar();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		menuDrawer.toggleMenu();
		return true;
	}

	@Override
	public void onBackPressed() {
		if (isPhotoDialogShown()) {
			hidePhotoDialog();
			return;
		}
		if (menuDrawer.isMenuVisible()) {
			menuDrawer.toggleMenu();
			return;
		}
		if (isNieuwsDetailShown() && nieuwsDetail.isCommentsPanelOpened()) {
			nieuwsDetail.hideComments();
			return;
		}
		if (isSearchBarShown()) {
			hideSearchBar();
			nieuws.clearSearch();
			searching = false;
			fixActionBar();
			return;
		}
		if (isNieuwsDetailShown())
			hideNieuwsDetail();
		else
			if (isTeamDetailShown()) {
				if (teamDetail.isACloudOpened())
					teamDetail.closeClouds();
				else {
					hideTeamDetail();
					setTitle(R.string.menu_teams);
				}
			} else
				if (waitingForSecondBackPress) {
					DataManager.getInstance().clearData();
					finish();
				} else
					startBackPressTimer();
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
		if (nieuwsDetail != null)
			nieuwsDetail.cancelTasks();
		nieuwsDetailView.setVisibility(View.GONE);
		activePageView.setVisibility(View.VISIBLE);
		nieuwsDetail = null;

		fixActionBar();
	}

	public void hideTeamDetail() {
		teamDetailView.setVisibility(View.GONE);
		activePageView.setVisibility(View.VISIBLE);
		teamDetail = null;

		fixActionBar();
	}

	public boolean isTeamDetailShown() {
		return teamDetailView.getVisibility() == View.VISIBLE;
	}

	public boolean isNieuwsDetailShown() {
		return nieuwsDetailView.getVisibility() == View.VISIBLE;
	}

	public void showAndClickCommentsButton() {
		if (!isNieuwsDetailShown())
			return;
		commentsButton.setVisibility(View.VISIBLE);
		commentsButton.performClick();
	}

	@SuppressLint("NewApi")
	private void showAboutDialog() {

		showAbout = true;

		Builder b;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			b = new Builder(this);
		else
			b = new Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
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

		saveGraphUser();

		if (isNieuwsDetailShown())
			nieuwsDetail.onResume();

	}

	@Override
	public void setTitle(int resid) {
		title.setText(resid);
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title.setText(title);
	}

	public void requestSearchBar() {
		if (actionBarContent.getVisibility() != View.VISIBLE)
			return;

		actionBarContent.setVisibility(View.GONE);
		searchInput.setVisibility(View.VISIBLE);
		searchInput.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInputFromWindow(searchInput.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

		searching = true;

		title.setVisibility(View.GONE);
		menuButton.setImageResource(R.drawable.menu_borderless);

	}

	public void hideSearchBar() {
		if (actionBarContent.getVisibility() != View.GONE)
			return;

		actionBarContent.setVisibility(View.VISIBLE);
		searchInput.setVisibility(View.GONE);
		title.setVisibility(View.VISIBLE);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putString("page", page.toString());
		outState.putBoolean("showAbout", showAbout);

		if (isTeamDetailShown())
			teamDetail.onSaveInstanceState(outState);

		if (teletekst != null)
			teletekst.onSaveInstanceState(outState);

		if (isPhotoDialogShown())
			photoDialog.onSaveInstanceState(outState);

		if (isNieuwsDetailShown())
			nieuwsDetail.onSaveInstanceState(outState);

		outState.putBoolean("menuOpened", menuDrawer.isMenuVisible());

		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);

		outState.putString("searchWord", searchInput.getText().toString());
		outState.putBoolean("searching", searching);

		super.onSaveInstanceState(outState);
	}

	private Team findTeamById(int teamId) {
		for (Season season : DataManager.getInstance().getTeams())
			for (Team t : season.getTeams())
				if (t.getId() == teamId)
					return t;
		return null;
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
			if (convertView == null)
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_menu, null);

			TextView text = (TextView) convertView.findViewById(R.id.menuItemTitle);
			View indicator = convertView.findViewById(R.id.menuItemIndicator);
			text.setText(getItem(position));
			indicator.setBackgroundResource(page == Page.values()[position] ? R.drawable.listitem_arrow
					: R.drawable.listitem_background);

			return convertView;
		}

	}

	public void logHashKey() {
		Log.d(TAG, "Now checking haskey");
		PackageInfo info;

		try {
			info = getPackageManager().getPackageInfo("com.markbuikema.juliana32", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				// String something = new String(Base64.encodeBytes(md.digest()));
				Log.e(TAG, "HASH KEY:" + something);
			}
		} catch (NameNotFoundException e1) {
			Log.e(TAG, e1.toString());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		Log.d(TAG, "Done printing hashkey");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			updateView();
			if (session.isOpened()) {
				saveGraphUser();
				Log.d("ACCESS_TOKEN", session.getAccessToken());
			}
		}
	}

	public void onClickLogin() {
		Session session = Session.getActiveSession();

		if (!session.isOpened() && !session.isClosed())
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		else
			Session.openActiveSession(this, true, statusCallback);
	}

	@SuppressWarnings("deprecation")
	protected void saveGraphUser() {
		if (!Session.getActiveSession().isOpened()) {
			userPicUrl = null;
			userName = null;
			userId = null;
			if (isNieuwsDetailShown())
				nieuwsDetail.setProfilePic(null);
			return;
		}
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... arg0) {
				Bundle params = new Bundle();
				params.putString("access_token", Session.getActiveSession().getAccessToken());
				params.putString("fields", "picture,name");
				try {
					Log.d("USER_INFO", "1");
					JSONObject object = new JSONObject(FacebookHelper.getFacebook().request("me", params));
					Log.d("USER_INFO", object.toString());

					userPicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
					userName = object.getString("name");
					userId = object.getString("id");

					if (isNieuwsDetailShown())
						return userPicUrl;

				} catch (MalformedURLException e) {
					return null;
				} catch (IOException e) {
					return null;
				} catch (JSONException e) {
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				if (result != null)
					nieuwsDetail.setProfilePic(result);
			}
		}.execute();
	}

	public void showPhotoDialog(String[] urls, int position, OnPhotoPagerDialogPageChangedListener callback) {
		if (urls == null || urls.length < 1)
			return;
		if (photoDialog != null)
			hidePhotoDialog();

		photoDialog = new PhotoPagerDialog(this, urls, callback);
		photoDialog.show(position);

		hideTitle();
	}

	public boolean isPhotoDialogShown() {
		return photoDialog != null;
	}

	public void hidePhotoDialog() {
		if (photoDialog == null)
			return;

		photoDialog.destroy();
		photoDialog = null;

		showTitle();
	}

	public void setDrawersEnabled(boolean enabled) {
		menuDrawer.setTouchMode(enabled ? MenuDrawer.TOUCH_MODE_BEZEL : MenuDrawer.TOUCH_MODE_NONE);
	}

	public void hideTitle() {
		try {
			((View) findViewById(android.R.id.title).getParent()).setVisibility(View.GONE);
		} catch (Exception e) {
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

	}

	public void showTitle() {
		try {
			((View) findViewById(android.R.id.title).getParent()).setVisibility(View.VISIBLE);
		} catch (Exception e) {
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

	}

	public String getUserName() {
		return userName;
	}

}

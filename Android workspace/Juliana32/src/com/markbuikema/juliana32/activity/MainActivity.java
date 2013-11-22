package com.markbuikema.juliana32.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;
import net.simonvt.menudrawer.MenuDrawer.Type;
import net.simonvt.menudrawer.Position;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.preference.PreferenceManagerHelper;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.Editor;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.adapter.MenuAdapter;
import com.markbuikema.juliana32.asynctask.EventRetriever;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.section.Agenda;
import com.markbuikema.juliana32.section.Contact;
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
import com.nineoldandroids.animation.Animator;

public class MainActivity extends Activity {

	public static final boolean OFFLINE_MODE = true;

	public static final int NOTIFICATION_INTERVAL = 1;// minutes
	private static final String TAG = "JulianaActivity";
	private static final long SECOND_BACK_PRESS_TIMEOUT = 3000;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private ImageView menuButton;
	private LinearLayout menuToggler;
	private ImageButton shareButton;
	private ImageButton picturesButton;
	private ImageButton searchButton;
	private ImageButton overflowButton;
	private ProgressBar loader;
	private TextView title;

	private LinearLayout actionBarContent;
	private EditText searchInput;

	private View activePageView;
	private View teamDetailView;
	private View nieuwsDetailView;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;

	private Teletekst teletekst;
	private Nieuws nieuws;
	private Teams teams;
	private Agenda agenda;
	@Deprecated
	private Contact contact;

	private PhotoPagerDialog photoDialog;

	private MenuDrawer menuDrawer;
	private ListView menu;
	private MenuAdapter menuAdapter;

	public Page page;
	private boolean waitingForSecondBackPress = false;
	private boolean refreshingNieuws = false;

	protected String userName;
	protected String userPicUrl;
	protected String userId;

	private boolean searching;

	private Animator mCurrentAnimator;

	public enum Page {
		NIEUWS, AGENDA, TEAMS, TELETEKST, @Deprecated
		CONTACT
	}

	public enum FailureReason {
		NO_INTERNET, SERVER_OFFLINE, UNKNOWN
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// DEBUG
		new EventRetriever().execute();

		logHashKey();

		Util.onOrientationChanged(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

		Type type =
		// getResources().getBoolean(R.bool.isTablet) ? Type.STATIC :
		Type.OVERLAY;
		menuDrawer = MenuDrawer.attach(this, type, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
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

		menu.setDivider(null);
		menu.setDividerHeight(0);
		menu.setAdapter(menuAdapter);
		OnItemClickListener l = new OnItemClickListener() {

			@Override
			public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onPageChanged(Page.values()[arg2]);
				menuDrawer.toggleMenu();

				if (arg2 == 2)
					showBetaDialogIfNecessary();
			}
		};
		menu.setOnItemClickListener(l);

		teamDetailView = findViewById(R.id.teamDetailView);
		nieuwsDetailView = findViewById(R.id.nieuwsDetailView);
		menuButton = (ImageView) findViewById(R.id.menuButton);
		menuToggler = (LinearLayout) findViewById(R.id.menuToggler);
		shareButton = (ImageButton) findViewById(R.id.menuShare);
		picturesButton = (ImageButton) findViewById(R.id.menuPictures);
		title = (TextView) findViewById(R.id.titleText);
		loader = (ProgressBar) findViewById(R.id.loading);
		actionBarContent = (LinearLayout) findViewById(R.id.actionBarContent);
		overflowButton = (ImageButton) findViewById(R.id.menuOverflow);
		searchButton = (ImageButton) findViewById(R.id.menuSearch);
		searchInput = (EditText) findViewById(R.id.searchInput);

		title.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));

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
				intent.putExtra(Intent.EXTRA_TEXT, (nieuwsDetail.getTitle().equals("Facebook") ? nieuwsDetail.getContent() : "")
						+ " (link: " + nieuwsDetail.getDetailUrl() + " )");

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

		// hide the overflow button if hardware menu button is detected
		if (Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(this).hasPermanentMenuKey()))
			overflowButton.setVisibility(View.GONE);

		initializePages();

		onPageChanged(Page.NIEUWS);

		if (getIntent() != null && getIntent().getData() != null) {
			String url = getIntent().getData().toString();
			String urlNieuwsId = null;
			if (url != null && url.startsWith("http://www.svjuliana32.nl/nieuws/nieuws")) {
				String[] split = url.split("/");
				urlNieuwsId = split[split.length - 1];
			}
			nieuws.setItemRequest(urlNieuwsId);
			setIntent(null);
		}

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

			int teamId = savedInstanceState.getInt("teamId", -1);
			if (teamId > -1) {
				Team team = findTeamById(teamId);
				requestTeamDetailPage(team);
				teamDetail.onRestoreInstanceState(savedInstanceState);
			}

			String nieuwsId = savedInstanceState.getString("nieuwsId");
			if (nieuwsId != null)
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

		// Session session = Session.getActiveSession();
		//
		// String text = "Session opened: " + session.isOpened() +
		// "\nAccess token: " + session.getAccessToken() + "\nPERMISSIONS:";
		//
		// for (String string : session.getPermissions())
		// text += "\n" + string;
		//
		// Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

		// Log.d("ACCESS TOKEN", "Token: " + session.getAccessToken());

	}

	public MenuDrawer getMenu() {
		return menuDrawer;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
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
		menuAdapter.setPage(page);

		if (activePageView != null)
			activePageView.setVisibility(View.GONE);

		boolean reloadNieuws = false;
		boolean reloadTeams = false;

		String title = getResources().getString(R.string.app_name);
		switch (page) {
		case NIEUWS:
			title = getResources().getString(R.string.menu_nieuws);
			activePageView = findViewById(R.id.nieuwsView);
			if (nieuws == null)
				nieuws = new Nieuws(this);
			nieuws.clearSearch();

			if (nieuws.getAdapterCount() < 1)
				reloadNieuws = true;
			break;
		case TEAMS:
			title = getResources().getString(R.string.menu_teams);
			activePageView = findViewById(R.id.teamsView);
			if (teams == null)
				teams = new Teams(this);
			if (!teams.isLoaded())
				loader.setVisibility(View.VISIBLE);

			if (teams.getAdapterCount() < 1)
				reloadTeams = true;

			break;
		case TELETEKST:
			title = getResources().getString(R.string.menu_teletekst);
			activePageView = findViewById(R.id.teletekstView);
			if (teletekst == null)
				teletekst = new Teletekst(this);
			break;
		case AGENDA:
			title = getResources().getString(R.string.menu_agenda);
			activePageView = findViewById(R.id.agendaView);
			if (agenda == null)
				agenda = new Agenda(this);
			break;
		case CONTACT:
			title = getResources().getString(R.string.menu_contact);
			activePageView = findViewById(R.id.contactView);
			if (contact == null)
				contact = new Contact(this);
			break;
		default:
			break;
		}

		teamDetailView.setVisibility(View.GONE);
		teamDetail = null;

		hideNieuwsDetail();

		loader.setVisibility(View.GONE);

		activePageView.setVisibility(View.VISIBLE);

		setTitle(title);

		if (menuAdapter != null)
			menuAdapter.notifyDataSetChanged();

		fixActionBar();

		if (reloadTeams)
			new TeamsRetriever(this) {
				@Override
				protected void onPostExecute(List<Team> result) {
					DataManager.getInstance().setTeams(result);
					teams = new Teams(MainActivity.this);
					teams.onTeamsLoaded();
				}
			}.execute();

		if (reloadNieuws) {
			if (nieuws == null)
				nieuws = new Nieuws(this);
			nieuws.refresh();
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showBetaDialogIfNecessary() {

		final SharedPreferences prefs = PreferenceManagerHelper.getDefaultSharedPreferences(this);

		if (!prefs.getBoolean("showTeamsMessage", true))
			return;

		final Editor edit = prefs.edit();

		View view = LayoutInflater.from(this).inflate(R.layout.tempdialog, null);
		final CheckBox doNotShowAgain = (CheckBox) view.findViewById(R.id.do_not_show_again);
		final Button close = (Button) view.findViewById(R.id.posbutton);

		Builder b = new Builder(this);

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
			shareButton.setVisibility(isNieuwsDetailShown() ? View.VISIBLE : View.GONE);
			picturesButton.setVisibility(isNieuwsDetailShown() && currentNewsItemHasPhotos() ? View.VISIBLE : View.GONE);
			searchButton.setVisibility(isNieuwsDetailShown() ? View.GONE : View.VISIBLE);
			break;
		case TEAMS:
			shareButton.setVisibility(View.GONE);
			picturesButton.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);
			if (teams != null && !isTeamDetailShown())
				loader.setVisibility(teams.isLoading() ? View.VISIBLE : View.GONE);
			break;
		case TELETEKST:
			loader.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			picturesButton.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);
			break;
		case CONTACT:
			loader.setVisibility(View.GONE);
			shareButton.setVisibility(View.GONE);
			picturesButton.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);
			break;
		default:
			break;
		}

		if (isNieuwsDetailShown())
			hideSearchBar();

		if (isTeamDetailShown() || isNieuwsDetailShown() || searching)
			menuButton.setImageResource(R.drawable.menu_borderless);
		else
			if (getResources().getBoolean(R.bool.isTablet))
				menuButton.setImageResource(R.drawable.ic_juliana);
			else
				menuButton.setImageResource(menuDrawer.isMenuVisible() ? R.drawable.menu_slid_borderless
						: R.drawable.menu_slide_borderless);

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
	public void onBackPressed() {
		if (isPhotoDialogShown()) {
			hidePhotoDialog();
			return;
		}
		if (menuDrawer.isMenuVisible()) {
			menuDrawer.toggleMenu();
			return;
		}
		if (isSearchBarShown()) {
			hideSearchBar();
			nieuws.clearSearch();
			searching = false;
			nieuws.updateMessage();
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

	@Override
	protected void onResume() {
		super.onResume();

		saveGraphUser();

		if (isNieuwsDetailShown())
			nieuwsDetail.onResume();

		onPageChanged(page);

		if (getIntent() != null && getIntent().getData() != null) {
			String url = getIntent().getData().toString();
			String urlNieuwsId = null;
			if (url != null && url.startsWith("http://www.svjuliana32.nl/nieuws/nieuws")) {
				String[] split = url.split("/");
				urlNieuwsId = split[split.length - 1];
			}
			if (nieuws.getAdapterCount() > 0)
				requestNieuwsDetailPage(nieuws.getNewsItem(urlNieuwsId));
			else
				nieuws.setItemRequest(urlNieuwsId);
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
		for (Team t : DataManager.getInstance().getTeams())
			if (t.getId() == teamId)
				return t;
		return null;
	}

	public Page getPage() {
		return page;
	}

	public void logHashKey() {
		// Log.d(TAG, "Now checking haskey");
		PackageInfo info;

		try {
			info = getPackageManager().getPackageInfo("com.markbuikema.juliana32", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				// String something = new String(Base64.encodeBytes(md.digest()));
				// Log.e(TAG, "HASH KEY:" + something);
			}
		} catch (NameNotFoundException e1) {
			Log.e(TAG, e1.toString());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		// Log.d(TAG, "Done printing hashkey");
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
			if (session.isOpened())
				saveGraphUser();
			// Log.d("ACCESS_TOKEN", session.getAccessToken());
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
					// Log.d("USER_INFO", "1");
					JSONObject object = new JSONObject(FacebookHelper.getFacebook().request("me", params));
					// Log.d("USER_INFO", object.toString());

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
		photoDialog.setPosition(position);
		photoDialog.show();

		hideTitle();
	}

	public void showPhotoDialog(final ImageView thumbView, String[] urls, int position,
			OnPhotoPagerDialogPageChangedListener callback) {
		if (urls == null || urls.length < 1)
			return;
		if (photoDialog != null)
			hidePhotoDialog();

		photoDialog = new PhotoPagerDialog(this, urls, callback);
		photoDialog.setPosition(position);
		photoDialog.show();

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
		menuToggler.setEnabled(enabled);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void hideTitle() {
		try {
			((View) findViewById(android.R.id.title).getParent()).setVisibility(View.GONE);
		} catch (Exception e) {
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		try {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		} catch (Exception e) {
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void showTitle() {
		try {
			((View) findViewById(android.R.id.title).getParent()).setVisibility(View.VISIBLE);
		} catch (Exception e) {
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		try {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		} catch (Exception e) {
		}

	}

	public String getUserName() {
		return userName;
	}

	public void reloadTeams() {
		if (teams != null)
			teams = new Teams(this);
	}

	public void reloadNieuws() {
		if (nieuws != null)
			nieuws = new Nieuws(this);
	}

	public void setRefreshingNieuws(boolean refreshing) {
		refreshingNieuws = refreshing;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_website:
			onWebsiteClick();
			break;
		case R.id.menu_item_facebook:
			onFacebookClick();
			break;
		case R.id.menu_item_twitter:
			onTwitterClick();
			break;
		case R.id.menu_item_settings:
			onSettingsClick();
			break;
		}
		return true;
	}

	private void onFacebookClick() {
		try {
			getPackageManager().getPackageInfo("com.facebook.katana", 0);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/294105307313875")));
		} catch (Exception e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/svjuliana32")));
		}
	}

	private void onTwitterClick() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=svjuliana32"));
			startActivity(intent);
		} catch (Exception e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/svjuliana32")));
		}
	}

	private void onWebsiteClick() {
		String url = "http://www.svjuliana32.nl";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	private void onSettingsClick() {
		Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		i.putExtra("userName", userName);
		i.putExtra("userPicUrl", userPicUrl);
		startActivity(i);
	}
}

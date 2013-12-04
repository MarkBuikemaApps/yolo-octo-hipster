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
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;
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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.adapter.MenuAdapter;
import com.markbuikema.juliana32.adapter.NieuwsAdapter;
import com.markbuikema.juliana32.asynctask.EventRetriever;
import com.markbuikema.juliana32.asynctask.TeamsRetriever;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
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
import com.markbuikema.juliana32.ui.Toaster;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper;
import com.markbuikema.juliana32.util.Util;
import com.markbuikema.juliana32.util.Util.LinkifyExtra;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class MainActivity extends Activity {

	public static final boolean OFFLINE_MODE = true;

	public static final int NOTIFICATION_INTERVAL = 1;// minutes
	private static final String TAG = "JulianaActivity";
	private static final long SECOND_BACK_PRESS_TIMEOUT = 3000;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private ImageView menuButton;
	private ImageView menuDrawerIcon;
	private LinearLayout menuToggler;
	private ImageButton shareButton;
	private ImageButton picturesButton;
	private ImageButton searchButton;
	private ImageButton overflowButton;
	private ProgressBar loader;
	private TextView title;

	private PopupMenu popupMenu;

	private LinearLayout actionBarContent;
	private EditText searchInput;

	private View activePageView;
	private View teamDetailView;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;
	private boolean nieuwsDetailShown;
	private View clickedNieuwsView;

	private ScrollView animatedViewContainer;

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

	private int actionBarHeight;
	private int defaultNieuwsViewWidth;

	private boolean searching;

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

		actionBarHeight = getResources().getDimensionPixelSize(R.dimen.nieuws_header_margin);

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
				float newValue = -11.0f * openRatio;
				ViewPropertyAnimator.animate(menuDrawerIcon).translationX(newValue).setDuration(0).start();
			}
		});

		menu = (ListView) findViewById(R.id.menuDrawer);
		menuAdapter = new MenuAdapter(this);

		menu.setDividerHeight(0);
		menu.setAdapter(menuAdapter);
		menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onPageChanged(Page.values()[arg2]);
				menuDrawer.toggleMenu();

				if (arg2 == 2)
					showBetaDialogIfNecessary();
			}
		});

		// make the menuToggler work when menu is opened
		findViewById(R.id.menuCorner).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return menuToggler.onTouchEvent(arg1);
			}
		});

		teamDetailView = findViewById(R.id.teamDetailView);
		menuButton = (ImageView) findViewById(R.id.menuButton);
		menuDrawerIcon = (ImageView) findViewById(R.id.menuDrawerIcon);
		menuToggler = (LinearLayout) findViewById(R.id.menuToggler);
		shareButton = (ImageButton) findViewById(R.id.menuShare);
		picturesButton = (ImageButton) findViewById(R.id.menuPictures);
		title = (TextView) findViewById(R.id.titleText);
		loader = (ProgressBar) findViewById(R.id.loading);
		actionBarContent = (LinearLayout) findViewById(R.id.actionBarContent);
		overflowButton = (ImageButton) findViewById(R.id.menuOverflow);
		searchButton = (ImageButton) findViewById(R.id.menuSearch);
		searchInput = (EditText) findViewById(R.id.searchInput);
		animatedViewContainer = (ScrollView) findViewById(R.id.animatedViewContainer);

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
		else {
			popupMenu = new PopupMenu(this, overflowButton);
			popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(menuListener);
			overflowButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					popupMenu.show();
				}
			});
		}

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
						requestNieuwsDetailPage(item, null);
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

			if (nieuws.isAdapterEmpty())
				reloadNieuws = true;
			break;
		case TEAMS:
			title = getResources().getString(R.string.menu_teams);
			activePageView = findViewById(R.id.teamsView);
			if (teams == null)
				teams = new Teams(this);
			if (!teams.isLoaded())
				loader.setVisibility(View.VISIBLE);

			if (teams.isAdapterEmpty())
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
			if (!nieuws.isRefreshing())
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

		if (isTeamDetailShown() || isNieuwsDetailShown() || searching) {
			menuButton.setImageResource(R.drawable.menu_borderless);
			menuDrawerIcon.setVisibility(View.GONE);
		} else {
			menuButton.setImageResource(R.drawable.menu_icon);
			menuDrawerIcon.setVisibility(View.VISIBLE);
		}

		if (page == Page.NIEUWS && searching)
			if (!isNieuwsDetailShown()) {
				requestSearchBar();
				menuButton.setImageResource(R.drawable.menu_borderless);
				menuDrawerIcon.setVisibility(View.GONE);
			}

		actionBarContent.setVisibility(menuDrawer.isMenuVisible() ? View.INVISIBLE : View.VISIBLE);

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

	// FIXME request
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void requestNieuwsDetailPage(final NieuwsItem item, final View clickedView) {
		if ((page != Page.NIEUWS && page != Page.TEAMS) || isNieuwsDetailShown())
			return;

		if (!item.isContentLoaded()) {
			item.startLoading(new OnContentLoadedListener() {

				@Override
				public void onContentLoaded(String content, List<String> photos) {
					requestNieuwsDetailPage(item, clickedView);
				}
			});
			loader.setVisibility(View.VISIBLE);
			return;
		}
		loader.setVisibility(View.GONE);

		nieuwsDetail = new NieuwsDetail(this, item);
		nieuwsDetailShown = true;
		saveGraphUser();

		clickedNieuwsView = clickedView;
		ViewHelper.setAlpha(clickedNieuwsView, 0);

		animatedViewContainer.removeAllViews();
		final View view = getAnimationView(item);
		ViewHelper.setTranslationX(view, clickedView.getLeft() - view.getLeft());
		ViewHelper.setTranslationY(view, clickedView.getTop() - view.getTop() - actionBarHeight);
		view.setLayoutParams(new LayoutParams(clickedView.getWidth(), clickedView.getHeight()));

		animatedViewContainer.addView(view);
		nieuws.setScrollingEnabled(false);

		ViewPropertyAnimator.animate(activePageView).alpha(.05f).setDuration(300);
		ViewPropertyAnimator.animate(view).translationX(0).translationY(0).setDuration(300);
		if (!item.isFromFacebook())
			ViewPropertyAnimator.animate(view.findViewById(R.id.nieuwsitem_subtitle)).alpha(0).setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					ViewPropertyAnimator.animate(view.findViewById(R.id.nieuwsitem_content)).alpha(1).setDuration(300);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
				}
			}).setDuration(300);

		String contentString = null;
		contentString = item.getContent();
		if (contentString == null)
			contentString = getResources().getString(R.string.content_not_found);
		contentString = contentString.replaceAll(Character.toString((char) 65532), "");

		TextView subTitleView = (TextView) view.findViewById(R.id.nieuwsitem_content);
		subTitleView.setText(Util.trimTrailingWhitespace(LinkifyExtra.addLinksHtmlAware(contentString)));
		subTitleView.setMovementMethod(LinkMovementMethod.getInstance());
		subTitleView.setLinksClickable(true);
		if (Build.VERSION.SDK_INT >= 11)
			subTitleView.setTextIsSelectable(true);
		if (getResources().getInteger(R.integer.columnCount) > 1)
			subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.readableTextSize));

		if (item.isFromFacebook() && ((FacebookNieuwsItem) item).getComments().size() > 0) {
			LinearLayout comments = (LinearLayout) view.findViewById(R.id.nieuwsitem_comments_container);

			populateComments(comments, (FacebookNieuwsItem) item);
			Util.expand(comments);
			ViewPropertyAnimator.animate(comments).alpha(1).setDuration(300);
		}

		Util.expandX(view);
		Util.expand(subTitleView);

		hideSearchBar();
		fixActionBar();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
	}

	private View getAnimationView(NieuwsItem item) {
		View view = null;
		if (item instanceof NormalNieuwsItem)
			view = NieuwsAdapter.constructNormalView(this, item, null);
		else
			if (item instanceof FacebookNieuwsItem)
				if (item.isPhoto())
					view = NieuwsAdapter.constructFacebookPhotoView(this, (FacebookNieuwsItem) item, null);
				else
					view = NieuwsAdapter.constructFacebookView(this, (FacebookNieuwsItem) item, null);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.topMargin = actionBarHeight;
		view.setLayoutParams(lp);
		return view;
	}

	public void populateComments(LinearLayout container, FacebookNieuwsItem item) {
		if (item.getComments().size() == 0) {
			container.setVisibility(View.GONE);
			return;
		} else
			container.setVisibility(View.VISIBLE);

		TextView caption = (TextView) container.findViewById(R.id.nieuwsitem_comments_caption);
		caption.setTypeface(Util.getRobotoCondensed(this));

		for (int i = 2; i < container.getChildCount(); i++)
			container.removeViewAt(i);

		for (Comment comment : item.getComments())
			container.addView(getCommentView(comment));

	}

	private View getCommentView(final Comment comment) {
		View view = LayoutInflater.from(this).inflate(R.layout.listitem_comment, null);

		TextView name = (TextView) view.findViewById(R.id.commentName);
		TextView date = (TextView) view.findViewById(R.id.commentDate);
		TextView text = (TextView) view.findViewById(R.id.commentMessage);
		final ImageView pic = (ImageView) view.findViewById(R.id.commentPicture);

		name.setTypeface(Util.getRobotoCondensed(this));
		date.setTypeface(Util.getRobotoCondensed(this));
		text.setTypeface(Util.getRobotoLight(this));

		name.setText(comment.getName());

		String dateString = Util.getDateString(this, comment.getCreatedAt());
		// Log.d("comment_date_adapter", dateString);
		date.setText(dateString);
		text.setText(comment.getText());
		UrlImageViewHelper.setUrlDrawable(pic, comment.getImgUrl());

		pic.setContentDescription(comment.getName());
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + comment.getUserId())));
			}
		});

		return view;
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
		Toaster.toast(this, getResources().getString(R.string.second_back_press));
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				waitingForSecondBackPress = false;
			}

		}, SECOND_BACK_PRESS_TIMEOUT);
	}

	// FIXME hide
	public void hideNieuwsDetail() {
		if (nieuwsDetail != null)
			nieuwsDetail.cancelTasks();
		if (clickedNieuwsView != null) {
			final View view = animatedViewContainer.getChildAt(0);
			TextView tvContent = (TextView) view.findViewById(R.id.nieuwsitem_content);
			Util.collapse(tvContent);
			Util.collapseX(view);
			LinearLayout comments = (LinearLayout) view.findViewById(R.id.nieuwsitem_comments_container);
			final boolean fb = comments != null;
			if (fb)
				Util.collapse(comments);
			ViewPropertyAnimator.animate(tvContent).alpha(fb ? 1 : 0).setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					TextView tvSubTitle = (TextView) view.findViewById(R.id.nieuwsitem_subtitle);
					ViewPropertyAnimator.animate(tvSubTitle).alpha(1).setListener(new AnimatorListener() {

						@Override
						public void onAnimationStart(Animator arg0) {
						}

						@Override
						public void onAnimationRepeat(Animator arg0) {
						}

						@Override
						public void onAnimationEnd(Animator arg0) {
							animatedViewContainer.scrollTo(0, 0);
							animatedViewContainer.removeAllViews();
							nieuws.setScrollingEnabled(true);
							if (clickedNieuwsView != null) {
								ViewHelper.setAlpha(clickedNieuwsView, 1f);
								clickedNieuwsView = null;
							}
						}

						@Override
						public void onAnimationCancel(Animator arg0) {

						}
					}).setDuration(300);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
				}
			}).setDuration(300);
			ViewPropertyAnimator.animate(view).translationX(clickedNieuwsView.getLeft() - view.getLeft())
					.translationY(clickedNieuwsView.getTop() - view.getTop() - actionBarHeight).setDuration(300);
		}
		ViewPropertyAnimator.animate(activePageView).alpha(1f).setDuration(300);
		nieuwsDetail = null;
		nieuwsDetailShown = false;

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
		return nieuwsDetailShown;
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
				requestNieuwsDetailPage(DataManager.getInstance().getNieuwsItemById(urlNieuwsId), null);
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
		menuDrawerIcon.setVisibility(View.GONE);

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
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (popupMenu == null)
			return super.onMenuOpened(featureId, menu);
		else
			popupMenu.show();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuListener.onMenuItemClick(item);
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

	private OnMenuItemClickListener menuListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
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
	};
}

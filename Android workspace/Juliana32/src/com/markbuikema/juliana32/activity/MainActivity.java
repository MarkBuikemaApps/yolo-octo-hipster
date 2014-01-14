package com.markbuikema.juliana32.activity;

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
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.preference.PreferenceManagerHelper;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.adapter.MenuAdapter;
import com.markbuikema.juliana32.asynctask.EventRetriever;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem.OnContentLoadedListener;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.model.UserInfo;
import com.markbuikema.juliana32.section.Agenda;
import com.markbuikema.juliana32.section.Contact;
import com.markbuikema.juliana32.section.Nieuws;
import com.markbuikema.juliana32.section.NieuwsDetail;
import com.markbuikema.juliana32.section.NieuwsDetail.FacebookProfileCallback;
import com.markbuikema.juliana32.section.TeamDetail;
import com.markbuikema.juliana32.section.Teams;
import com.markbuikema.juliana32.section.Teletekst;
import com.markbuikema.juliana32.ui.PhotoPagerDialog;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.ui.Toaster;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.Util;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity {

	public static final boolean OFFLINE_MODE = true;

	public static final int NOTIFICATION_INTERVAL = 1;// minutes
	private static final String TAG = "JulianaActivity";
	private static final long SECOND_BACK_PRESS_TIMEOUT = 3000;

	private ImageView menuDrawerIcon;
	private ImageView menuUpIcon;
	private LinearLayout menuToggler;
	private ImageButton shareButton;
	private ImageButton searchButton;
	private ImageButton overflowButton;
	private ProgressBar loader;
	private TextView title;

	private LinearLayout actionBar;

	private PopupMenu popupMenu;

	private LinearLayout actionBarContent;
	private EditText searchInput;

	private View activePageView;
	private View teamDetailView;

	private TeamDetail teamDetail;
	private NieuwsDetail nieuwsDetail;

	private UserInfo facebookUser;

	private Teletekst teletekst;
	private Nieuws nieuws;
	private Teams teams;
	private Agenda agenda;
	@Deprecated
	private Contact contact;

	private PhotoPagerDialog photoDialog;
	private View teamTabs;

	private MenuDrawer menuDrawer;
	private ListView menu;
	private MenuAdapter menuAdapter;

	public Page page;
	private boolean waitingForSecondBackPress = false;
	private boolean refreshingNieuws = false;
	private boolean isTablet;

	private int actionBarHeight;
	private int defaultNieuwsViewWidth;
	private int drawerOffset;

	private boolean searching;

	public enum Page {
		NIEUWS, AGENDA, TEAMS, TELETEKST, @Deprecated
		CONTACT
	}

	public enum FailureReason {
		NO_INTERNET, SERVER_OFFLINE, UNKNOWN
	}

	@SuppressLint ("NewApi" )
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		// DEBUG
		new EventRetriever().execute();

		logHashKey();

		actionBarHeight = getResources().getDimensionPixelSize( R.dimen.nieuws_header_margin );
		drawerOffset = getResources().getDimensionPixelSize( R.dimen.drawerOffset );
		isTablet = getResources().getBoolean( R.bool.isTablet );

		Type type =
		// getResources().getBoolean(R.bool.isTablet) ? Type.STATIC :
		Type.OVERLAY;
		menuDrawer = MenuDrawer.attach( this, type, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW );
		menuDrawer.setTouchMode( MenuDrawer.TOUCH_MODE_BEZEL );
		menuDrawer.setContentView( R.layout.activity_main );
		menuDrawer.setMenuView( R.layout.menu_main );

		menuDrawer.setOnDrawerStateChangeListener( new OnDrawerStateChangeListener() {

			@Override
			public void onDrawerStateChange( int oldState, int newState ) {
				fixActionBar();
				if ( ViewHelper.getTranslationY( actionBar ) < 0 )
					ViewPropertyAnimator.animate( actionBar ).translationY( 0 ).setListener( null ).setDuration( 300 ).start();

				View tabs = findViewById( R.id.teamsTabsIndicator );
				if ( tabs != null && ViewHelper.getTranslationY( tabs ) < 0 )
					ViewPropertyAnimator.animate( tabs ).translationY( 0 ).setListener( null ).setDuration( 300 ).start();

			}

			@Override
			public void onDrawerSlide( float openRatio, int offsetPixels ) {
				float newValue = drawerOffset * openRatio;
				ViewHelper.setTranslationX( menuDrawerIcon, newValue );
			}
		} );

		menu = (ListView) findViewById( R.id.menuDrawer );
		menuAdapter = new MenuAdapter( this );

		menu.setDividerHeight( 0 );
		menu.setAdapter( menuAdapter );
		menu.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {
				onPageChanged( Page.values()[ arg2 ] );
				menuDrawer.toggleMenu();

				if ( arg2 == 2 )
					showBetaDialogIfNecessary();
			}
		} );

		// make the menuToggler work when menu is opened
		findViewById( R.id.menuCorner ).setOnTouchListener( new OnTouchListener() {

			@Override
			public boolean onTouch( View arg0, MotionEvent arg1 ) {
				return menuToggler.onTouchEvent( arg1 );
			}
		} );

		actionBar = (LinearLayout) findViewById( R.id.actionBar );
		teamDetailView = findViewById( R.id.teamDetailView );

		menuDrawerIcon = (ImageView) findViewById( R.id.menuDrawerIcon );
		menuUpIcon = (ImageView) findViewById( R.id.menuUpIcon );
		menuToggler = (LinearLayout) findViewById( R.id.menuToggler );
		shareButton = (ImageButton) findViewById( R.id.menuShare );
		title = (TextView) findViewById( R.id.titleText );
		loader = (ProgressBar) findViewById( R.id.loading );
		actionBarContent = (LinearLayout) findViewById( R.id.actionBarContent );
		overflowButton = (ImageButton) findViewById( R.id.menuOverflow );
		searchButton = (ImageButton) findViewById( R.id.menuSearch );
		searchInput = (EditText) findViewById( R.id.searchInput );
		teamTabs = findViewById( R.id.teamsTabsIndicator );

		title.setTypeface( Util.getRobotoLight( this ) );

		menuToggler.setClickable( true );
		menuToggler.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {
				if ( isTeamDetailShown() || isNieuwsDetailShown() || isSearchBarShown() )
					onBackPressed();
				else if ( ViewHelper.getTranslationY( actionBar ) < 0 ) {
					ViewPropertyAnimator.animate( actionBar ).translationY( 0 ).setDuration( 300 ).setListener( new AnimatorListener() {

						@Override
						public void onAnimationStart( Animator arg0 ) {
						}

						@Override
						public void onAnimationRepeat( Animator arg0 ) {
						}

						@Override
						public void onAnimationEnd( Animator arg0 ) {
							menuDrawer.toggleMenu();
						}

						@Override
						public void onAnimationCancel( Animator arg0 ) {
						}
					} ).start();
					
					View tabs = findViewById( R.id.teamsTabsIndicator );
					if ( tabs != null && ViewHelper.getTranslationY( tabs ) < 0 )
						ViewPropertyAnimator.animate( tabs ).translationY( 0 ).setListener( null ).setDuration( 300 ).start();

				} else
					menuDrawer.toggleMenu();
			}
		} );

		shareButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				final Intent intent = new Intent( Intent.ACTION_SEND );

				intent.setType( "text/plain" );
				intent.putExtra( Intent.EXTRA_SUBJECT, nieuwsDetail.getTitle() );
				intent.putExtra( Intent.EXTRA_TEXT, ( nieuwsDetail.getTitle().equals( "Facebook" ) ? nieuwsDetail.getContent() : "" )
						+ " (link: " + nieuwsDetail.getDetailUrl() + " )" );

				startActivity( Intent.createChooser( intent, getString( R.string.share ) ) );
			}
		} );

		searchButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				requestSearchBar();
				searchInput.setText( "" );
			}
		} );

		searchInput.addTextChangedListener( new TextWatcher() {

			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count ) {
				nieuws.search( s.toString() );
				searchInput.setTextColor( getResources().getColor( nieuws.isAdapterEmpty() ? R.color.red : android.R.color.black ) );
			}

			@Override
			public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
			}

			@Override
			public void afterTextChanged( Editable s ) {
			}
		} );
		if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB )
			searchInput.setCustomSelectionActionModeCallback( new ActionMode.Callback() {

				public boolean onPrepareActionMode( ActionMode mode, Menu menu ) {
					return false;
				}

				public void onDestroyActionMode( ActionMode mode ) {
				}

				public boolean onCreateActionMode( ActionMode mode, Menu menu ) {
					return false;
				}

				public boolean onActionItemClicked( ActionMode mode, MenuItem item ) {
					return false;
				}
			} );

		if ( isTablet ) {
			int searchBarWidth = getResources().getDimensionPixelSize( R.dimen.bigSearchBarWidth );
			LinearLayout.LayoutParams searchInputLP = new LinearLayout.LayoutParams( searchBarWidth, LayoutParams.WRAP_CONTENT );
			searchInputLP.gravity = Gravity.RIGHT;
			searchInput.setLayoutParams( searchInputLP );
		}

		searchInput.setOnFocusChangeListener( new OnFocusChangeListener() {

			@Override
			public void onFocusChange( View arg0, boolean arg1 ) {
				nieuws.setAnimationsEnabled( !arg1 );
			}
		} );

		// hide the overflow button if hardware menu button is detected
		if ( Build.VERSION.SDK_INT <= 10 || ( Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get( this ).hasPermanentMenuKey() ) )
			overflowButton.setVisibility( View.GONE );
		else {
			popupMenu = new PopupMenu( this, overflowButton );
			popupMenu.getMenuInflater().inflate( R.menu.main, popupMenu.getMenu() );
			popupMenu.setOnMenuItemClickListener( menuListener );
			overflowButton.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick( View arg0 ) {
					popupMenu.show();
				}
			} );
		}

		initializePages();

		onPageChanged( Page.NIEUWS );

		if ( getIntent() != null && getIntent().getData() != null ) {
			String url = getIntent().getData().toString();
			String urlNieuwsId = null;
			if ( url != null && url.startsWith( "http://www.svjuliana32.nl/nieuws/nieuws" ) ) {
				String[] split = url.split( "/" );
				urlNieuwsId = split[ split.length - 1 ];
			}
			nieuws.setItemRequest( urlNieuwsId );
			setIntent( null );
		}

		// FACEBOOK STUFF//////////////////
		Session session = Session.getActiveSession();
		if ( session == null ) {
			if ( savedInstanceState != null ) {
				session = Session.restoreSession( this, null, statusCallback, savedInstanceState );
			}
			if ( session == null ) {
				session = new Session( this );
			}
			Session.setActiveSession( session );
			if ( session.getState().equals( SessionState.CREATED_TOKEN_LOADED ) ) {
				session.openForRead( new Session.OpenRequest( this ).setPermissions( "basic_info" ).setCallback( statusCallback ) );
			}
		}

		// RESTORE ACTIVITY STATE ////////////////////
		if ( savedInstanceState != null ) {

			if ( teletekst != null )
				teletekst.onRestoreInstanceState( savedInstanceState );
			onPageChanged( Page.valueOf( savedInstanceState.getString( "page" ) ) );

			int teamId = savedInstanceState.getInt( "teamId", -1 );
			if ( teamId > -1 ) {
				Team team = Teams.findTeamById( teamId );
				requestTeamDetailPage( team );
				teamDetail.onRestoreInstanceState( savedInstanceState );
			}

			String nieuwsId = savedInstanceState.getString( "nieuwsId" );
			if ( nieuwsId != null )
				for ( NieuwsItem item : DataManager.getInstance().getNieuwsItems() )
					if ( item.getId() == nieuwsId ) {
						requestNieuwsDetailPage( item, null );
						break;
					}

			int photoDialogPage = savedInstanceState.getInt( "photoDialogPage", -1 );
			if ( photoDialogPage > -1 ) {
				String[] urls = savedInstanceState.getStringArray( "photoDialogUrls" );
				showPhotoDialog( urls, photoDialogPage, null );
			}

			if ( savedInstanceState.getBoolean( "menuOpened" ) )
				menuDrawer.toggleMenu();

			searching = savedInstanceState.getBoolean( "searching", false );

			if ( page == Page.NIEUWS && searching && !isNieuwsDetailShown() )
				requestSearchBar();

			if ( searching )
				searchInput.setText( savedInstanceState.getString( "searchWord" ) );

		}
	}

	protected boolean isSearchBarShown() {
		return searchInput.getVisibility() == View.VISIBLE;
	}

	public MenuDrawer getMenu() {
		return menuDrawer;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	private void initializePages() {
		for ( Page page : Page.values() )
			onPageChanged( page );
	}

	public void onPageChanged( Page page ) {
		if ( this.page == Page.NIEUWS && searching ) {
			searching = false;
			hideSearchBar();
			fixActionBar();
		}

		this.page = page;
		menuAdapter.setPage( page );

		if ( activePageView != null )
			activePageView.setVisibility( View.GONE );

		boolean reloadNieuws = false;

		String title = getResources().getString( R.string.app_name );
		switch ( page ) {
		case NIEUWS:
			title = getResources().getString( R.string.menu_nieuws );
			activePageView = findViewById( R.id.nieuwsView );
			if ( nieuws == null )
				nieuws = new Nieuws( this );
			nieuws.clearSearch();

			if ( nieuws.isAdapterEmpty() )
				reloadNieuws = true;
			break;
		case TEAMS:
			title = getResources().getString( R.string.menu_teams );
			activePageView = findViewById( R.id.teamsView );
			if ( teams == null )
				teams = new Teams( this );

			teams.onResume();
			break;
		case TELETEKST:
			title = getResources().getString( R.string.menu_teletekst );
			activePageView = findViewById( R.id.teletekstView );
			if ( teletekst == null )
				teletekst = new Teletekst( this );
			break;
		case AGENDA:
			title = getResources().getString( R.string.menu_agenda );
			activePageView = findViewById( R.id.agendaView );
			if ( agenda == null )
				agenda = new Agenda( this );
			break;
		case CONTACT:
			// title = getResources().getString( R.string.menu_contact );
			// activePageView = findViewById( R.id.contactView );
			// if ( contact == null )
			// contact = new Contact( this );
			break;
		default:
			break;
		}

		teamDetailView.setVisibility( View.GONE );
		teamDetail = null;

		hideNieuwsDetail();

		loader.setVisibility( View.GONE );

		activePageView.setVisibility( View.VISIBLE );

		setTitle( title );

		if ( menuAdapter != null )
			menuAdapter.notifyDataSetChanged();

		fixActionBar();

		if ( reloadNieuws ) {
			if ( nieuws == null )
				nieuws = new Nieuws( this );
			if ( !nieuws.isRefreshing() )
				nieuws.refresh();
		}
	}

	@TargetApi (Build.VERSION_CODES.HONEYCOMB )
	private void showBetaDialogIfNecessary() {

		final SharedPreferences prefs = PreferenceManagerHelper.getDefaultSharedPreferences( this );

		if ( !prefs.getBoolean( "showTeamsMessage", true ) )
			return;

		final Editor edit = prefs.edit();

		View view = LayoutInflater.from( this ).inflate( R.layout.tempdialog, null );
		final CheckBox doNotShowAgain = (CheckBox) view.findViewById( R.id.do_not_show_again );
		final Button close = (Button) view.findViewById( R.id.posbutton );

		Builder b = new Builder( this );

		final AlertDialog dialog = b.setView( view ).create();
		close.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				if ( doNotShowAgain.isChecked() ) {
					edit.putBoolean( "showTeamsMessage", false );
					edit.commit();
				}
				dialog.dismiss();
			}
		} );
		dialog.show();

	}

	public void fixActionBar() {
		switch ( page ) {

		case NIEUWS:
			loader.setVisibility( View.GONE );
			shareButton.setVisibility( isNieuwsDetailShown() ? View.VISIBLE : View.GONE );
			searchButton.setVisibility( isNieuwsDetailShown() || isTablet ? View.GONE : View.VISIBLE );
			searchInput.setVisibility( isTablet ? View.VISIBLE : View.GONE );
			break;
		case TEAMS:
			shareButton.setVisibility( View.GONE );
			searchButton.setVisibility( View.GONE );
			if ( teams != null && !isTeamDetailShown() )
				loader.setVisibility( !teams.isLoaded() ? View.VISIBLE : View.GONE );

			break;
		case TELETEKST:
			loader.setVisibility( View.GONE );
			shareButton.setVisibility( View.GONE );
			searchButton.setVisibility( View.GONE );

			break;
		case CONTACT:
			loader.setVisibility( View.GONE );
			shareButton.setVisibility( View.GONE );
			searchButton.setVisibility( View.GONE );

			break;
		default:
			break;
		}

		if ( isNieuwsDetailShown() )
			hideSearchBar();

		if ( isTeamDetailShown() || isNieuwsDetailShown() || searching ) {
			menuUpIcon.setVisibility( View.VISIBLE );
			menuDrawerIcon.setVisibility( View.GONE );
		} else {
			menuUpIcon.setVisibility( View.GONE );
			menuDrawerIcon.setVisibility( View.VISIBLE );
		}

		if ( page == Page.NIEUWS && searching )
			if ( !isNieuwsDetailShown() ) {
				requestSearchBar();
				menuUpIcon.setVisibility( View.VISIBLE );
				menuDrawerIcon.setVisibility( View.GONE );
			}

		if ( teamTabs != null )
			teamTabs.setVisibility( page == Page.TEAMS && !isTeamDetailShown() ? View.VISIBLE : View.GONE );

		actionBarContent.setVisibility( menuDrawer.isMenuVisible() ? View.INVISIBLE : View.VISIBLE );

	}

	private boolean currentNewsItemHasPhotos() {
		if ( nieuwsDetail == null )
			return false;
		return nieuwsDetail.hasPhotos();
	}

	public void requestTeamDetailPage( Team team ) {
		if ( page != Page.TEAMS || isTeamDetailShown() )
			return;

		if ( ViewHelper.getTranslationY( actionBar ) < 0 )
			ViewPropertyAnimator.animate( actionBar ).translationY( 0 ).setListener( null ).setDuration( 300 ).start();

		View tabs = findViewById( R.id.teamsTabsIndicator );
		if ( tabs != null && ViewHelper.getTranslationY( tabs ) < 0 )
			ViewPropertyAnimator.animate( tabs ).translationY( 0 ).setListener( null ).setDuration( 300 ).start();

		
		teamDetail = new TeamDetail( this, team );

		activePageView.setVisibility( View.GONE );
		teamDetailView.setVisibility( View.VISIBLE );

		setTitle( team.getName() );

		fixActionBar();

		increaseCategoryFrequency( team.getCategory() );
	}

	private void increaseCategoryFrequency( Category category ) {
		SharedPreferences sp = getSharedPreferences( "categories", 0 );
		int currentCount = sp.getInt( "cat" + category.ordinal(), 0 );
		Editor e = sp.edit();
		e.putInt( "cat" + category.ordinal(), currentCount + 1 );
		e.commit();

		Log.d( "counter", "new count for cat " + category.getName() + " :  " + sp.getInt( "cat" + category.ordinal(), -1 ) );
	}

	@TargetApi (Build.VERSION_CODES.HONEYCOMB )
	public void requestNieuwsDetailPage( final NieuwsItem item, final View clickedView ) {

		if ( ( page != Page.NIEUWS && page != Page.TEAMS ) || isNieuwsDetailShown() )
			return;

		if ( !item.isContentLoaded() ) {
			item.startLoading( new OnContentLoadedListener() {

				@Override
				public void onContentLoaded( String content, List<String> photos ) {
					requestNieuwsDetailPage( item, clickedView );
				}
			} );
			loader.setVisibility( View.VISIBLE );
			return;
		}
		loader.setVisibility( View.GONE );

		hideSearchBar();

		nieuws.fadeList( true );
		nieuws.setScrollingEnabled( false );

		nieuwsDetail = new NieuwsDetail( this, clickedView, item );

		fixActionBar();

		InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
		imm.hideSoftInputFromWindow( searchInput.getWindowToken(), 0 );
	}

	public void hideNieuwsDetail() {

		if ( nieuwsDetail == null )
			return;

		if ( searching )
			requestSearchBar();

		nieuwsDetail.hide();
		nieuws.fadeList( false );
		nieuwsDetail = null;
		nieuws.setScrollingEnabled( true );

		fixActionBar();
	}

	@Override
	public void onBackPressed() {
		if ( isPhotoDialogShown() ) {
			hidePhotoDialog();
			return;
		}
		if ( menuDrawer.isMenuVisible() ) {
			menuDrawer.toggleMenu();
			return;
		}
		if ( isSearchBarShown() ) {
			hideSearchBar();
			nieuws.clearSearch();
			searching = false;
			nieuws.updateMessage();
			fixActionBar();
			return;
		}
		if ( isNieuwsDetailShown() )
			hideNieuwsDetail();
		else if ( isTeamDetailShown() ) {
			if ( teamDetail.isACloudOpened() )
				teamDetail.closeClouds();
			else {
				hideTeamDetail();
				setTitle( R.string.menu_teams );
			}
		} else if ( waitingForSecondBackPress ) {
			DataManager.getInstance().clearData();
			finish();
		} else
			startBackPressTimer();
	}

	private void startBackPressTimer() {
		waitingForSecondBackPress = true;
		Toaster.toast( this, getResources().getString( R.string.second_back_press ) );
		new Timer().schedule( new TimerTask() {

			@Override
			public void run() {
				waitingForSecondBackPress = false;
			}

		}, SECOND_BACK_PRESS_TIMEOUT );
	}

	public void hideTeamDetail() {
		teamDetailView.setVisibility( View.GONE );
		activePageView.setVisibility( View.VISIBLE );
		teamDetail = null;

		fixActionBar();
	}

	public boolean isTeamDetailShown() {
		return teamDetailView.getVisibility() == View.VISIBLE;
	}

	public boolean isNieuwsDetailShown() {
		return nieuwsDetail != null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		onPageChanged( page );

		if ( getIntent() != null && getIntent().getData() != null ) {
			String url = getIntent().getData().toString();
			String urlNieuwsId = null;
			if ( url != null && url.startsWith( "http://www.svjuliana32.nl/nieuws/nieuws" ) ) {
				String[] split = url.split( "/" );
				urlNieuwsId = split[ split.length - 1 ];
			}
			if ( nieuws.getAdapterCount() > 0 )
				requestNieuwsDetailPage( DataManager.getInstance().getNieuwsItemById( urlNieuwsId ), null );
			else
				nieuws.setItemRequest( urlNieuwsId );
		}
	}

	@Override
	public void setTitle( int resid ) {
		title.setText( resid );
	}

	@Override
	public void setTitle( CharSequence title ) {
		this.title.setText( title );
	}

	public void requestSearchBar() {
		if ( isSearchBarShown() )
			return;

		actionBar.setBackgroundResource( R.drawable.actionbar_contextual );
		actionBarContent.setVisibility( View.GONE );
		searchInput.setVisibility( View.VISIBLE );
		searchInput.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
		inputMethodManager.toggleSoftInputFromWindow( searchInput.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0 );

		searching = true;

		title.setVisibility( View.GONE );

		menuUpIcon.setVisibility( View.VISIBLE );
		menuDrawerIcon.setVisibility( View.GONE );
	}

	public void hideSearchBar() {
		if ( !isSearchBarShown() )
			return;

		actionBar.setBackgroundResource( R.drawable.actionbar );

		actionBarContent.setVisibility( View.VISIBLE );
		searchInput.setVisibility( View.GONE );
		title.setVisibility( View.VISIBLE );

		InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
		imm.hideSoftInputFromWindow( searchInput.getWindowToken(), 0 );
	}

	@Override
	protected void onSaveInstanceState( Bundle outState ) {

		outState.putString( "page", page.toString() );

		if ( isTeamDetailShown() )
			teamDetail.onSaveInstanceState( outState );

		if ( teletekst != null )
			teletekst.onSaveInstanceState( outState );

		if ( isPhotoDialogShown() )
			photoDialog.onSaveInstanceState( outState );

		if ( isNieuwsDetailShown() )
			nieuwsDetail.onSaveInstanceState( outState );

		outState.putBoolean( "menuOpened", menuDrawer.isMenuVisible() );

		outState.putString( "searchWord", searchInput.getText().toString() );
		outState.putBoolean( "searching", searching );

		Session session = Session.getActiveSession();
		Session.saveSession( session, outState );

		super.onSaveInstanceState( outState );
	}

	public Page getPage() {
		return page;
	}

	public void logHashKey() {
		// Log.d(TAG, "Now checking haskey");
		PackageInfo info;

		try {
			info = getPackageManager().getPackageInfo( "com.markbuikema.juliana32", PackageManager.GET_SIGNATURES );
			for ( Signature signature : info.signatures ) {
				MessageDigest md;
				md = MessageDigest.getInstance( "SHA" );
				md.update( signature.toByteArray() );
				String something = new String( Base64.encode( md.digest(), 0 ) );
				// String something = new
				// String(Base64.encodeBytes(md.digest()));
				// Log.e(TAG, "HASH KEY:" + something);
			}
		} catch ( NameNotFoundException e1 ) {
			Log.e( TAG, e1.toString() );
		} catch ( NoSuchAlgorithmException e ) {
			Log.e( TAG, e.toString() );
		} catch ( Exception e ) {
			Log.e( TAG, e.toString() );
		}
		// Log.d(TAG, "Done printing hashkey");
	}

	public void showPhotoDialog( String[] urls, int position, OnPhotoPagerDialogPageChangedListener callback ) {
		if ( urls == null || urls.length < 1 )
			return;
		if ( photoDialog != null )
			hidePhotoDialog();

		photoDialog = new PhotoPagerDialog( this, urls, callback );
		photoDialog.setPosition( position );
		photoDialog.show();

		hideTitle();
	}

	public void showPhotoDialog( final ImageView thumbView, String[] urls, int position, OnPhotoPagerDialogPageChangedListener callback ) {
		if ( urls == null || urls.length < 1 )
			return;
		if ( photoDialog != null )
			hidePhotoDialog();

		photoDialog = new PhotoPagerDialog( this, urls, callback );
		photoDialog.setPosition( position );
		photoDialog.show();

		hideTitle();

	}

	public boolean isPhotoDialogShown() {
		return photoDialog != null;
	}

	public void hidePhotoDialog() {
		if ( photoDialog == null )
			return;

		photoDialog.destroy();
		photoDialog = null;

		showTitle();
	}

	public void setDrawersEnabled( boolean enabled ) {
		menuDrawer.setTouchMode( enabled ? MenuDrawer.TOUCH_MODE_BEZEL : MenuDrawer.TOUCH_MODE_NONE );
		menuToggler.setEnabled( enabled );
	}

	@TargetApi (Build.VERSION_CODES.ICE_CREAM_SANDWICH )
	public void hideTitle() {
		try {
			( (View) findViewById( android.R.id.title ).getParent() ).setVisibility( View.GONE );
		} catch ( Exception e ) {
		}
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN );
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
			getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LOW_PROFILE );
	}

	@TargetApi (Build.VERSION_CODES.ICE_CREAM_SANDWICH )
	public void showTitle() {
		try {
			( (View) findViewById( android.R.id.title ).getParent() ).setVisibility( View.VISIBLE );
		} catch ( Exception e ) {
		}
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN );
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
			getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_VISIBLE );
	}

	public void setRefreshingNieuws( boolean refreshing ) {
		refreshingNieuws = refreshing;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onMenuOpened( int featureId, Menu menu ) {
		if ( popupMenu == null )
			return super.onMenuOpened( featureId, menu );
		else
			popupMenu.show();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		return menuListener.onMenuItemClick( item );
	}

	private void onFacebookClick() {
		try {
			getPackageManager().getPackageInfo( "com.facebook.katana", 0 );
			startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "fb://profile/294105307313875" ) ) );
		} catch ( Exception e ) {
			startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://www.facebook.com/svjuliana32" ) ) );
		}
	}

	private void onTwitterClick() {
		try {
			Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "twitter://user?screen_name=svjuliana32" ) );
			startActivity( intent );
		} catch ( Exception e ) {
			startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://twitter.com/#!/svjuliana32" ) ) );
		}
	}

	private void onWebsiteClick() {
		String url = "http://www.svjuliana32.nl";
		Intent i = new Intent( Intent.ACTION_VIEW );
		i.setData( Uri.parse( url ) );
		startActivity( i );
	}

	private void onSettingsClick() {
		Intent i = new Intent( MainActivity.this, SettingsActivity.class );
		startActivity( i );
	}

	private OnMenuItemClickListener menuListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem item ) {
			switch ( item.getItemId() ) {
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

	public void notifyTeletekstGone() {
		menuAdapter.removeTeletekst();
	}

	public void setTabs( TabPageIndicator tabs ) {
		this.teamTabs = tabs;
	}

	// FACEBOOK
	public void getFacebookUser( final FacebookProfileCallback callback ) {
		if ( facebookUser == null ) {
			Request userRequest = Request.newMeRequest( Session.getActiveSession(), new GraphUserCallback() {

				@Override
				public void onCompleted( GraphUser user, Response response ) {

					Log.d( "facebook_login", "getFacebookUser response: " + ( response == null ? "null" : response.toString() ) );

					if ( user != null )
						facebookUser = new UserInfo( user.getName(), user.getId() );

					callback.onFacebookProfileRetrieved( facebookUser );
				}
			} );
			userRequest.executeAsync();

		} else {
			callback.onFacebookProfileRetrieved( facebookUser );
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback( statusCallback );
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback( statusCallback );
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult( requestCode, resultCode, data );
		Session.getActiveSession().onActivityResult( this, requestCode, resultCode, data );
	}

	public void onClickLogin() {
		Session session = Session.getActiveSession();
		if ( !session.isOpened() && !session.isClosed() ) {
			session.openForRead( new Session.OpenRequest( this ).setPermissions( "basic_info" ).setCallback( statusCallback ) );
		} else {
			Session.openActiveSession( this, true, statusCallback );
		}
	}

	public static void onClickLogout() {
		Session session = Session.getActiveSession();
		if ( !session.isClosed() ) {
			session.closeAndClearTokenInformation();
		}
	}

	private StatusCallback statusCallback = new StatusCallback() {

		@Override
		public void call( Session session, SessionState state, Exception exception ) {
			Log.d( "facebook_login", "Session: " + ( session == null ? "null" : session.toString() ) + ", Exception: "
					+ ( exception == null ? "null" : exception.toString() ) );

			if ( isNieuwsDetailShown() )
				nieuwsDetail.onFacebookSessionChange( session );

		}
	};

	private void deletePermissions() {
		Session session = Session.getActiveSession();
		if ( session.getPermissions().size() > 0 ) {
			Request permissionRequest = Request.newGraphPathRequest( session, "me/permissions", new Callback() {

				@Override
				public void onCompleted( Response response ) {
					Log.d( "facebook_login_permissions", response == null ? "null" : response.toString() );

				}
			} );
			permissionRequest.setHttpMethod( HttpMethod.DELETE );
			permissionRequest.executeAsync();
		}
	}

	// END FACEBOOK
}

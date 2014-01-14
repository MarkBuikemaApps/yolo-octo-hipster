package com.markbuikema.juliana32.section;

import java.util.List;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.SettingsActivity;
import com.markbuikema.juliana32.adapter.NieuwsAdapter;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshAttacher;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshAttacher.OnRefreshListener;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshLayout;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.Util;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnScrollDirectionChangeListener;

public class Nieuws {

	private final static String TAG = "Nieuws";

	private StaggeredGridView nieuwsList;
	private PullToRefreshAttacher refresherAttacher;

	private NieuwsAdapter nieuwsAdapter;
	private MainActivity activity;

	private String itemRequestId;

	private NieuwsRetriever nieuwsRetriever;

	private TextView noItems;

	public Nieuws( final Context ctx ) {
		activity = (MainActivity) ctx;
		View mainView = activity.findViewById( R.id.nieuwsView );
		nieuwsList = (StaggeredGridView) mainView.findViewById( R.id.nieuwsList );
		noItems = (TextView) mainView.findViewById( R.id.noItems );

		nieuwsAdapter = new NieuwsAdapter( ctx );
		nieuwsList.setAdapter( nieuwsAdapter );
		noItems.setText( nieuwsAdapter.getCount() < 1 ? activity.getResources().getString( R.string.no_item ) : "" );

		nieuwsList.setSelector( null );
		nieuwsList.setOnScrollDirectionChangeListener( new OnScrollDirectionChangeListener() {

			@Override
			public void onScrollDirectionChange( boolean scrollingDown ) {
				nieuwsAdapter.setScrollDirection( scrollingDown );
			}

		} );

		PullToRefreshLayout ptrLayout = (PullToRefreshLayout) mainView.findViewById( R.id.nieuwsListRefresher );
		refresherAttacher = PullToRefreshAttacher.get( activity );
		ptrLayout.setPullToRefreshAttacher( refresherAttacher, new OnRefreshListener() {

			@Override
			public void onRefreshStarted( View view ) {
				refresh();
			}
		} );
	}

	public void refresh() {
		activity.setRefreshingNieuws( true );
		activity.fixActionBar();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( activity, 0 );
		boolean facebook = prefs.getBoolean( SettingsActivity.FACEBOOK, true );
		boolean website = prefs.getBoolean( SettingsActivity.WEBSITE, true );

		nieuwsRetriever = new NieuwsRetriever( facebook, website ) {

			@Override
			protected void onPreExecute() {
				Log.d( "nieuws", "start refresh" );
				refresherAttacher.setRefreshing( true );
				if ( nieuwsAdapter.getCount() == 0 )
					noItems.setText( "Nieuwsitems laden..." );
				else
					noItems.setText( "" );
			}

			@Override
			protected void onPostExecute( List<NieuwsItem> result ) {
				nieuwsAdapter.clear();
				activity.setRefreshingNieuws( false );

				DataManager.getInstance().setNieuwsItems( result );
				nieuwsAdapter.update();
				Log.d( "nieuws", "done loading nieuws" );

				if ( itemRequestId != null ) {

					NieuwsItem item = DataManager.getInstance().getNieuwsItemById( itemRequestId );
					if ( item != null )
						activity.requestNieuwsDetailPage( item, null );

					itemRequestId = null;
				}

				noItems.setText( nieuwsAdapter.getCount() < 1 ? activity.getResources().getString( R.string.no_item ) : "" );

				refresherAttacher.setRefreshComplete();
				activity.fixActionBar();
			}

			@Override
			public void onPhotosLoaded() {
				invalidate();
				Util.linkPhotosToTeam();
			}
		};

		nieuwsRetriever.execute();
	}

	public void setItemRequest( String nieuwsId ) {
		itemRequestId = nieuwsId;
	}

	public void invalidate() {
		if ( nieuwsAdapter != null )
			nieuwsAdapter.notifyDataSetChanged();
	}

	public void search( String s ) {
		if ( nieuwsAdapter == null )
			return;
		nieuwsAdapter.setSearchword( s );
		if ( nieuwsAdapter.getCount() == 0 )
			noItems.setText( "Uw zoekopdracht heeft geen resultaten opgeleverd." );
		else
			noItems.setText( nieuwsAdapter.getCount() < 1 ? activity.getResources().getString( R.string.no_item ) : "" );

	}

	public void clearSearch() {
		if ( nieuwsAdapter != null )
			nieuwsAdapter.clearSearchword();
	}

	public int getAdapterCount() {
		if ( nieuwsAdapter == null )
			return 0;
		
		return nieuwsAdapter.getCount();
	}

	public void updateMessage() {
		if ( nieuwsAdapter == null )
			return;
		if ( nieuwsAdapter.getCount() == 0 )
			noItems.setText( activity.getResources().getString( R.string.no_item ) );
		else
			noItems.setText( "" );
	}

	public boolean isRefreshing() {
		return refresherAttacher.isRefreshing();
	}

	public void setScrollingEnabled( boolean enabled ) {
		nieuwsList.setScrollingEnabled( enabled );
		refresherAttacher.setEnabled( enabled );
	}

	public boolean isAdapterEmpty() {
		return nieuwsAdapter.getCount() <= nieuwsAdapter.getColumnCount();
	}

	public void animateNormalNieuwsDetail( boolean in, final View animatedView ) {
		Animation a = new Animation() {

			@Override
			protected void applyTransformation( float interpolatedTime, Transformation t ) {

				animatedView.requestLayout();
				animatedView.invalidate();
				Log.d( "applytransformation", "time: " + interpolatedTime );
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		a.setDuration( 3000 );
		animatedView.setAnimation( a );
	}

	public void fadeList( boolean out ) {
		ViewPropertyAnimator.animate( nieuwsList ).alpha( out ? .05f : 1 ).setDuration( 200 );
	}

	public void setAnimationsEnabled(boolean b) {
		nieuwsAdapter.setAnimationsEnabled(b);
	}
}

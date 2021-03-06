package com.markbuikema.juliana32.adapter;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.util.Util;

public class TeamAdapter extends ArrayAdapter<Team> {

	private Category cat;

	private int topMargin;
	private int columnCount;

	public TeamAdapter( Context context, Category cat, List<Team> teams ) {
		super( context, 0, teams );
		this.cat = cat;

		Resources res = context.getResources();
		int stackedActionBarHeight = res.getDimensionPixelSize( R.dimen.action_bar_height_stacked );
		int actionBarHeight = res.getDimensionPixelSize( R.dimen.action_bar_height );
		
		boolean landscape = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		boolean tablet = res.getBoolean( R.bool.isTablet );
		if (!landscape && !tablet)
			topMargin = stackedActionBarHeight;
		else
			topMargin = actionBarHeight;
			

		columnCount = context.getResources().getInteger( R.integer.teamColumns );
	}

	@Override
	public View getView( int position, View convertView, ViewGroup parent ) {

		if ( position < columnCount ) {
			View topView = new View( getContext() );
			LayoutParams lp = new LayoutParams( LayoutParams.MATCH_PARENT, topMargin );
			topView.setLayoutParams( lp );
			return topView;
		}

		if ( convertView == null )
			convertView = LayoutInflater.from( getContext() ).inflate( R.layout.listitem_team );

		ImageView teamPhoto = (ImageView) convertView.findViewById( R.id.teamGridPicture );
		TextView teamName = (TextView) convertView.findViewById( R.id.teamGridName );

		final Team team = getItem( position );

		UrlImageViewHelper.setUrlDrawable( teamPhoto, team.getTeamPhotoUrl(), R.drawable.team_placeholder );
		teamName.setTypeface( Util.getRobotoLight( getContext() ) );
		teamName.setText( team.getName() );

		teamPhoto.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {
				( (MainActivity) getContext() ).requestTeamDetailPage( team );
			}
		} );

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType( int position ) {
		return position < columnCount ? 0 : 1;
	}

	@Override
	public int getCount() {
		return super.getCount() + columnCount;
	}

	@Override
	public long getItemId( int position ) {
		return position < columnCount ? 0 : position - columnCount;
	}

	@Override
	public Team getItem( int position ) {
		return position < columnCount ? null : super.getItem( position - columnCount );
	}

}
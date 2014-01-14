package com.markbuikema.juliana32.adapter;

import org.holoeverywhere.widget.GridView;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.ui.TabTransformer;

public class TeamsPagerAdapter extends PagerAdapter {

	private TeamAdapter[] adapters;

	public TeamsPagerAdapter() {
	}

	@Override
	public Object instantiateItem( ViewGroup container, int position ) {
		GridView view = (GridView) LayoutInflater.from( container.getContext() ).inflate( R.layout.grid_teams, null );

		view.setAdapter( adapters[ position ] );

		boolean landscape = container.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		boolean tablet = container.getContext().getResources().getBoolean( R.bool.isTablet );
		if ( !landscape && !tablet )
			view.setOnScrollListener( new TabTransformer( (Activity) container.getContext() ) );
		container.addView( view );

		return view;
	}

	@Override
	public int getCount() {
		return adapters == null ? 0 : adapters.length;
	}

	public void setAdapters( TeamAdapter[] adapters ) {
		this.adapters = adapters;
		notifyDataSetChanged();
	}

	@Override
	public boolean isViewFromObject( View arg0, Object arg1 ) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem( ViewGroup container, int position, Object object ) {
		container.removeView( (View) object );
	}

	@Override
	public CharSequence getPageTitle( int position ) {
		return Category.values()[ position ].getName();
	}

}

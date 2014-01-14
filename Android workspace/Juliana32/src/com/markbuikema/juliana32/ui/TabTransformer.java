package com.markbuikema.juliana32.ui;

import android.app.Activity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.markbuikema.juliana32.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author Mark Buikema
 */
public class TabTransformer implements OnScrollListener {

	private int prevFVI;
	private int prevScrollY;
	private boolean prevScrollingDown;

	private int tabY;
	private int tabAnchorY;

	private int actionBarHeight;

	private View tabs;
	private View actionBar;

	public TabTransformer( Activity act ) {

		tabs = act.findViewById( R.id.teamsTabsIndicator );
		actionBar = act.findViewById( R.id.actionBar );

		actionBarHeight = act.getResources().getDimensionPixelSize( R.dimen.action_bar_height );
	}

	@Override
	public void onScrollStateChanged( AbsListView view, int scrollState ) {

	}

	@Override
	public void onScroll( AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
		//only works when the scrolling view has at least 1 child
		if ( view.getChildCount() == 0 )
			return;

		int newScrollY = view.getChildAt( 0 ).getTop();

		// iff firstVisibleItem changed, exit method
		if ( prevFVI != firstVisibleItem ) {
			int scrollDifference = prevScrollY - newScrollY;
			tabAnchorY -= scrollDifference;

			prevFVI = firstVisibleItem;
			prevScrollY = newScrollY;
			return;
		}

		boolean scrollingDown = newScrollY > prevScrollY;

		// iff scrolling direction changed, change the starting point of
		// movement
		if ( prevScrollingDown != scrollingDown ) {
			tabAnchorY = newScrollY - tabY;
		}

		// calculate the new translation
		tabY = newScrollY - tabAnchorY;
		if ( tabY < -actionBarHeight )
			tabY = -actionBarHeight;
		if ( tabY > 0 )
			tabY = 0;

		// translate the views
		ViewHelper.setTranslationY( tabs, tabY );
		ViewHelper.setTranslationY( actionBar, tabY );

		//save current values for next time the view is scrolled
		prevScrollingDown = scrollingDown;
		prevScrollY = newScrollY;

	}
}
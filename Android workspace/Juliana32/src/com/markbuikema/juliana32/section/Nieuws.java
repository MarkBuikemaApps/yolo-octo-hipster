package com.markbuikema.juliana32.section;

import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.TextView;

import android.util.Log;
import android.view.View;

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
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;
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

	public Nieuws(final Activity act) {
		activity = (MainActivity) act;
		View mainView = act.findViewById(R.id.nieuwsView);
		nieuwsList = (StaggeredGridView) mainView.findViewById(R.id.nieuwsList);
		noItems = (TextView) mainView.findViewById(R.id.noItems);

		nieuwsAdapter = new NieuwsAdapter(act);
		nieuwsList.setAdapter(nieuwsAdapter);
		noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

		nieuwsList.setSelector(null);
		nieuwsList.setOnScrollDirectionChangeListener(new OnScrollDirectionChangeListener() {

			@Override
			public void onScrollDirectionChange(boolean scrollingDown) {
				nieuwsAdapter.setScrollDirection(scrollingDown);
			}

		});

		PullToRefreshLayout ptrLayout = (PullToRefreshLayout) mainView.findViewById(R.id.nieuwsListRefresher);
		refresherAttacher = PullToRefreshAttacher.get(act);
		ptrLayout.setPullToRefreshAttacher(refresherAttacher, new OnRefreshListener() {

			@Override
			public void onRefreshStarted(View view) {
				refresh();
			}
		});

		nieuwsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
				Nieuws.this.onItemClick(position);
			}
		});
	}

	public void onItemClick(int position) {

		NieuwsItem item = nieuwsAdapter.getItem(position);
		activity.requestNieuwsDetailPage(item);
	}

	public void refresh() {
		activity.setRefreshingNieuws(true);
		activity.fixActionBar();

		SharedPreferences prefs = activity.getSharedPreferences(SettingsActivity.PREFERENCES, 0);
		boolean facebook = prefs.getBoolean(SettingsActivity.FACEBOOK, true);
		boolean website = prefs.getBoolean(SettingsActivity.WEBSITE, true);

		nieuwsRetriever = new NieuwsRetriever(facebook, website) {

			@Override
			protected void onPreExecute() {
				Log.d("nieuws", "start refresh");
				refresherAttacher.setRefreshing(true);
				if (nieuwsAdapter.getCount() == 0)
					noItems.setText("Nieuwsitems laden...");
				else
					noItems.setText("");
			}

			@Override
			protected void onPostExecute(List<NieuwsItem> result) {
				nieuwsAdapter.clear();
				activity.setRefreshingNieuws(false);

				DataManager.getInstance().setNieuwsItems(result);
				nieuwsAdapter.update();
				Log.d("nieuws", "done loading nieuws");

				if (itemRequestId != null) {

					NieuwsItem item = DataManager.getInstance().getNieuwsItemById(itemRequestId);
					if (item != null)
						activity.requestNieuwsDetailPage(item);

					itemRequestId = null;
				}

				noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

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

	public void setItemRequest(String nieuwsId) {
		itemRequestId = nieuwsId;
	}

	public void invalidate() {
		if (nieuwsAdapter != null)
			nieuwsAdapter.notifyDataSetChanged();
	}

	public void search(String s) {
		if (nieuwsAdapter == null)
			return;
		nieuwsAdapter.setSearchword(s);
		if (nieuwsAdapter.getCount() == 0)
			noItems.setText("Uw zoekopdracht heeft geen resultaten opgeleverd.");
		else
			noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

	}

	public void clearSearch() {
		if (nieuwsAdapter != null)
			nieuwsAdapter.clearSearchword();
	}

	public int getAdapterCount() {
		if (nieuwsAdapter == null)
			return 0;
		return nieuwsAdapter.getCount();
	}

	public void updateMessage() {
		if (nieuwsAdapter == null)
			return;
		if (nieuwsAdapter.getCount() == 0)
			noItems.setText(activity.getResources().getString(R.string.no_item));
		else
			noItems.setText("");
	}

	public boolean isRefreshing() {
		return refresherAttacher.isRefreshing();
	}

	public boolean isAdapterEmpty() {
		return nieuwsAdapter.getCount() <= nieuwsAdapter.getColumnCount();
	}
}

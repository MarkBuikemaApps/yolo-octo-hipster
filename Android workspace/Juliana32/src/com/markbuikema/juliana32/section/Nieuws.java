package com.markbuikema.juliana32.section;

import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.TextView;

import android.view.View;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.SettingsActivity;
import com.markbuikema.juliana32.adapter.NieuwsAdapter;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshAttacher;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshAttacher.OnRefreshListener;
import com.markbuikema.juliana32.ui.pulltorefresh.PullToRefreshLayout;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper.PhotoGetter;
import com.markbuikema.juliana32.util.Util;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

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
		nieuwsList.setSelector(null);

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

		noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");
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
				refresherAttacher.setRefreshing(true);
				if (nieuwsAdapter.getCount() == 0)
					noItems.setText("Nieuwsitems laden...");
				else
					noItems.setText("");
			}

			@Override
			protected void onPostExecute(List<NieuwsItem> result) {
				nieuwsAdapter.clear();

				DataManager.getInstance().setNieuwsItems(result);
				nieuwsAdapter.update();

				for (NieuwsItem item : result)
					if (item.isFromFacebook()) {
						final FacebookNieuwsItem fbni = ((FacebookNieuwsItem) item);
						if (fbni.isPhoto())
							new PhotoGetter() {
								@Override
								protected void onPostExecute(List<String> result) {
									for (String photo : result)
										fbni.addPhoto(photo);
									onPhotoLoaded();
									// Log.d("ADDED_PHOTOS", "title: " + fbni.getTitle() +
									// "count: " + fbni.getPhotoCount());
								}

							}.execute(fbni);
					}

				if (itemRequestId != null) {

					NieuwsItem item = getNewsItem(itemRequestId);
					activity.requestNieuwsDetailPage(item);

					itemRequestId = null;
				}

				noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

				refresherAttacher.setRefreshComplete();
				activity.setRefreshingNieuws(false);
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

	public NieuwsItem getNewsItem(String itemRequestId) {
		for (int i = 0; i < nieuwsAdapter.getCount(); i++)
			if (nieuwsAdapter.getItem(i).getId().equals(itemRequestId))
				return nieuwsAdapter.getItem(i);
		return null;
	}

	public void setItemRequest(String nieuwsId) {
		itemRequestId = nieuwsId;
	}

	public void invalidate() {
		nieuwsAdapter.notifyDataSetChanged();
	}

	public void search(String s) {
		nieuwsAdapter.setSearchword(s);
		if (nieuwsAdapter.getCount() == 0)
			noItems.setText("Uw zoekopdracht heeft geen resultaten opgeleverd.");
		else
			noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

	}

	public void clearSearch() {
		nieuwsAdapter.clearSearchword();
	}

	public int getAdapterCount() {
		return nieuwsAdapter.getCount();
	}

	public void updateMessage() {
		if (nieuwsAdapter.getCount() == 0)
			noItems.setText(activity.getResources().getString(R.string.no_item));
		else
			noItems.setText("");
	}
}

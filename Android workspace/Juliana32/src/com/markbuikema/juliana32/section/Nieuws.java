package com.markbuikema.juliana32.section;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.MainActivity.Page;
import com.markbuikema.juliana32.activity.SettingsActivity;
import com.markbuikema.juliana32.adapter.NieuwsAdapter;
import com.markbuikema.juliana32.asynctask.NieuwsRetriever;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.util.DataManager;
import com.markbuikema.juliana32.util.FacebookHelper.PhotoGetter;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

public class Nieuws {

	private final static String TAG = "Nieuws";

	private StaggeredGridView nieuwsList;
	private NieuwsAdapter nieuwsAdapter;
	private ImageButton refreshButton;
	private ProgressBar loading;
	private MainActivity activity;

	private int itemRequestId = -1;

	private NieuwsRetriever nieuwsRetriever;

	private TextView noItems;

	public Nieuws(final Activity act) {
		activity = (MainActivity) act;
		View mainView = act.findViewById(R.id.nieuwsView);
		nieuwsList = (StaggeredGridView) mainView.findViewById(R.id.nieuwsList);
		refreshButton = (ImageButton) act.findViewById(R.id.menuRefresh);
		loading = (ProgressBar) act.findViewById(R.id.loading);
		noItems = (TextView) mainView.findViewById(R.id.noItems);

		nieuwsAdapter = new NieuwsAdapter(act);

		nieuwsList.setAdapter(nieuwsAdapter);
		nieuwsList.setSelector(null);

		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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

	public void showRefreshButton() {
		refreshButton.setVisibility(View.VISIBLE);
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
				nieuwsAdapter.clear();
				loading.setVisibility(View.VISIBLE);
				refreshButton.setVisibility(View.GONE);
				noItems.setText("");
			}

			@Override
			protected void onPostExecute(List<NieuwsItem> result) {

				DataManager.getInstance().setNieuwsItems(result);
				nieuwsAdapter.update();

				loading.setVisibility(View.GONE);
				if (activity.getPage() == Page.NIEUWS && !activity.isNieuwsDetailShown())
					refreshButton.setVisibility(View.VISIBLE);

				for (NieuwsItem item : result)
					if (item.isFromFacebook()) {
						final FacebookNieuwsItem fbni = ((FacebookNieuwsItem) item);
						if (fbni.isPhoto())
							new PhotoGetter() {
								@Override
								protected void onPostExecute(List<String> result) {
									for (String photo : result)
										fbni.addPhoto(photo);
									// Log.d("ADDED_PHOTOS", "title: " + fbni.getTitle() +
									// "count: " + fbni.getPhotoCount());

								}

							}.execute(fbni);
					}

				if (itemRequestId != -1) {

					NieuwsItem item = getNewsItem(itemRequestId);
					activity.requestNieuwsDetailPage(item);

					itemRequestId = -1;
				}

				noItems.setText(nieuwsAdapter.getCount() < 1 ? activity.getResources().getString(R.string.no_item) : "");

				activity.setRefreshingNieuws(false);
				activity.fixActionBar();
			}

		};

		nieuwsRetriever.execute();
	}

	public NieuwsItem getNewsItem(int newsId) {
		for (int i = 0; i < nieuwsAdapter.getCount(); i++)
			if (nieuwsAdapter.getItem(i).getId() == newsId)
				return nieuwsAdapter.getItem(i);
		return null;
	}

	public void setItemRequest(int newsId) {
		itemRequestId = newsId;
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

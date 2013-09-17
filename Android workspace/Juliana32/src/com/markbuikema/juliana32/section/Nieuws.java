package com.markbuikema.juliana32.section;

import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.activity.MainActivity.Page;
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

	public Nieuws(final Activity act) {
		activity = (MainActivity) act;
		View mainView = act.findViewById(R.id.nieuwsView);
		nieuwsList = (StaggeredGridView) mainView.findViewById(R.id.nieuwsList);
		refreshButton = (ImageButton) act.findViewById(R.id.menuRefresh);
		loading = (ProgressBar) act.findViewById(R.id.loading);

		nieuwsAdapter = new NieuwsAdapter(act);
		for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
			nieuwsAdapter.add(item);

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

	}

	public void onItemClick(int position) {

		NieuwsItem item = nieuwsAdapter.getItem(position);
		activity.requestNiewsDetailPage(item);
	}

	public void showRefreshButton() {
		refreshButton.setVisibility(View.VISIBLE);
	}

	public void refresh() {
		nieuwsRetriever = new NieuwsRetriever() {

			@Override
			protected void onPreExecute() {
				nieuwsAdapter.clear();
				loading.setVisibility(View.VISIBLE);
				refreshButton.setVisibility(View.GONE);
			}

			@Override
			protected void onPostExecute(List<NieuwsItem> result) {

				DataManager.getInstance().setNieuwsItems(result);

				loading.setVisibility(View.GONE);
				if (activity.getPage() == Page.NIEUWS && !activity.isNieuwsDetailShown())
					refreshButton.setVisibility(View.VISIBLE);

				for (NieuwsItem item : result) {
					if (item.isFromFacebook()) {
						final FacebookNieuwsItem fbi = ((FacebookNieuwsItem) item);
						if (fbi.isPhoto())
							new PhotoGetter() {
								@Override
								protected void onPostExecute(List<String> result) {
									for (String photo : result)
										fbi.addPhoto(photo);
									Log.d("ADDED_PHOTOS", "title: " + fbi.getTitle() + "count: " + fbi.getPhotoCount());

								}

							}.execute(fbi);
					}
					nieuwsAdapter.add(item);

				}

				if (itemRequestId != -1) {

					NieuwsItem item = getNewsItem(itemRequestId);
					activity.requestNiewsDetailPage(item);

					itemRequestId = -1;
				}
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

}

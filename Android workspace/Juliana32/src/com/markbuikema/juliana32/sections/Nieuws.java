package com.markbuikema.juliana32.sections;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.activities.MainActivity.Page;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.tools.DataManager;
import com.markbuikema.juliana32.tools.NieuwsRetriever;
import com.markbuikema.juliana32.tools.Tools;

public class Nieuws {

	private final static String TAG = "Nieuws";

	private ListView nieuwsList;
	private NieuwsAdapter nieuwsAdapter;
	private ImageButton refreshButton;
	private ProgressBar loading;
	private MainActivity activity;

	private int itemRequestId = -1;

	private NieuwsRetriever nieuwsRetriever;

	public Nieuws(final Activity act) {
		activity = (MainActivity) act;
		View mainView = act.findViewById(R.id.nieuwsView);
		nieuwsList = (ListView) mainView.findViewById(R.id.nieuwsList);
		refreshButton = (ImageButton) act.findViewById(R.id.menuRefresh);
		loading = (ProgressBar) act.findViewById(R.id.loading);

		nieuwsAdapter = new NieuwsAdapter(act);
		nieuwsList.setAdapter(nieuwsAdapter);

		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		nieuwsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NieuwsItem item = nieuwsAdapter.getItem(arg2);
				activity.requestNiewsDetailPage(item);
			}
		});

		refresh();

	}

	public void showRefreshButton() {
		refreshButton.setVisibility(View.VISIBLE);
	}

	private class NieuwsAdapter extends ArrayAdapter<NieuwsItem> {

		private Context context;

		public NieuwsAdapter(Context context) {
			super(context, 0);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NieuwsItem item = getItem(position);

			if (item instanceof NormalNieuwsItem) {

				if (convertView == null || (convertView != null && convertView.findViewById(R.id.titleView) == null))
					convertView = LayoutInflater.from(context).inflate(R.layout.listitem_nieuwsitem, null);

				TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
				TextView subTitleView = (TextView) convertView.findViewById(R.id.subTitleView);
				titleView.setText(item.getTitle());
				subTitleView.setText(item.getSubTitle());
				TextView createdAtView = (TextView) convertView.findViewById(R.id.createdAtView);
				createdAtView.setText(Tools.getDateString(((NormalNieuwsItem) item).getCreatedAt()));

				convertView.requestLayout();
				return convertView;
			} else
				if (item instanceof TeaserNieuwsItem) {
					if (convertView == null || (convertView != null && convertView.findViewById(R.id.teaserImage) == null))
						convertView = LayoutInflater.from(context).inflate(R.layout.listitem_teaseritem, null);

					TextView titleView = (TextView) convertView.findViewById(R.id.teaserTitle);
					ImageView imageView = (ImageView) convertView.findViewById(R.id.teaserImage);
					titleView.setText(item.getTitle());
					Bitmap bmp = ((TeaserNieuwsItem) item).getImage();
					if (bmp != null)
						imageView.setImageBitmap(bmp);

					convertView.requestLayout();
					return convertView;
				} else {
					if (convertView == null || (convertView != null && convertView.findViewById(R.id.facebook_content) == null))
						convertView = LayoutInflater.from(context).inflate(R.layout.listitem_facebookitem, null);
					TextView content = (TextView) convertView.findViewById(R.id.facebook_content);
					TextView likeCount = (TextView) convertView.findViewById(R.id.facebook_likecount);
					TextView createdAt = (TextView) convertView.findViewById(R.id.facebook_date);

					FacebookNieuwsItem fbItem = (FacebookNieuwsItem) item;
					content.setText(fbItem.getContent());
					likeCount.setText(fbItem.getLikeCount() + " likes");
					createdAt.setText(Tools.getDateString(fbItem.getCreatedAt()));

					convertView.requestLayout();
					return convertView;
				}

		}

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
				for (NieuwsItem item : result)
					nieuwsAdapter.add(item);

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
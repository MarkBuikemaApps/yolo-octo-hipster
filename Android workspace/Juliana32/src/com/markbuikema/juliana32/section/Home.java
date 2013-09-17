package com.markbuikema.juliana32.section;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.FixtureAdapter;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.util.DataManager;

public class Home {

	private static final String TAG = "Home Section";
	private static final int MARGIN = 20;

	private MainActivity activity;

	private ListView fixtures;
	private FixtureAdapter fixtureAdapter;
	private LinearLayout teaserContainer;
	private List<TeaserNieuwsItem> teasers;

	private int teaserWidth;
	private int teaserHeight;

	public Home(MainActivity act) {
		activity = act;
		View mainView = act.findViewById(R.id.homeView);
		fixtures = (ListView) mainView.findViewById(R.id.fixtures);
		teaserContainer = (LinearLayout) mainView.findViewById(R.id.teaserContainer);

		teasers = new ArrayList<TeaserNieuwsItem>();

		setTeaserDimensions();

		populateGames();

		for (TeaserNieuwsItem item : DataManager.getInstance().getTeaserItems())
			addTeaser(item);
	}

	public void populateGames() {
		ArrayList<Game> games = activity.getLatestGames();
		fixtureAdapter = new FixtureAdapter(activity, games);
		fixtures.setAdapter(fixtureAdapter);
	}

	private void setTeaserDimensions() {
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		teaserWidth = (d.getWidth() / 3) * 2;
		float height = (teaserWidth / 246f) * 96f;
		teaserHeight = (int) height;
	}

	@SuppressLint("NewApi")
	public void addTeaser(final TeaserNieuwsItem item) {
		Log.d(TAG, item.toString());
		teasers.add(item);
		View view = LayoutInflater.from(activity).inflate(R.layout.listitem_teaseritem, null);
		TextView teaserText = (TextView) view.findViewById(R.id.teaserTitle);
		ImageView teaserImage = (ImageView) view.findViewById(R.id.teaserImage);
		teaserText.setText(item.getTitle());
		BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), item.getImage());
		teaserImage.setImageDrawable(drawable);
		LayoutParams params = new LayoutParams(teaserWidth, teaserHeight);
		params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		params.gravity = teasers.size() % 2 == 1 ? Gravity.LEFT : Gravity.RIGHT;
		view.setLayoutParams(params);

		view.setClickable(true);
		view.setContentDescription(item.getSubTitle());

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.requestNiewsDetailPage(item);
			}
		});

		teaserContainer.addView(view);
	}

}
package com.markbuikema.juliana32.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.asynctask.PictureChanger;

public class SettingsActivity extends Activity {

	private ImageButton backButton;
	private ListView settingsList;
	private SettingsAdapter adapter;
	private SharedPreferences preferences;

	private Setting facebookCaption;
	private Setting facebookSetting;

	public final static String PREFERENCES = "Juliana32_instellingen";
	public final static String NOTIFICATIONS = "Juliana32_notificaties";
	public final static String FACEBOOK = "Juliana32_facebook";
	public final static String WEBSITE = "Juliana32_website";

	private String userName;
	private String userPicUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		userName = getIntent().getStringExtra("userName");
		userPicUrl = getIntent().getStringExtra("userPicUrl");

		preferences = getSharedPreferences(PREFERENCES, 0);

		backButton = (ImageButton) findViewById(R.id.backButton);
		settingsList = (ListView) findViewById(R.id.settingsList);

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		adapter = new SettingsAdapter(this, 0);
		settingsList.setAdapter(adapter);
		settingsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Setting clickedSetting = adapter.getItem(pos);
				if (clickedSetting instanceof CheckBoxSetting)
					((CheckBoxSetting) clickedSetting).toggle();
				if (clickedSetting instanceof FacebookSetting)
					onClickLogout();
			}
		});
		adapter.add(new SettingCaption("Nieuwsberichten"));
		adapter.add(new CheckBoxSetting("Notificaties", "bij nieuwe nieuwsberichten", NOTIFICATIONS));
		adapter.add(new SettingCaption("Bronnen"));
		adapter.add(new CheckBoxSetting("Facebook", "", FACEBOOK));
		adapter.add(new CheckBoxSetting("Juliana website", "", WEBSITE));

		Session facebookSession = Session.getActiveSession();
		if (facebookSession.isOpened()) {

			facebookCaption = new SettingCaption("Facebook");
			facebookSetting = new FacebookSetting();

			adapter.add(facebookCaption);
			adapter.add(facebookSetting);
		}

	}

	public class SettingsAdapter extends ArrayAdapter<Setting> {

		public SettingsAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int pos, View v, ViewGroup vg) {
			return getItem(pos).getView();
		}
	}

	public class Setting {
		protected String title;
		protected String subTitle;
		protected View view;

		public Setting(String title, String subTitle, View view) {
			this.title = title;
			this.subTitle = subTitle;
			this.view = view;

			TextView titleView = (TextView) view.findViewById(R.id.settingTitle);
			if (titleView != null)
				titleView.setText(title);

			TextView subTitleView = (TextView) view.findViewById(R.id.settingSubTitle);
			if (subTitleView != null)
				subTitleView.setText(subTitle);

			if (titleView != null && (title == null || title.equals("")))
				titleView.setVisibility(View.GONE);
			if (subTitleView != null && (subTitle == null || subTitle.equals("")))
				subTitleView.setVisibility(View.GONE);
		}

		public View getView() {
			return view;
		}
	}

	public class CheckBoxSetting extends Setting {

		CheckBox checkBox;

		public CheckBoxSetting(String title, String subTitle, final String preferenceName) {
			super(title, subTitle, getLayoutInflater().inflate(R.layout.setting_checkbox, null));
			checkBox = (CheckBox) view.findViewById(R.id.settingCheckBox);
			checkBox.setChecked(preferences.getBoolean(preferenceName, false));
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Editor edit = preferences.edit();
					edit.putBoolean(preferenceName, isChecked);
					edit.commit();
				}
			});
		}

		public boolean isChecked() {
			return checkBox.isChecked();
		}

		public void toggle() {
			checkBox.setChecked(!checkBox.isChecked());
		}
	}

	public class FacebookSetting extends Setting {

		private ImageView picture;
		private TextView name;

		public FacebookSetting() {

			super("Uitloggen", userName, getLayoutInflater().inflate(R.layout.setting_facebook, null));

			picture = (ImageView) view.findViewById(R.id.settingFacebookPic);
			name = (TextView) view.findViewById(R.id.settingSubTitle);

			new PictureChanger() {
				@Override
				protected void onPostExecute(Bitmap result) {
					if (result == null)
						picture.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),
								R.drawable.silhouette)));
					else
						picture.setBackground(new BitmapDrawable(getResources(), result));
				}
			}.execute(userPicUrl);

			Toast.makeText(SettingsActivity.this, userName, Toast.LENGTH_LONG).show();

		}
	}

	public class SettingCaption extends Setting {

		public SettingCaption(String title) {
			super(title, "", getLayoutInflater().inflate(R.layout.setting_caption, null));
		}

	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed())
			session.closeAndClearTokenInformation();

		adapter.remove(facebookCaption);
		adapter.remove(facebookSetting);

	}

}
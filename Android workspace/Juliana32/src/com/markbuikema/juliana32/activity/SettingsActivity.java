package com.markbuikema.juliana32.activity;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.Editor;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Session;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.ui.Button;

public class SettingsActivity extends Activity {

	private LinearLayout backButton;
	private ListView settingsList;
	private SettingsAdapter adapter;
	private SharedPreferences preferences;

	private Setting facebookCaption;
	private Setting facebookSetting;
	private boolean showAbout = false;

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

		backButton = (LinearLayout) findViewById(R.id.backButton);
		settingsList = (ListView) findViewById(R.id.settingsList);
		((TextView) findViewById(R.id.titleText)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));

		backButton.setClickable(true);
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

				if (clickedSetting.getCallback() != null)
					clickedSetting.getCallback().onItemClick(arg0, arg1, pos, arg3);

			}
		});
		// adapter.add(new SettingCaption("Nieuwsberichten"));
		// adapter.add(new CheckBoxSetting("Notificaties",
		// "bij nieuwe nieuwsberichten", NOTIFICATIONS, false));
		adapter.add(new SettingCaption("Bronnen"));
		adapter.add(new CheckBoxSetting("Facebook", "", FACEBOOK, true));
		adapter.add(new CheckBoxSetting("Juliana website", "", WEBSITE, true));

		Session facebookSession = Session.getActiveSession();
		if (facebookSession.isOpened()) {

			facebookCaption = new SettingCaption("Facebook");
			facebookSetting = new FacebookSetting();

			adapter.add(facebookCaption);
			adapter.add(facebookSetting);
		}

		adapter.add(new SettingCaption("Info"));
		adapter.add(new Setting("Over deze app", "", new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showAboutDialog();
			}
		}));

		if (savedInstanceState != null) {
			showAbout = savedInstanceState.getBoolean("showAbout");
			if (showAbout)
				showAboutDialog();
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
		protected OnItemClickListener callback;

		private Setting(String title, String subTitle, View view) {
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

		public Setting(String title, String subTitle, OnItemClickListener callback) {
			this.title = title;
			this.subTitle = subTitle;
			this.callback = callback;
			view = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.setting_checkbox, null);

			view.findViewById(R.id.settingCheckBox).setVisibility(View.GONE);

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

		public OnItemClickListener getCallback() {
			return callback;
		}
	}

	public class CheckBoxSetting extends Setting {

		CheckBox checkBox;

		public CheckBoxSetting(String title, String subTitle, final String preferenceName, boolean defaultValue) {
			super(title, subTitle, getLayoutInflater().inflate(R.layout.setting_checkbox, null));
			checkBox = (CheckBox) view.findViewById(R.id.settingCheckBox);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Editor edit = preferences.edit();
					edit.putBoolean(preferenceName, isChecked);
					edit.commit();
				}
			});
			checkBox.setChecked(preferences.getBoolean(preferenceName, defaultValue));

			view.requestLayout();
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

		public FacebookSetting() {

			super("Uitloggen", userName, getLayoutInflater().inflate(R.layout.setting_facebook, null));

			picture = (ImageView) view.findViewById(R.id.settingFacebookPic);

			UrlImageViewHelper.setUrlDrawable(picture, userPicUrl, R.drawable.silhouette);
			view.requestLayout();

		}
	}

	public class SettingCaption extends Setting {

		public SettingCaption(String title) {
			super(title, "", getLayoutInflater().inflate(R.layout.setting_caption, null));
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();

		if (session.isOpened() && session.getPermissions().contains("publish_actions")) {
			Bundle b = new Bundle();
			b.putString("fields", "publish_actions");
			Request r = Request.newGraphPathRequest(session, "me/permissions", null);
			r.setHttpMethod(HttpMethod.DELETE);
			r.setParameters(b);
			Request.executeBatchAsync(r);
		}

		if (!session.isClosed())
			session.closeAndClearTokenInformation();

		adapter.remove(facebookCaption);
		adapter.remove(facebookSetting);

		Toast.makeText(this, "U bent uitgelogd", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("showAbout", showAbout);
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("NewApi")
	private void showAboutDialog() {

		showAbout = true;

		Builder b = new Builder(this);

		View view = LayoutInflater.from(this).inflate(R.layout.about, null);
		Button emailButton = (Button) view.findViewById(R.id.aboutEmail);

		final AlertDialog ad = b.setView(view).create();
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
				String uriString = "mailto:" + getString(R.string.about_email) + "?subject="
						+ getString(R.string.about_email_subject);
				uriString = uriString.replaceAll(" ", "%20");
				emailIntent.setData(Uri.parse(uriString));
				startActivity(Intent.createChooser(emailIntent, "Email versturen"));

				ad.dismiss();

			}
		});
		ad.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				showAbout = false;
			}
		});
		ad.show();

	}
}
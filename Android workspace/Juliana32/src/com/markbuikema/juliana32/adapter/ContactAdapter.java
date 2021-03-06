package com.markbuikema.juliana32.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Member;
import com.markbuikema.juliana32.util.Util;

@Deprecated
public class ContactAdapter extends ArrayAdapter<Member> {

	private List<List<Member>> content;
	private List<String> headers;
	private List<Boolean> isHeader;

	public ContactAdapter(Context context) {
		super(context, 0);
		try {
			content = new ArrayList<List<Member>>();
			headers = new ArrayList<String>();

			JSONObject object = new JSONObject(Util.loadJSONFromAsset(getContext(), "contacts.json"));
			JSONArray categories = object.getJSONArray("categories");
			for (int i = 0; i < categories.length(); i++) {
				JSONObject category = categories.getJSONObject(i);
				headers.add(category.getString("name"));
				content.add(new ArrayList<Member>());
				JSONArray members = category.getJSONArray("members");
				for (int j = 0; j < members.length(); j++) {
					JSONObject memberObject = members.getJSONObject(j);
					String name = memberObject.optString("name");
					String function = memberObject.optString("function");
					String phoneNumber = memberObject.optString("phoneNumber");
					String email = memberObject.optString("email");
					Member member = new Member(name, function, phoneNumber, email);
					content.get(i).add(member);
				}
			}

			Log.d("members", "added " + content.size() + " categories of members");

			isHeader = new ArrayList<Boolean>(getCount());
			for (List<Member> list : content) {
				isHeader.add(true);
				for (Member m : list)
					isHeader.add(false);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return isHeader.get(position).booleanValue() ? 0 : 1;
	}

	@Override
	public int getCount() {
		int count = 0;
		for (List<Member> memberList : content)
			count += memberList.size() + 1;
		return count;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Log.d("members", "P:" + position);

		int group = -1;
		for (int i = 0; i <= position; i++)
			if (isHeader.get(i).booleanValue())
				group++;
		final int groupPosition = group;
		Log.d("members", "GP:" + groupPosition);

		int child = 0;
		for (int i = 0; i <= position; i++) {
			child++;
			if (isHeader.get(i).booleanValue())
				child = 0;
		}
		final int childPosition = child;
		Log.d("members", "CP:" + childPosition);

		switch (getItemViewType(position)) {
		case 0:
			if (convertView == null || (convertView != null && convertView.findViewById(R.id.teamText) == null))
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_caption, null);

			TextView name = (TextView) convertView.findViewById(R.id.teamText);
			String header = headers.get(groupPosition);
			name.setText(header);

			return convertView;
		default:
			final Member member = content.get(groupPosition).get(childPosition - 1);

			if (convertView == null)
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_member, null);

			TextView memberName = (TextView) convertView.findViewById(R.id.memberName);
			TextView memberFunction = (TextView) convertView.findViewById(R.id.memberFunction);
			ImageButton memberCall = (ImageButton) convertView.findViewById(R.id.memberCallButton);
			ImageButton memberEmail = (ImageButton) convertView.findViewById(R.id.memberEmailButton);

			memberName.setText(member.getName());
			memberFunction.setText(member.getFunction());

			memberCall.setVisibility(member.getPhoneNumber().equals("") ? View.INVISIBLE : View.VISIBLE);
			memberEmail.setVisibility(member.getEmail().equals("") ? View.INVISIBLE : View.VISIBLE);

			memberCall.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setData(Uri.parse("tel:" + member.getPhoneNumber()));
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(callIntent);
				}
			});

			memberEmail.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
					String uriString = "mailto:" + member.getEmail() + "?subject=" + "Aan " + member.getFunction() + " "
							+ headers.get(groupPosition).toLowerCase(Locale.US) + " Juliana '32";
					uriString = uriString.replaceAll(" ", "%20");
					emailIntent.setData(Uri.parse(uriString));
					getContext().startActivity(Intent.createChooser(emailIntent, "Email versturen aan " + member.getEmail()));
				}
			});

			return convertView;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}
}

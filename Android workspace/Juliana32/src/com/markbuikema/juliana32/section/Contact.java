package com.markbuikema.juliana32.section;

import android.widget.ListView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.ContactAdapter;

@Deprecated
public class Contact {

	private MainActivity act;
	private ListView contactsView;
	private ContactAdapter adapter;

	public Contact(MainActivity act) {
		this.act = act;
		contactsView = (ListView) act.findViewById(R.id.contactView);
		adapter = new ContactAdapter(act);
		contactsView.setAdapter(adapter);
	}
}

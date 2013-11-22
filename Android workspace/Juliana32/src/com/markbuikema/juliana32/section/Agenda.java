package com.markbuikema.juliana32.section;

import org.holoeverywhere.widget.ViewPager;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;

public class Agenda {

	private MainActivity act;
	private ViewPager weekPager;

	public Agenda(MainActivity act) {
		this.act = act;
		weekPager = (ViewPager) act.findViewById(R.id.agendaView);
	}
}

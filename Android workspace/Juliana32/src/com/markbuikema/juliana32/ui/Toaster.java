package com.markbuikema.juliana32.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.util.Util;

public abstract class Toaster {
	public static void toast(Activity context, String message) {
		toast(context, message, -1, -1, Toast.LENGTH_LONG);
	}

	public static void toast(Activity context, String message, int x, int y, int duration) {
		LayoutInflater inflater = context.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toaster, (ViewGroup) context.findViewById(R.id.toast_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.toasterLabel);
		text.setText(message);
		text.setTypeface(Util.getRobotoCondensed(context));

		Toast toast = new Toast(context);
		if (x > -1 && y > -1)
			toast.setGravity(Gravity.LEFT | Gravity.TOP, x, y + 50);
		else
			toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 50);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}
}

package com.markbuikema.juliana32.asynctask;

import com.markbuikema.juliana32.util.Util;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class PictureChanger extends AsyncTask<String, Void, Bitmap> {
	@Override
	protected Bitmap doInBackground(String... arg0) {
		try {
			if (arg0[0] == null)
				return null;
			else
				return Util.getPictureFromUrl(arg0[0]);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}
}
package com.markbuikema.juliana32.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class PictureRetriever extends AsyncTask<String, Void, Bitmap> {
	@Override
	protected Bitmap doInBackground(String... params) {
		return Tools.getPictureFromUrl(params[0]);
	}
}
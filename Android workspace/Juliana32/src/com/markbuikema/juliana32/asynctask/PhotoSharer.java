package com.markbuikema.juliana32.asynctask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.util.Util;

public class PhotoSharer extends AsyncTask<String, Void, String> {

	private String teamName;
	private Context ctx;
	
	
	public PhotoSharer(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected String doInBackground(String... params) {
		teamName = params[1];
		return cacheImage(Util.getPhotoInputStreamFromUrl(params[0]), teamName);
	}

	@Override
	protected void onPostExecute(String result) {
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("image/*");
		share.putExtra(Intent.EXTRA_SUBJECT, "Foto " + teamName);
		share.putExtra(Intent.EXTRA_TEXT, teamName);
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + result));
		try {
			ctx.startActivity(Intent.createChooser(share, ctx.getResources().getString(R.string.sharePhoto)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String cacheImage(InputStream is, String teamName) {
		String path = "";
		try {
			File cacheDir = ctx.getExternalCacheDir();
			File downloadingMediaFile = new File(cacheDir, "Foto " + teamName + ".jpg");
			byte[] buf = new byte[256];
			FileOutputStream out = new FileOutputStream(downloadingMediaFile);
			while (true) {
				int rd = is.read(buf, 0, 256);
				if (rd == -1 || rd == 0) break;
				out.write(buf, 0, rd);
			}
			is.close();
			out.close();
			return downloadingMediaFile.getPath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return path;
	}
}
package com.markbuikema.juliana32.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;

public class Tools {

	private static final String PREF_SHOULD_SHOW_HINT = "shouldShowHint";
	private static Bitmap[] teletekst;

	public static void putTeletekst(Bitmap[] bmps) {
		teletekst = bmps;
	}

	public static Bitmap getTeletekst(int index) {
		if (index >= 0 && teletekst.length > index)
			return teletekst[index];
		else
			return null;
	}

	public static String getHttpContent(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getPictureFromUrl(String url) {

		// exit method if method is called from main thread (not allowed)
		if (Looper.myLooper() == Looper.getMainLooper()) return null;

		URL image;
		try {
			image = new URL(url.replaceAll("&amp;", "&"));

			URLConnection conn = image.openConnection();
			conn.connect();
			return BitmapFactory.decodeStream(new BufferedInputStream(conn.getInputStream()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean shouldShowTeletekstHint(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("teletekst", 0);
		try {
			return prefs.getBoolean(PREF_SHOULD_SHOW_HINT, true);
		} finally {
			Editor edit = prefs.edit();
			edit.putBoolean(PREF_SHOULD_SHOW_HINT, false);
			edit.commit();
		}
	}

}

package com.markbuikema.juliana32.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.GregorianCalendar;

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
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class Tools {

	private static final String PREF_SHOULD_SHOW_HINT = "shouldShowHint";
	private static Bitmap[] teletekst;
	private static Bitmap facebookLogo;
	private static Bitmap julianaLogo;

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
		if (Looper.myLooper() == Looper.getMainLooper())
			return null;

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

	public static InputStream getPhotoInputStreamFromUrl(String url) {

		// exit method if method is called from main thread (not allowed)
		if (Looper.myLooper() == Looper.getMainLooper())
			return null;

		URL image;
		try {
			image = new URL(url.replaceAll("&amp;", "&"));

			URLConnection conn = image.openConnection();
			conn.connect();
			return new BufferedInputStream(conn.getInputStream());
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

	public static String getDateString(GregorianCalendar date) {
		String string = date.get(GregorianCalendar.DAY_OF_MONTH) + "/" + (date.get(GregorianCalendar.MONTH) + 1) + "/"
				+ Integer.toString(date.get(GregorianCalendar.YEAR)).substring(2);
		return string;
	}

	public static Bitmap getFacebookLogo(Context ctx) {
		if (facebookLogo == null)
			facebookLogo = BitmapFactory.decodeResource(ctx.getResources(),
					com.markbuikema.juliana32.R.drawable.ic_action_facebook);
		return facebookLogo;
	}

	public static Bitmap getJulianaLogo(Context ctx) {
		if (julianaLogo == null)
			julianaLogo = BitmapFactory.decodeResource(ctx.getResources(), com.markbuikema.juliana32.R.drawable.ic_launcher);
		return julianaLogo;
	}

	private static void hideView(final View v) {
		if (v.getVisibility() == View.GONE || v.getVisibility() == View.INVISIBLE)
			return;

		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(v);
		animator.setDuration(350);
		animator.alpha(0);
		animator.setListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				v.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				v.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				v.setVisibility(View.GONE);
			}
		});
		animator.start();
	}

	private static void showView(View v) {
		if (v.getVisibility() == View.VISIBLE)
			return;
		v.setVisibility(View.VISIBLE);
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(v);
		animator.alpha(1);
		animator.setDuration(350);
		animator.start();
	}

	public static void setVisibility(View v, boolean visible) {
		if (visible)
			showView(v);
		else
			hideView(v);

	}
}
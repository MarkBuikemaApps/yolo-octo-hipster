package com.markbuikema.juliana32.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.Like;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.Season;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;

public class Util {

	public static final String PHOTO_URL_PREFIX = "http://graph.facebook.com/";
	public static final String PHOTO_URL_SUFFIX = "/picture";
	public static final long WEEK = 1000 * 60 * 60 * 24 * 7;
	private static float screenWidth = -1;

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

	public static String getDateString(Context ctx, GregorianCalendar date) {
		return DateTimeUtils.getInstance(ctx).getTimeDiffString(date.getTimeInMillis());
	}

	public static void onOrientationChanged(boolean portrait) {
		screenWidth = -1;
	}

	@SuppressWarnings("deprecation")
	public static float getScreenWidth(Context ctx) {
		if (screenWidth == -1) {
			Display d = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			screenWidth = d.getWidth();
		}
		return screenWidth;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static float pxToDp(float px) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, Resources.getSystem().getDisplayMetrics());

	}

	public static GregorianCalendar parseDate(String websiteDate) {
		int dateDay = Integer.parseInt(websiteDate.substring(0, 2));
		int dateYear = Integer.parseInt(websiteDate.substring(websiteDate.length() - 4, websiteDate.length()));
		int dateMonth = 0;
		Month month = Month.valueOf(websiteDate.split(" ")[1].toUpperCase());
		switch (month) {
		case FEBRUARI:
			dateMonth = 1;
			break;
		case MAART:
			dateMonth = 2;
			break;
		case APRIL:
			dateMonth = 3;
			break;
		case MEI:
			dateMonth = 4;
			break;
		case JUNI:
			dateMonth = 5;
			break;
		case JULI:
			dateMonth = 6;
			break;
		case AUGUSTUS:
			dateMonth = 7;
			break;
		case SEPTEMBER:
			dateMonth = 8;
			break;
		case OKTOBER:
			dateMonth = 9;
			break;
		case NOVEMBER:
			dateMonth = 10;
			break;
		case DECEMBER:
			dateMonth = 11;
			break;
		default:
			dateMonth = 0;
			break;
		}

		return new GregorianCalendar(dateYear, dateMonth, dateDay);
	}

	private static enum Month {
		JANUARI, FEBRUARI, MAART, APRIL, MEI, JUNI, JULI, AUGUSTUS, SEPTEMBER, OKTOBER, NOVEMBER, DECEMBER
	}

	public static void linkPhotosToTeam() {

		for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
			try {
				FacebookNieuwsItem fbni = (FacebookNieuwsItem) item;
				if (!fbni.isPhoto())
					continue;

				String title = fbni.getTitle();
				String afterJul = title.split("Juliana")[1];
				String checkString;
				if (afterJul.contains("32"))
					checkString = afterJul.substring(3, 8);
				else
					checkString = afterJul.substring(0, 6);

				String[] teams = new String[] {
						"A1", "A2", "B1", "B2", "C1", "C2", "C3", "C4", "D1", "D2", "E1", "E2", "F1", "F2", "Da", "1", "2", "3", "4",
						"5", "6", "7", "8", "9", "10"
				};
				for (String team : teams)
					if (checkString.contains(team)) {
						// Log.d("photoadder", checkString + " contains " + team);
						for (Season s : DataManager.getInstance().getTeams())
							for (Team t : s.getTeams())
								if (isTeam(team, t)) {
									// Log.d("photoadder", "FBNI " + fbni.getTitle() +
									// " first has " + fbni.getPhotoCount() + " photos");
									for (String photo : fbni.getPhotos())
										t.addPhoto(photo);
									break;
								}
						break;
					}

			} catch (Exception e) {
				continue;
			}

	}

	/**
	 * @param team
	 *          team code
	 * @param t
	 *          the team to compare with
	 * @return whether it is the team
	 */
	private static boolean isTeam(String team, Team t) {
		boolean b = (" " + t.getCode()).contains(team);

		// if (b)
		// Log.d("isteam", "team " + team + " is team " + t.getName());
		// else
		// Log.d("isteam", "team " + team + " is not team " + t.getName());

		return b;
	}

	public static String getTeamCode(Team team) {
		if (team.getCategory() == Category.DAMES) {

		}

		return null;

	}

	public static String getLikeString(FacebookNieuwsItem item, String username) {
		List<Like> likes = new ArrayList<Like>(item.getLikes());
		boolean liked = false;
		for (Like like : item.getLikes())
			if (like.getName().equals(username))
				liked = true;
		if (item.isLiked() && !liked)
			likes.add(0, new Like("", username));
		if (!item.isLiked() && liked)
			for (Like like : item.getLikes())
				if (like.getName().equals(username)) {
					likes.remove(like);
					break;
				}

		String s = "";
		if (likes.isEmpty())
			return s;

		if (likes.size() == 1)
			return likes.get(0).getName() + " vindt dit leuk.";

		for (int i = 0; i < likes.size(); i++) {
			s += likes.get(i).getName();

			if (i < likes.size() - 2)
				s += ", ";
			else
				if (i < likes.size() - 1)
					s += " en ";
		}

		s += " vinden dit leuk.";
		return s;
	}

	public static String loadJSONFromAsset(Context context, String fileName) {
		String json = null;
		try {

			InputStream is = context.getAssets().open(fileName);

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	public static NormalNieuwsItem findWedstrijdVerslag(Game game) {
		String club = "";
		String string = game.getOtherTeam();
		try {
			string = string.split("'")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		for (String word : string.split(" "))
			try {
				Integer.valueOf(word);
				break;
			} catch (NumberFormatException e) {
				if (word.startsWith("'"))
					break;
				club += word + " ";
			}
		club = club.trim();

		for (NieuwsItem item : DataManager.getInstance().getNieuwsItems())
			try {
				NormalNieuwsItem i = (NormalNieuwsItem) item;
				if (i.getCreatedAt().getTimeInMillis() < game.getDate()
						|| i.getCreatedAt().getTimeInMillis() > game.getDate() + WEEK)
					continue;

				if (i.getSubTitle().contains(club) || i.getTitle().contains(club))
					return i;

			} catch (ClassCastException e) {
				continue;
			}
		return null;
	}
}
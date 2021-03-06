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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.Game;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.Team;
import com.markbuikema.juliana32.model.Team.Category;
import com.markbuikema.juliana32.model.WebsiteNieuwsItem;
import com.markbuikema.juliana32.section.Teams;

public class Util {

	public static final String PHOTO_URL_PREFIX = "http://graph.facebook.com/";
	public static final String PHOTO_URL_SUFFIX = "/picture";
	public static final long WEEK = 1000 * 60 * 60 * 24 * 7;
	private static Typeface robotoLight;
	private static Typeface robotoThin;
	private static Typeface robotoCondensed;
	private static Typeface robotoSlabLight;

	public static String getHttpContent( String url ) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet( url );
		try {
			HttpResponse response = client.execute( get );
			return EntityUtils.toString( response.getEntity(), "UTF-8" );
		} catch ( ClientProtocolException e ) {
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getPhotoInputStreamFromUrl( String url ) {

		// exit method if method is called from main thread (not allowed)
		if ( Looper.myLooper() == Looper.getMainLooper() )
			return null;

		URL image;
		try {
			image = new URL( url.replaceAll( "&amp;", "&" ) );

			URLConnection conn = image.openConnection();
			conn.connect();
			return new BufferedInputStream( conn.getInputStream() );
		} catch ( MalformedURLException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDateString( Context ctx, GregorianCalendar date ) {
		return DateTimeUtils.getInstance( ctx ).getTimeDiffString( date.getTimeInMillis() );
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static float pxToDp( float px ) {
		return TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, px, Resources.getSystem().getDisplayMetrics() );
	}

	public static GregorianCalendar parseDate( String websiteDate ) {
		int dateDay = Integer.parseInt( websiteDate.substring( 0, 2 ) );
		int dateYear = Integer.parseInt( websiteDate.substring( websiteDate.length() - 4, websiteDate.length() ) );
		int dateMonth = 0;
		Month month = Month.valueOf( websiteDate.split( " " )[ 1 ].toUpperCase() );
		switch ( month ) {
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

		return new GregorianCalendar( dateYear, dateMonth, dateDay );
	}

	private static enum Month {
		JANUARI, FEBRUARI, MAART, APRIL, MEI, JUNI, JULI, AUGUSTUS, SEPTEMBER, OKTOBER, NOVEMBER, DECEMBER
	}

	public static void linkPhotosToTeam() {

		for ( NieuwsItem item : DataManager.getInstance().getNieuwsItems() )
			try {
				FacebookNieuwsItem fbni = (FacebookNieuwsItem) item;
				if ( !fbni.isPhoto() )
					continue;

				String title = fbni.getTitle();
				String afterJul = title.split( "Juliana" )[ 1 ];
				String checkString;
				if ( afterJul.contains( "32" ) )
					checkString = afterJul.substring( 3, 8 );
				else
					checkString = afterJul.substring( 0, 6 );

				String[] teamCodes = new String[] { "A1", "A2", "B1", "B2", "C1", "C2", "C3", "C4", "D1", "D2", "E1", "E2", "F1", "F2",
						"Da", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
				for ( String team : teamCodes )
					if ( checkString.contains( team ) ) {
						// Log.d("photoadder", checkString + " contains " +
						// team);
						for ( Team t : Teams.getTeams() )
							if ( isTeam( team, t ) ) {
								// Log.d("photoadder", "FBNI " + fbni.getTitle()
								// +
								// " first has " + fbni.getPhotoCount() +
								// " photos");
								for ( String photo : fbni.getPhotos() )
									t.addPhoto( photo );
								break;
							}
						break;
					}

			} catch ( Exception e ) {
				continue;
			}

	}

	/**
	 * @param team
	 *            team code
	 * @param t
	 *            the team to compare with
	 * @return whether it is the team
	 */
	private static boolean isTeam( String team, Team t ) {
		boolean b = ( " " + t.getCode() ).contains( team );

		// if (b)
		// Log.d("isteam", "team " + team + " is team " + t.getName());
		// else
		// Log.d("isteam", "team " + team + " is not team " + t.getName());

		return b;
	}

	public static String getTeamCode( Team team ) {
		if ( team.getCategory() == Category.DAMES ) {

		}

		return null;

	}

	public static String loadJSONFromAsset( Context context, String fileName ) {
		String json = null;
		try {

			InputStream is = context.getAssets().open( fileName );

			int size = is.available();

			byte[] buffer = new byte[ size ];

			is.read( buffer );

			is.close();

			json = new String( buffer, "UTF-8" );

		} catch ( IOException ex ) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	public static WebsiteNieuwsItem findWedstrijdVerslag( Game game ) {
		String club = "";
		String string = game.getOtherTeam();
		try {
			string = string.split( "'" )[ 0 ];
		} catch ( ArrayIndexOutOfBoundsException e ) {
		}
		for ( String word : string.split( " " ) )
			try {
				Integer.valueOf( word );
				break;
			} catch ( NumberFormatException e ) {
				if ( word.startsWith( "'" ) )
					break;
				club += word + " ";
			}
		club = club.trim();

		for ( NieuwsItem item : DataManager.getInstance().getNieuwsItems() )
			try {
				WebsiteNieuwsItem i = (WebsiteNieuwsItem) item;
				if ( i.getCreatedAt().getTimeInMillis() < game.getDate() || i.getCreatedAt().getTimeInMillis() > game.getDate() + WEEK )
					continue;

				if ( i.getSubTitle().contains( club ) || i.getTitle().contains( club ) )
					return i;

			} catch ( ClassCastException e ) {
				continue;
			}
		return null;
	}

	public static Typeface getRobotoLight( Context context ) {
		if ( robotoLight == null )
			robotoLight = Typeface.createFromAsset( context.getAssets(), "Roboto-Light.ttf" );
		return robotoLight;
	}

	public static Typeface getRobotoThin( Context context ) {
		if ( robotoThin == null )
			robotoThin = Typeface.createFromAsset( context.getAssets(), "Roboto-Thin.ttf" );
		return robotoThin;
	}

	public static Typeface getRobotoCondensed( Context context ) {
		if ( robotoCondensed == null )
			robotoCondensed = Typeface.createFromAsset( context.getAssets(), "RobotoCondensed-Light.ttf" );
		return robotoCondensed;
	}

	public static Typeface getRobotoSlabLight( Context context ) {
		if ( robotoSlabLight == null )
			robotoSlabLight = Typeface.createFromAsset( context.getAssets(), "RobotoSlab-Light.ttf" );
		return robotoSlabLight;
	}

	@TargetApi (Build.VERSION_CODES.JELLY_BEAN )
	public static void removeOnGlobalLayoutListener( View v, ViewTreeObserver.OnGlobalLayoutListener listener ) {
		if ( Build.VERSION.SDK_INT < 16 )
			v.getViewTreeObserver().removeGlobalOnLayoutListener( listener );
		else
			v.getViewTreeObserver().removeOnGlobalLayoutListener( listener );
	}

	// EXPAND ANIMATION
	public static void expand( final View v ) {

		v.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				final int targetHeight = v.getHeight();
				Log.d( "expand", "target:" + targetHeight );

				v.getLayoutParams().height = 0;
				v.setVisibility( View.VISIBLE );
				Animation a = new Animation() {
					@Override
					protected void applyTransformation( float interpolatedTime, Transformation t ) {

						v.getLayoutParams().height = interpolatedTime == 1 ? LayoutParams.WRAP_CONTENT
								: (int) ( targetHeight * interpolatedTime );
						v.getParent().requestLayout();
					}

					@Override
					public boolean willChangeBounds() {
						return true;
					}
				};
				a.setDuration( 500 );
				v.startAnimation( a );
				removeOnGlobalLayoutListener( v, this );
			}
		} );

	}

	public static void collapse( final View v ) {
		final int initialHeight = v.getMeasuredHeight();

		Animation a = new Animation() {
			@Override
			protected void applyTransformation( float interpolatedTime, Transformation t ) {
				if ( interpolatedTime == 1 )
					v.setVisibility( View.GONE );
				else {
					v.getLayoutParams().height = initialHeight - (int) ( initialHeight * interpolatedTime );
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		a.setDuration(
		// (int) (initialHeight /
		// v.getContext().getResources().getDisplayMetrics().density)
		500 );
		v.startAnimation( a );
	}

	public static class LinkifyExtra extends Linkify {
		public static Spanned addLinksHtmlAware( String htmlString ) {
			try {
				// gather links from html
				Spanned spann = Html.fromHtml( htmlString );
				URLSpan[] old = spann.getSpans( 0, spann.length(), URLSpan.class );
				List<Pair<Integer, Integer>> htmlLinks = new ArrayList<Pair<Integer, Integer>>();
				for ( URLSpan span : old )
					htmlLinks.add( new Pair<Integer, Integer>( spann.getSpanStart( span ), spann.getSpanEnd( span ) ) );
				// linkify spanned, html link will be lost
				Linkify.addLinks( (Spannable) spann, Linkify.ALL );
				// add html links back
				for ( int i = 0; i < old.length; i++ )
					( (Spannable) spann ).setSpan( old[ i ], htmlLinks.get( i ).first, htmlLinks.get( i ).second,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );

				return spann;
			} catch ( Exception e ) {
				return null;
			}
		}
	}

	public static CharSequence trimTrailingWhitespace( CharSequence source ) {

		if ( source == null )
			return "";

		int end = source.length();
		int start = -1;

		while ( --end >= 0 && Character.isWhitespace( source.charAt( end ) ) )
			;
		while ( ++start < end && Character.isWhitespace( source.charAt( start ) ) )
			;

		return source.subSequence( start, end + 1 );
	}
}

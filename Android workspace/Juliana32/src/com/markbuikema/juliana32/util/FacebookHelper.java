package com.markbuikema.juliana32.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Session;
import com.facebook.android.Facebook;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.Like;
import com.markbuikema.juliana32.model.NieuwsItem;

@SuppressWarnings ("deprecation" )
public class FacebookHelper {
	private static final String TAG = "FacebookHelperJuliana";

	public static final String JULIANA_ID = "294105307313875";
	public static final String APP_ID = "281741915275542";
	public static final String ACCESS_TOKEN = "281741915275542|uc5YqxhvQIBkMga9srZNMx_3UBU";
	private static final String FACEBOOK_TIMELINE_PHOTOS_ALBUM_ID = "339047466152992";

	private static final int FACEBOOK_POST_COUNT = 100;

	private static Facebook facebook;

	public static Facebook getFacebook() {
		if ( facebook == null )
			facebook = new Facebook( APP_ID );
		return facebook;
	}

	public static void addFacebookFeed( List<NieuwsItem> items ) {
		Bundle params = new Bundle();
		params.putString( "access_token", ACCESS_TOKEN );
		params.putString( "fields", "feed.limit(" + FACEBOOK_POST_COUNT + "),photos" );
		String json = null;
		try {
			json = getFacebook().request( JULIANA_ID, params );
		} catch ( MalformedURLException e ) {
			return;
		} catch ( IOException e ) {
			return;
		}
		try {

			JSONObject all = new JSONObject( json );

			JSONObject feed = all.getJSONObject( "feed" );
			try {
				JSONArray array = feed.getJSONArray( "data" );
				Log.d( "FB_POST_COUNT", "count: " + array.length() );
				for ( int i = 0; i < array.length(); i++ ) {
					FacebookNieuwsItem item = processJSONObject( array.getJSONObject( i ) );
					if ( item != null )
						items.add( item );
				}
			} catch ( JSONException e ) {
				FacebookNieuwsItem item = processJSONObject( feed.getJSONObject( "data" ) );
				if ( item != null )
					items.add( item );
			}
		} catch ( JSONException e ) {
		}

		int count = 0;
		for ( NieuwsItem i : items )
			if ( i instanceof FacebookNieuwsItem )
				count++;

		Log.d( "FB_POST_COUNT", "count: " + count );

	}

	private static FacebookNieuwsItem processJSONObject( JSONObject o ) {
		boolean photo = false;
		try {
			if ( o.getString( "type" ).equals( "photo" ) )
				// Log.d("PHOTOJSON", o.toString());
				photo = true;
		} catch ( JSONException e ) {
		}

		Log.d( TAG, "data:" + o.toString() );

		String id;
		String title;
		String content;
		String dateString;
		String link;
		String imgUrl;
		List<Like> likes = new ArrayList<Like>();
		List<Comment> comments = new ArrayList<Comment>();
		String albumId = null;
		String defaultPhoto;
		if ( photo )
			try {
				String parsableAlbum = o.getString( "link" );
				// Log.d("ALBUM", "1: " + parsableAlbum);
				parsableAlbum = parsableAlbum.split( "set=a." )[ 1 ];
				// Log.d("ALBUM", "2: " + parsableAlbum);
				albumId = parsableAlbum.split( "\\." )[ 0 ];
				// Log.d("ALBUM", "3: " + albumId);

			} catch ( JSONException e1 ) {
				e1.printStackTrace();
			} catch ( IndexOutOfBoundsException e ) {
				e.printStackTrace();
			}

		if ( albumId != null && albumId.equals( FACEBOOK_TIMELINE_PHOTOS_ALBUM_ID ) )
			albumId = null;

		try {
			if ( !o.getJSONObject( "from" ).getString( "name" ).toLowerCase( Locale.US ).contains( "juliana" ) )
				return null;
		} catch ( JSONException e ) {
			return null;
		}

		try {
			defaultPhoto = o.getString( "object_id" );
		} catch ( JSONException e ) {
			defaultPhoto = null;
		}

		try {
			id = o.getString( "id" );
		} catch ( JSONException e ) {
			id = null;
		}
		// Log.d( "FB_POST", "pre message parse" );
		try {
			content = o.getString( "message" );
		} catch ( JSONException e ) {
			content = null;
		}
		// Log.d( "FB_POST", "post message parse" );

		try {
			if ( photo )
				title = o.getString( "name" );
			else
				title = "Facebook";
		} catch ( JSONException e ) {
			title = "Facebook";
		}

		try {
			dateString = o.getString( "created_time" );
		} catch ( JSONException e ) {
			dateString = null;
		}

		try {
			link = o.getString( "link" );
		} catch ( JSONException e ) {
			link = null;
		}

		try {
			imgUrl = o.getString( "picture" );
		} catch ( JSONException e ) {
			imgUrl = null;
		}

		try {
			JSONObject likeJSON = o.getJSONObject( "likes" );
			JSONArray data = likeJSON.getJSONArray( "data" );
			for ( int i = 0; i < data.length(); i++ ) {
				JSONObject like = data.getJSONObject( i );
				String name = like.getString( "name" );
				String likeId = like.getString( "id" );
				likes.add( new Like( likeId, name ) );
			}
		} catch ( JSONException e ) {
		}

		try {
			JSONObject commentJSON = o.getJSONObject( "comments" );
			JSONArray commentArray = commentJSON.getJSONArray( "data" );
			for ( int i = 0; i < commentArray.length(); i++ ) {
				JSONObject c = commentArray.getJSONObject( i );
				Comment comment = new Comment( c.getString( "id" ), c.getJSONObject( "from" ).getString( "name" ), c.getJSONObject( "from" )
						.getString( "id" ), c.getString( "message" ), c.getString( "created_time" ) );
				comments.add( comment );
			}
		} catch ( JSONException e ) {
		}

		GregorianCalendar date = toDate( dateString );

		if (!photo && content == null) return null;
		
		FacebookNieuwsItem item = new FacebookNieuwsItem( id, title, content, date, link, imgUrl, likes, comments, photo ? albumId : null,
				photo ? defaultPhoto : null );

		Log.d( "FBNI", item.toString() );

		return item;

	}

	public static class CommentLoader extends AsyncTask<String, Comment, List<Comment>> {

		@Override
		protected List<Comment> doInBackground( String... id ) {
			List<Comment> output = new ArrayList<Comment>();
			
			// Log.d("COMMENT", "Started commentloader");
			Bundle params = new Bundle();
			params.putString( "access_token", FacebookHelper.ACCESS_TOKEN );
			params.putInt( "limit", 150 );
			try {
				JSONObject comments = new JSONObject( FacebookHelper.getFacebook().request( "/" + id[ 0 ] + "/comments", params ) );
				JSONArray data = comments.getJSONArray( "data" );
				for ( int i = 0; i < data.length(); i++ ) {
					// Log.d("COMMENT", data.getJSONObject(i).toString() +
					// "...");
					Comment comment = processCommentJSON( data.getJSONObject( i ) );
					if ( comment != null ) {
						publishProgress( comment );
						output.add(comment);
					}
				}
			} catch ( MalformedURLException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			} catch ( JSONException e ) {
				e.printStackTrace();
			} catch ( IndexOutOfBoundsException e ) {
				e.printStackTrace();
			}

			return output;
		}

		private Comment processCommentJSON( JSONObject data ) {
			String id;
			try {
				id = data.getString( "id" );
			} catch ( JSONException e ) {
				id = "";
			}

			String name;
			try {
				name = data.getJSONObject( "from" ).getString( "name" );
			} catch ( JSONException e ) {
				name = "Fout bij het laden van de naam";
			}

			String userId = null;
			try {
				userId = data.getJSONObject( "from" ).getString( "id" );
			} catch ( JSONException e1 ) {
				e1.printStackTrace();
			}

			String text;
			try {
				text = data.getString( "message" );
			} catch ( JSONException e ) {
				text = "Fout bij het laden van de reactie";
			}

			String dateString;
			try {
				dateString = data.getString( "created_time" ); // 2013-08-23T14:55:26+0000
			} catch ( JSONException e ) {
				dateString = "2019-12-31T23:59:59+0000";
			}

			Comment comment = new Comment( id, name, userId, text, dateString );
			return comment;
		}
	}

	public static class PhotoGetter extends AsyncTask<FacebookNieuwsItem, Void, List<String>> {

		@Override
		protected List<String> doInBackground( FacebookNieuwsItem... params ) {
			try {
				return params == null ? null : getFacebookPhotos( params[ 0 ].getAlbumId(), params[ 0 ].getDefaultPhoto() );
			} catch ( IndexOutOfBoundsException e ) {
				return null;
			}
		}
	}

	private static List<String> getFacebookPhotos( String albumId, String defaultPhoto ) {
		List<String> photos = new ArrayList<String>();

		if ( albumId == null ) {
			photos.add( defaultPhoto );
			return photos;
		}
		try {
			Bundle parameters = new Bundle();
			parameters.putString( "access_token", ACCESS_TOKEN );
			parameters.putString( "fields", "photos.limit(150).fields(id)" );
			JSONObject json = new JSONObject( getFacebook().request( albumId, parameters ) );

			Log.d( "album " + albumId, json.toString() );

			JSONArray data = json.getJSONObject( "photos" ).getJSONArray( "data" );
			for ( int i = 0; i < data.length(); i++ )
				photos.add( data.getJSONObject( i ).getString( "id" ) );

		} catch ( MalformedURLException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}

		return photos;
	}

	public static GregorianCalendar toDate( String dateString ) {
		String yearString = dateString.substring( 0, 4 );
		String monthString = dateString.substring( 5, 7 );
		String dayString = dateString.substring( 8, 10 );
		String hourString = dateString.substring( 11, 13 );
		String minuteString = dateString.substring( 14, 16 );
		String secondString = dateString.substring( 17, 19 );
		String timezoneString = dateString.substring( 19, 22 );
		GregorianCalendar createdAt = new GregorianCalendar();
		createdAt.setTimeZone( TimeZone.getTimeZone( "GMT" + timezoneString + ":00" ) );
		createdAt.set( Integer.parseInt( yearString ), Integer.parseInt( monthString ) - 1, Integer.parseInt( dayString ),
				Integer.parseInt( hourString ), Integer.parseInt( minuteString ), Integer.parseInt( secondString ) );
		return createdAt;
	}

	/////AUTHENTICATION/////////////////////////////////////////////////////////////////////////
	
}

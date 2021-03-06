package com.markbuikema.juliana32.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.markbuikema.juliana32.util.Util;

public class Team {

	public enum Category {

		SENIOREN( "Senioren" ), JUNIOREN( "Junioren" ), DAMES( "Dames" );

		private String name;

		private Category( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static final String TAG = "Team model";
	private static int nextId = 0;

	private int id;
	private String name;
	private String code;
	private Category category;
	private String teamPhotoUrl;
	private ArrayList<Table> tables;
	private ArrayList<Game> games;
	private ArrayList<String> photos;

	private static Comparator<Table> tableComparator = new Comparator<Table>() {

		@Override
		public int compare( Table lhs, Table rhs ) {
			if ( lhs.getId() < rhs.getId() )
				return - 1;
			else
				if ( lhs.getId() > rhs.getId() )
					return 1;
				else
					return 0;
		}

	};

	/**
	 * @param name
	 *          should be "Juliana " + the code (e.g. B2)
	 * @param category
	 *          Dames, Senioren or Junioren
	 * @param teamPhotoUrl
	 *          The url that leads to the team photo, may be null
	 */
	public Team( String name, Category category, String teamPhotoUrl ) {
		id = nextId++ ;
		this.name = name;
		this.category = category;
		this.teamPhotoUrl = teamPhotoUrl;

		try {
			code = name.split( " " )[ 1 ];
		} catch ( ArrayIndexOutOfBoundsException e ) {
			code = "error";
		}

		games = new ArrayList<Game>();
		tables = new ArrayList<Table>();
		photos = new ArrayList<String>();
	}

	// public void addTableRow(TableRow row) {
	// if (row == null) return;
	// tables.add(row);
	// }

	public boolean isCategory( Category cat ) {
		return category == cat;
	}

	public void addGame( Game game ) {
		if ( game == null )
			return;
		games.add( game );
	}

	public ArrayList<Game> getUitslagen() {
		ArrayList<Game> uitslagen = new ArrayList<Game>();
		for ( Game game : games )
			if ( game.isPlayed() )
				uitslagen.add( game );
		return uitslagen;
	}

	public ArrayList<Game> getProgramma() {
		ArrayList<Game> programma = new ArrayList<Game>();
		for ( Game game : games )
			if ( ! game.isPlayed() )
				programma.add( game );
		return programma;
	}

	public int getGameCount() {
		return games.size();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	public String getCode() {
		return code;
	}

	public List<Game> getGames() {
		return Collections.unmodifiableList( games );
	}

	public List<Table> getTables() {
		return Collections.unmodifiableList( tables );
	}

	public String getTeamPhotoUrl() {
		return teamPhotoUrl;
	}

	public void addTable( Table table ) {
		tables.add( table );
		Collections.sort( tables, tableComparator );

		// Log.d(TAG, "table added");
	}

	public void addPhoto( String photo ) {
		if ( photo == null )
			return;

		// Log.d(TAG, "String added to team " + name + ", url: " + photo);
		photos.add( photo );
	}

	@SuppressWarnings( "unchecked" )
	public ArrayList<String> getStrings() {
		return (ArrayList<String>) photos.clone();
	}

	public int getStringCount() {
		return photos.size();
	}

	public String getTableName( int position ) {
		return tables.get( position ).getName();
	}

	public String[] getPhotoUrls() {
		String[] urls = new String[ photos.size() ];
		for ( int i = 0; i < photos.size(); i++ )
			if ( photos.get( i ).startsWith( "http" ) )
				urls[ i ] = photos.get( i );
			else
				urls[ i ] = Util.PHOTO_URL_PREFIX + photos.get( i ) + Util.PHOTO_URL_SUFFIX;
		return urls;
	}

	public int getPhotoCount() {
		return photos.size();
	}

	public List<String> getPhotos() {
		return Collections.unmodifiableList( photos );
	}

}

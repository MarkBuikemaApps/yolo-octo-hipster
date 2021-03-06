package com.markbuikema.juliana32.adapter;

import static com.markbuikema.juliana32.R.id.rowBackground;
import static com.markbuikema.juliana32.R.id.rowConceded;
import static com.markbuikema.juliana32.R.id.rowDivider;
import static com.markbuikema.juliana32.R.id.rowDrawn;
import static com.markbuikema.juliana32.R.id.rowLost;
import static com.markbuikema.juliana32.R.id.rowMinusPoints;
import static com.markbuikema.juliana32.R.id.rowPlayed;
import static com.markbuikema.juliana32.R.id.rowPoints;
import static com.markbuikema.juliana32.R.id.rowPosition;
import static com.markbuikema.juliana32.R.id.rowScored;
import static com.markbuikema.juliana32.R.id.rowTeam;
import static com.markbuikema.juliana32.R.id.rowWon;
import static com.markbuikema.juliana32.R.layout.listitem_tablerow;
import static com.markbuikema.juliana32.R.string.conceded;
import static com.markbuikema.juliana32.R.string.drawn;
import static com.markbuikema.juliana32.R.string.lost;
import static com.markbuikema.juliana32.R.string.minusPoints;
import static com.markbuikema.juliana32.R.string.number;
import static com.markbuikema.juliana32.R.string.played;
import static com.markbuikema.juliana32.R.string.points;
import static com.markbuikema.juliana32.R.string.scored;
import static com.markbuikema.juliana32.R.string.team;
import static com.markbuikema.juliana32.R.string.won;

import java.util.ArrayList;
import java.util.Locale;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.R.color;
import com.markbuikema.juliana32.model.TableRow;

public class TableAdapter extends ArrayAdapter<TableRow> {

	private static final String TAG = "TableAdapter";

	@SuppressWarnings("unchecked")
	public TableAdapter(Context context, ArrayList<TableRow> objects) {
		super(context, 0, (ArrayList<TableRow>) objects.clone());
		TableRow dummyRow = new TableRow();
		insert(dummyRow, 0);
		// d(TAG, "Adapter instantiated with " + getCount() + " views");

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// instantiate convertview to make sure it's not null, inflate layout if
		// necessary
		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(listitem_tablerow, null);

		// get the row to get the data from
		TableRow row = getItem(position);

		// shortcut to resources
		Resources r = getContext().getResources();

		// identify the row identification color and divider views
		View background = convertView.findViewById(rowBackground);
		View divider = convertView.findViewById(rowDivider);

		// set divider color, and row identification color
		if (position % 2 == 0)
			// even numbers
			background.setBackgroundColor(r.getColor(color.white));
		else
			// odd numbers
			background.setBackgroundColor(r.getColor(color.ltgrey));

		if (position > 0 && row.getTeamName().toLowerCase(Locale.US).contains("juliana"))
			background.setBackgroundColor(Color.rgb(240, 240, 240));

		divider.setBackgroundResource(R.drawable.menudivider);

		// identify the views
		TextView teamPosition = (TextView) convertView.findViewById(rowPosition);
		TextView teamName = (TextView) convertView.findViewById(rowTeam);
		TextView teamPlayed = (TextView) convertView.findViewById(rowPlayed);
		TextView teamPoints = (TextView) convertView.findViewById(rowPoints);

		if (position == 0) {
			// header row
			background.setBackgroundColor(r.getColor(color.grey));
			teamPosition.setText(r.getString(number));
			teamName.setText(r.getString(team));
			teamPlayed.setText(r.getString(played));
			teamPoints.setText(r.getString(points));
		} else {
			// content rows
			teamPosition.setText(Integer.toString(position));
			teamName.setText(row.getTeamName());
			teamPlayed.setText(Integer.toString(row.getPlayed()));
			teamPoints.setText(Integer.toString(row.getPoints()));
		}

		// identify the additional views for landscape mode
		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			TextView teamWon = (TextView) convertView.findViewById(rowWon);
			TextView teamDrawn = (TextView) convertView.findViewById(rowDrawn);
			TextView teamLost = (TextView) convertView.findViewById(rowLost);
			TextView teamScored = (TextView) convertView.findViewById(rowScored);
			TextView teamConceded = (TextView) convertView.findViewById(rowConceded);
			TextView teamMinusPoints = (TextView) convertView.findViewById(rowMinusPoints);

			if (position == 0) {
				// header row
				teamWon.setText(r.getString(won));
				teamDrawn.setText(r.getString(drawn));
				teamLost.setText(r.getString(lost));
				teamScored.setText(r.getString(scored));
				teamConceded.setText(r.getString(conceded));
				teamMinusPoints.setText(r.getString(minusPoints));
			} else {
				// content rows
				teamWon.setText(Integer.toString(row.getWon()));
				teamDrawn.setText(Integer.toString(row.getDrawn()));
				teamLost.setText(Integer.toString(row.getLost()));
				teamScored.setText(Integer.toString(row.getScored()));
				teamConceded.setText(Integer.toString(row.getConceded()));
				teamMinusPoints.setText(Integer.toString(row.getMinusPoints()));
			}
		}

		return convertView;
	}
}

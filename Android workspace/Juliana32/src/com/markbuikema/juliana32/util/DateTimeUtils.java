package com.markbuikema.juliana32.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.format.DateUtils;

import com.markbuikema.juliana32.R;

public class DateTimeUtils extends DateUtils {

	private static String mTimestampLabelYesterday;
	private static String mTimestampLabelToday;
	private static String mTimestampLabelJustNow;
	private static String mTimestampLabelMinutesAgo;
	private static String mTimestampLabelHoursAgo;

	private static Context context;
	private static DateTimeUtils instance;

	/**
	 * Singleton contructor, needed to get access to the application context &
	 * strings for i18n
	 * 
	 * @param context
	 *          Context
	 * @return DateTimeUtils singleton instanec
	 * @throws Exception
	 */
	public static DateTimeUtils getInstance(Context ctx) {
		context = ctx;
		if (instance == null) {
			instance = new DateTimeUtils();
			mTimestampLabelYesterday = context.getResources().getString(R.string.timestamp_yesterday);
			mTimestampLabelToday = context.getResources().getString(R.string.timestamp_today);
			mTimestampLabelJustNow = context.getResources().getString(R.string.timestamp_just_now);
			mTimestampLabelMinutesAgo = context.getResources().getString(R.string.timestamp_minutes_ago);
			mTimestampLabelHoursAgo = context.getResources().getString(R.string.timestamp_hours_ago);
		}
		return instance;
	}

	/**
	 * Checks if the given date is yesterday.
	 * 
	 * @param date
	 *          - Date to check.
	 * @return TRUE if the date is yesterday, FALSE otherwise.
	 */
	public static boolean isYesterday(long date) {

		final Calendar currentDate = Calendar.getInstance();
		currentDate.setTimeInMillis(date);

		final Calendar yesterdayDate = Calendar.getInstance();
		yesterdayDate.add(Calendar.DATE, -1);

		return yesterdayDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
				&& yesterdayDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR);
	}

	public static String[] weekdays = new String[] {
			"", "Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"
	};
	public static final long millisInADay = 1000 * 60 * 60 * 24;

	/**
	 * Displays a user-friendly date difference string
	 * 
	 * @param timedate
	 *          Timestamp to format as date difference from now
	 * @return Friendly-formatted date diff string
	 */
	public String getTimeDiffString(long timedate) {
		Calendar startDateTime = Calendar.getInstance();
		Calendar endDateTime = Calendar.getInstance();
		endDateTime.setTimeInMillis(timedate);
		long milliseconds1 = startDateTime.getTimeInMillis();
		long milliseconds2 = endDateTime.getTimeInMillis();
		long diff = milliseconds1 - milliseconds2;

		long hours = diff / (60 * 60 * 1000);
		long minutes = diff / (60 * 1000);
		minutes = minutes - 60 * hours;

		boolean isToday = DateTimeUtils.isToday(timedate);
		boolean isYesterday = DateTimeUtils.isYesterday(timedate);

		if (hours > 0 && hours < 12)
			return String.format(mTimestampLabelHoursAgo, hours);
		else
			if (hours <= 0) {
				if (minutes > 0)
					return String.format(mTimestampLabelMinutesAgo, minutes);
				else
					return mTimestampLabelJustNow;
			} else
				if (isToday)
					return mTimestampLabelToday;
				else
					if (isYesterday)
						return mTimestampLabelYesterday;
					else
						if (startDateTime.getTimeInMillis() - timedate < millisInADay * 6)
							return weekdays[endDateTime.get(Calendar.DAY_OF_WEEK)];
						else
							return getDateString(timedate);
	}

	private static String getDateString(long timedate) {
		GregorianCalendar date = new GregorianCalendar();
		date.setTimeInMillis(timedate);
		String output = date.get(GregorianCalendar.DAY_OF_MONTH) + " " + getMonthInDutch(date.get(GregorianCalendar.MONTH));
		if (new GregorianCalendar().get(GregorianCalendar.YEAR) != date.get(GregorianCalendar.YEAR))
			output += " " + date.get(GregorianCalendar.YEAR);

		return output;

	}

	private static String getMonthInDutch(int month) {
		switch (month) {
		case 1:
			return "februari";
		case 2:
			return "maart";
		case 3:
			return "april";
		case 4:
			return "mei";
		case 5:
			return "juni";
		case 6:
			return "juli";
		case 7:
			return "augustus";
		case 8:
			return "september";
		case 9:
			return "oktober";
		case 10:
			return "november";
		case 11:
			return "december";
		default:
			return "januari";

		}

	}
}
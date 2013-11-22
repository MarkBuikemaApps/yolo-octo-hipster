package com.markbuikema.juliana32.adapter;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.util.Util;

public class CommentAdapter extends ArrayAdapter<Comment> {

	public CommentAdapter(Context context) {
		super(context, 0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_comment, null);

		TextView name = (TextView) convertView.findViewById(R.id.commentName);
		TextView date = (TextView) convertView.findViewById(R.id.commentDate);
		TextView text = (TextView) convertView.findViewById(R.id.commentMessage);
		final ImageView pic = (ImageView) convertView.findViewById(R.id.commentPicture);

		final Comment comment = getItem(position);

		name.setText(comment.getName());

		String dateString = Util.getDateString(getContext(), comment.getCreatedAt());
		// Log.d("comment_date_adapter", dateString);
		date.setText(dateString);
		text.setText(comment.getText());
		UrlImageViewHelper.setUrlDrawable(pic, comment.getImgUrl());

		pic.setContentDescription(comment.getName());

		return convertView;
	}

}

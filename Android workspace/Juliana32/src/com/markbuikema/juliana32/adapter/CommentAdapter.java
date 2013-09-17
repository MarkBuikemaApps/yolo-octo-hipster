package com.markbuikema.juliana32.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.util.DateTimeUtils;

public class CommentAdapter extends ArrayAdapter<Comment> {

	public CommentAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_comment, null);

		TextView name = (TextView) convertView.findViewById(R.id.commentName);
		TextView date = (TextView) convertView.findViewById(R.id.commentDate);
		TextView text = (TextView) convertView.findViewById(R.id.commentMessage);
		ImageView pic = (ImageView) convertView.findViewById(R.id.commentPicture);

		Comment comment = getItem(position);

		name.setText(comment.getName());
		date.setText(DateTimeUtils.getInstance(getContext()).getTimeDiffString(comment.getCreatedAt().getTimeInMillis()));
		text.setText(comment.getText());

		Bitmap picture = comment.getImage();
		if (picture != null)
			pic.setBackground(new BitmapDrawable(getContext().getResources(), picture));

		return convertView;
	}

}

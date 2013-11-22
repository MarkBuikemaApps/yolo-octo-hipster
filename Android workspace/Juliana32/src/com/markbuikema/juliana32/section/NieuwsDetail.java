package com.markbuikema.juliana32.section;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.Like;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.ui.Button;
import com.markbuikema.juliana32.ui.PhotoPagerDialog.OnPhotoPagerDialogPageChangedListener;
import com.markbuikema.juliana32.util.FacebookHelper.CommentLoader;
import com.markbuikema.juliana32.util.Util;

public class NieuwsDetail {

	private static final String TAG = "NieuwsDetail";

	private MainActivity act;
	private NieuwsItem item;

	private TextView title;
	private TextView subTitle;
	private TextView date;
	private ImageView logo;
	private ProgressBar loader;
	private TextView commentsText;
	private LinearLayout commentContainer;
	private TextView content;
	private TextView likeText;
	private ImageView commentProfilePic;
	private ImageView commentProfilePicOverlay;
	private ImageButton likeButton;
	private EditText commentInput;
	private Button facebookLoginButton;
	private ProgressBar likeLoader;
	private FrameLayout likeButtonContainer;

	private int photoIndex;

	protected CommentLoader commentLoader;

	public NieuwsDetail(final MainActivity act, final NieuwsItem item) {
		this.act = act;
		this.item = item;
		photoIndex = 0;

		View mainView = act.findViewById(R.id.nieuwsDetailView);

		Toast.makeText(act, "photo: " + item.isPhoto(), Toast.LENGTH_LONG).show();

		if (item.isPhoto()) {
			title = (TextView) mainView.findViewById(R.id.nieuwsDetailTitle);
			date = (TextView) mainView.findViewById(R.id.nieuwsDetailDate);
			logo = (ImageView) mainView.findViewById(R.id.nieuwsDetailIcon);
			mainView.findViewById(R.id.nieuwsDetailPhotoFrame).setVisibility(View.VISIBLE);

			mainView.findViewById(R.id.nieuwsDetailAlternativeTitle).setVisibility(View.GONE);
			mainView.findViewById(R.id.nieuwsDetailAlternativeDate).setVisibility(View.GONE);
			mainView.findViewById(R.id.nieuwsDetailAlternativeIcon).setVisibility(View.GONE);

			int[] colors = new int[] {
					act.getResources().getColor(R.color.transparentblack), Color.TRANSPARENT
			};
			Drawable titleProtection = new GradientDrawable(Orientation.BOTTOM_TOP, colors);
			Drawable dateProtection = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
			mainView.findViewById(R.id.nieuwsDetailTitleBackgroundProtection).setBackgroundDrawable(titleProtection);
			mainView.findViewById(R.id.nieuwsDetailDateBackgroundProtection).setBackgroundDrawable(dateProtection);
			ImageView picture = (ImageView) mainView.findViewById(R.id.nieuwsDetailPhoto);
			UrlImageViewHelper.setUrlDrawable(picture, item.isFromFacebook() ? Util.PHOTO_URL_PREFIX + item.getPhoto(0)
					+ Util.PHOTO_URL_SUFFIX : item.getPhoto(0));
		} else {
			title = (TextView) mainView.findViewById(R.id.nieuwsDetailAlternativeTitle);
			date = (TextView) mainView.findViewById(R.id.nieuwsDetailAlternativeDate);
			logo = (ImageView) mainView.findViewById(R.id.nieuwsDetailAlternativeIcon);

			title.setVisibility(View.VISIBLE);
			date.setVisibility(View.VISIBLE);
			logo.setVisibility(View.VISIBLE);

			mainView.findViewById(R.id.nieuwsDetailPhotoFrame).setVisibility(View.GONE);
		}
		title.setTextColor(item.isPhoto() ? Color.WHITE : Color.BLACK);
		date.setTextColor(item.isPhoto() ? Color.WHITE : Color.BLACK);

		commentsText = (TextView) mainView.findViewById(R.id.nieuwsDetailCommentsText);
		subTitle = (TextView) mainView.findViewById(R.id.nieuwsDetailSubtitle);
		content = (TextView) mainView.findViewById(R.id.nieuwsDetailContent);
		commentContainer = (LinearLayout) mainView.findViewById(R.id.commentContainer);
		commentInput = (EditText) mainView.findViewById(R.id.commentInput);
		commentProfilePic = (ImageView) mainView.findViewById(R.id.commentProfilePic);
		commentProfilePicOverlay = (ImageView) mainView.findViewById(R.id.commentProfilePicOverlay);
		facebookLoginButton = (Button) act.findViewById(R.id.commentLoginButton);
		loader = (ProgressBar) act.findViewById(R.id.loading);
		likeButton = (ImageButton) mainView.findViewById(R.id.likeButton);
		likeText = (TextView) mainView.findViewById(R.id.likeText);
		likeLoader = (ProgressBar) mainView.findViewById(R.id.likeLoading);
		likeButtonContainer = (FrameLayout) mainView.findViewById(R.id.likeButtonContainer);

		if (item.isFromFacebook()) {
			FacebookNieuwsItem fbni = (FacebookNieuwsItem) item;
			if (!fbni.isLiked()) {
				boolean liked = false;
				for (Like like : fbni.getLikes())
					if (like.getName().equals(act.getUserName())) {
						liked = true;
						break;
					}
				fbni.setLiked(liked);
			}
			likeButton.setImageResource(fbni.isLiked() ? R.drawable.fb_liked : R.drawable.fb_like);
			likeText.setText(Util.getLikeString(fbni, act.getUserName()));
			if (fbni.getCommentCount() > 0)
				populateComments();
			else {
				commentsText.setVisibility(View.GONE);
				commentContainer.removeAllViews();
			}

		}

		title.setText(item.getTitle());

		commentInput.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE : View.GONE);
		commentProfilePic.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		commentProfilePicOverlay.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		likeButtonContainer.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		likeButton.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE : View.GONE);

		likeText.setVisibility(item.isFromFacebook() ? View.VISIBLE : View.GONE);
		facebookLoginButton.setVisibility(!item.isFromFacebook() || Session.getActiveSession().isOpened() ? View.GONE
				: View.VISIBLE);

		likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					FacebookNieuwsItem fbni = (FacebookNieuwsItem) item;
					onLikedChanged(!fbni.isLiked());
				} catch (ClassCastException e) {
					return;
				}
			}
		});

		commentInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
		commentInput.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !Session.getActiveSession().isOpened())
					act.onClickLogin();
			}
		});

		facebookLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				act.onClickLogin();
			}
		});

		commentInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(android.widget.TextView arg0, int arg1, KeyEvent arg2) {
				int charCount = commentInput.getText().length();
				if (charCount < 2 || charCount > 4000) {
					Toast.makeText(act, "Deze reactie is te kort.", Toast.LENGTH_LONG).show();

					return false;
				} else {

					postComment(commentInput.getText().toString());
					return true;
				}
			}
		});

		if (item.getSubTitle() == null)
			subTitle.setVisibility(View.GONE);
		else
			subTitle.setText(Html.fromHtml("<i>" + item.getSubTitle() + "</i>"));

		String contentString = item.getContent();
		if (contentString == null)
			contentString = "error";
		contentString = contentString.replaceAll(Character.toString((char) 65532), "");

		content.setText(LinkifyExtra.addLinksHtmlAware(contentString));
		content.setMovementMethod(LinkMovementMethod.getInstance());
		content.setLinksClickable(true);

		date.setText(Util.getDateString(act, item.getCreatedAt()));

		if (item.isFromFacebook())
			logo.setImageResource(R.drawable.ic_fb);
		else
			logo.setImageResource(R.drawable.ic_juliana);

		Toast
				.makeText(act, "Alternative views used: " + (title.getId() == R.id.nieuwsDetailAlternativeTitle), Toast.LENGTH_LONG)
				.show();
	}

	public void populateComments() {
		String id = ((FacebookNieuwsItem) item).getFbId();
		commentLoader = new CommentLoader() {

			@Override
			protected void onPreExecute() {
				commentContainer.removeAllViews();
				commentsText.setVisibility(View.GONE);
				loader.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onProgressUpdate(Comment... values) {
				for (Comment comment : values)
					commentContainer.addView(getCommentView(comment));
			}

			@Override
			protected void onPostExecute(Void result) {
				loader.setVisibility(View.GONE);
				act.fixActionBar();
				if (commentContainer.getChildCount() > 0)
					commentsText.setVisibility(View.VISIBLE);

			}
		};
		commentLoader.execute(id);
	}

	private View getCommentView(final Comment comment) {
		View view = LayoutInflater.from(act).inflate(R.layout.listitem_comment, null);

		TextView name = (TextView) view.findViewById(R.id.commentName);
		TextView date = (TextView) view.findViewById(R.id.commentDate);
		TextView text = (TextView) view.findViewById(R.id.commentMessage);
		final ImageView pic = (ImageView) view.findViewById(R.id.commentPicture);

		name.setText(comment.getName());

		String dateString = Util.getDateString(act, comment.getCreatedAt());
		// Log.d("comment_date_adapter", dateString);
		date.setText(dateString);
		text.setText(comment.getText());
		UrlImageViewHelper.setUrlDrawable(pic, comment.getImgUrl());

		pic.setContentDescription(comment.getName());
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + comment.getUserId())));
			}
		});
		return view;
	}

	public void setProfilePic(String url) {
		UrlImageViewHelper.setUrlDrawable(commentProfilePic, url, R.drawable.silhouette);
	}

	protected void postComment(final String message) {

		if (!Session.getActiveSession().isOpened())
			Toast.makeText(act, "Facebook session is not opened!", Toast.LENGTH_LONG).show();

		if (!Session.getActiveSession().getPermissions().contains("publish_actions")
				|| !Session.getActiveSession().getPermissions().contains("publish_stream")) {
			Session.getActiveSession().requestNewPublishPermissions(
					new NewPermissionsRequest(act, "publish_actions", "publish_stream"));
			Session.getActiveSession().addCallback(new StatusCallback() {

				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.getPermissions().contains("publish_actions") && session.getPermissions().contains("publish_stream")) {
						postComment(message);
						Session.getActiveSession().removeCallback(this);
					}
				}
			});
			return;
		}

		Session session = Session.getActiveSession();
		String id = ((FacebookNieuwsItem) item).getFbId();
		String graphPath = id + "/comments";
		JSONObject object = new JSONObject();
		try {
			object.put("message", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		GraphObject graphObject = GraphObject.Factory.create(object);

		Request request = Request.newPostRequest(session, graphPath, graphObject, new Callback() {

			@Override
			public void onCompleted(Response response) {
				if (response.getError() == null) {
					Toast.makeText(act, "Reactie is geplaatst!", Toast.LENGTH_LONG).show();
					commentInput.setText("");
					((FacebookNieuwsItem) item).comment();
					act.fixActionBar();
					populateComments();
				} else
					Toast.makeText(act, "Er is iets misgegaan. Probeer opnieuw in te loggen.", Toast.LENGTH_LONG).show();
			}
		});

		request.executeAsync();
	}

	public String getTitle() {
		return item.getTitle();
	}

	public void onResume() {
		commentInput.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE : View.GONE);
		commentProfilePic.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		commentProfilePicOverlay.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		likeButtonContainer.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE
				: View.GONE);
		likeButton.setVisibility(item.isFromFacebook() && Session.getActiveSession().isOpened() ? View.VISIBLE : View.GONE);

		likeText.setVisibility(item.isFromFacebook() ? View.VISIBLE : View.GONE);
		facebookLoginButton.setVisibility(Session.getActiveSession().isOpened() ? View.GONE : View.VISIBLE);
	}

	public String getDetailUrl() {
		if (item instanceof NormalNieuwsItem)
			return ((NormalNieuwsItem) item).getDetailUrl();
		else
			return "http://www.facebook.com/" + ((FacebookNieuwsItem) item).getFbId().replace("_", "/posts/");
	}

	public boolean hasPhotos() {
		return item.getPhotoCount() > 0;
	}

	public boolean hasComments() {
		try {
			return ((FacebookNieuwsItem) item).getCommentCount() > 0;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putString("nieuwsId", item.getId());
	}

	public void onLikedChanged(final boolean liked) {
		final FacebookNieuwsItem fbni;
		try {
			fbni = ((FacebookNieuwsItem) item);
		} catch (ClassCastException e) {
			return;
		}

		if (!Session.getActiveSession().getPermissions().contains("publish_actions")
				|| !Session.getActiveSession().getPermissions().contains("publish_stream")) {

			Session.getActiveSession().requestNewPublishPermissions(
					new NewPermissionsRequest(act, "publish_actions", "publish_stream"));
			Session.getActiveSession().addCallback(new StatusCallback() {

				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.getPermissions().contains("publish_actions") && session.getPermissions().contains("publish_stream")) {
						onLikedChanged(liked);
						Session.getActiveSession().removeCallback(this);
					}
				}
			});
			return;
		}

		Request request = Request.newGraphPathRequest(Session.getActiveSession(), fbni.getFbId() + "/likes", new Callback() {

			@Override
			public void onCompleted(Response response) {
				if (response.getError() == null) {
					fbni.setLiked(liked);
					likeButton.setImageResource(liked ? R.drawable.fb_liked : R.drawable.fb_like);
					likeText.setText(Util.getLikeString(fbni, act.getUserName()));

					// Log.d("like", "success");
				} else
					likeButton.setImageResource(!liked ? R.drawable.fb_liked : R.drawable.fb_like);
				likeButton.setEnabled(true);
				likeLoader.setVisibility(View.GONE);

			}
		});
		if (liked)
			request.setHttpMethod(HttpMethod.POST);
		else
			request.setHttpMethod(HttpMethod.DELETE);

		request.executeAsync();
		// Log.d("like", "executed async");
		likeButton.setEnabled(false);
		likeLoader.setVisibility(View.VISIBLE);
		likeButton.setImageBitmap(null);
	}

	public static class LinkifyExtra extends Linkify {
		public static Spanned addLinksHtmlAware(String htmlString) {
			// gather links from html
			Spanned spann = Html.fromHtml(htmlString);
			URLSpan[] old = spann.getSpans(0, spann.length(), URLSpan.class);
			List<Pair<Integer, Integer>> htmlLinks = new ArrayList<Pair<Integer, Integer>>();
			for (URLSpan span : old)
				htmlLinks.add(new Pair<Integer, Integer>(spann.getSpanStart(span), spann.getSpanEnd(span)));
			// linkify spanned, html link will be lost
			Linkify.addLinks((Spannable) spann, Linkify.ALL);
			// add html links back
			for (int i = 0; i < old.length; i++)
				((Spannable) spann).setSpan(old[i], htmlLinks.get(i).first, htmlLinks.get(i).second,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			return spann;
		}
	}

	public void showPhotos() {
		act.showPhotoDialog(item.getPhotos(), photoIndex, new OnPhotoPagerDialogPageChangedListener() {

			@Override
			public void onPhotoPagerDialogPageChanged(int pageIndex) {
				photoIndex = pageIndex;
			}

		});
	}

	public void cancelTasks() {
		if (commentLoader != null && !commentLoader.isCancelled())
			commentLoader.cancel(true);
	}

	public String getContent() {
		return item.getContent();
	}
}
package com.markbuikema.juliana32.sections;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.model.TeaserNieuwsItem;
import com.markbuikema.juliana32.tools.CommentAdapter;
import com.markbuikema.juliana32.tools.FacebookHelper.CommentLoader;
import com.markbuikema.juliana32.tools.PhotoSharer;
import com.markbuikema.juliana32.tools.PictureRetriever;
import com.markbuikema.juliana32.tools.Tools;

public class NieuwsDetail {

	private static final String TAG = "NieuwsDetail";

	private static final String NEW_LINE = "NEWLINEREFERENCE1337";

	private MainActivity act;
	private NieuwsItem item;

	private TextView title;
	private TextView subTitle;
	private TextView date;
	private ImageView logo;
	private ImageButton showCommentsButton;
	private ProgressBar loader;
	private ListView comments;
	private LinearLayout commentContainer;
	private CommentAdapter commentAdapter;
	private LinearLayout photoContainer;
	private TextView content;
	private ImageView commentProfilePic;
	private EditText commentInput;

	public NieuwsDetail(final MainActivity act, final NieuwsItem item) {
		this.act = act;
		this.item = item;

		View mainView = act.findViewById(R.id.nieuwsDetailView);

		title = (TextView) mainView.findViewById(R.id.nieuwsDetailTitle);
		subTitle = (TextView) mainView.findViewById(R.id.nieuwsDetailSubtitle);
		content = (TextView) mainView.findViewById(R.id.nieuwsContent);
		date = (TextView) mainView.findViewById(R.id.nieuwsDetailDate);
		logo = (ImageView) mainView.findViewById(R.id.nieuwsDetailIcon);
		commentContainer = (LinearLayout) mainView.findViewById(R.id.commentContainer);
		comments = (ListView) mainView.findViewById(R.id.commentList);
		commentInput = (EditText) mainView.findViewById(R.id.commentInput);
		commentProfilePic = (ImageView) mainView.findViewById(R.id.commentProfilePic);
		showCommentsButton = (ImageButton) act.findViewById(R.id.menuComments);
		loader = (ProgressBar) act.findViewById(R.id.loading);

		title.setText(item.getTitle());

		commentInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
		commentInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				Session.openActiveSession(act, true, new Session.StatusCallback() {

					@Override
					public void call(Session session, SessionState state, Exception exception) {

					}
				});

				return true;
			}
		});

		commentAdapter = new CommentAdapter(act);
		comments.setAdapter(commentAdapter);
		comments.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/"
						+ commentAdapter.getItem(position).getUserId())));
			}
		});

		if (item.getSubTitle() == null)
			subTitle.setVisibility(View.GONE);
		else
			subTitle.setText(Html.fromHtml("<i>" + item.getSubTitle() + "</i>"));

		showCommentsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (commentContainer.getVisibility() == View.VISIBLE) {
					commentContainer.setVisibility(View.GONE);
					return;
				}

				String id = ((FacebookNieuwsItem) item).getFbId();
				new CommentLoader() {

					@Override
					protected void onPreExecute() {
						showCommentsButton.setVisibility(View.GONE);
						loader.setVisibility(View.VISIBLE);
						commentAdapter.clear();
					}

					@Override
					protected void onProgressUpdate(Comment... values) {
						for (Comment comment : values)
							commentAdapter.add(comment);
					}

					@Override
					protected void onPostExecute(Void result) {
						commentContainer.setVisibility(View.VISIBLE);
						showCommentsButton.setVisibility(View.VISIBLE);
						loader.setVisibility(View.GONE);
					}
				}.execute(id);

			}
		});

		String contentString = item.getContent();
		if (contentString == null)
			contentString = "error";
		contentString = contentString.replaceAll(NEW_LINE, "\n");
		contentString = contentString.replaceAll(Character.toString((char) 65532), "");

		content.setText(android.text.Html.fromHtml(contentString.replace("\n", "<br />")));
		content.setMovementMethod(LinkMovementMethod.getInstance());

		if (item instanceof NormalNieuwsItem)
			date.setText(Tools.getDateString(((NormalNieuwsItem) item).getCreatedAt()));

		if (item.isFromFacebook()) {
			content.setAutoLinkMask(Linkify.ALL);
			logo.setImageBitmap(Tools.getFacebookLogo(act));
		} else
			logo.setImageBitmap(Tools.getJulianaLogo(act));

		photoContainer = (LinearLayout) act.getPhotoDrawerView().findViewById(R.id.newsPhotoContainer);
		populatePhotos();
	}

	protected void postComment(String message) {

		JSONObject json = new JSONObject();
		JSONObject comment = new JSONObject();
		try {
			comment.put("comment", json);
			json.put("message", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d("SESSION", "TOKEN: " + Session.getActiveSession().getAccessToken());

		GraphObject object = GraphObject.Factory.create(json);
		Request.executePostRequestAsync(Session.getActiveSession(), String.valueOf(item.getId()), object, new Callback() {

			@Override
			public void onCompleted(Response response) {
				Log.d("POST", response.toString() + " hoi");
				act.showAndClickCommentsButton();
			}
		});

	}

	private void populatePhotos() {
		photoContainer.removeAllViews();

		for (int i = 0; i < item.getPhotoCount(); i++) {
			final String url = item.getPhoto(i);
			View view = LayoutInflater.from(act).inflate(R.layout.photo_item, null);
			final ImageView image = (ImageView) view.findViewById(R.id.photoView);
			final ImageButton shareButton = (ImageButton) view.findViewById(R.id.photoShareButton);

			shareButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new PhotoSharer(act).execute(url, item.getTitle());
				}
			});
			new PictureRetriever() {
				@Override
				protected void onPostExecute(Bitmap result) {
					image.setImageBitmap(result);
					shareButton.setVisibility(View.VISIBLE);
				};

			}.execute(url);
			view.setPadding(15, 15, 15, 15);
			photoContainer.addView(view);
		}
	}

	public String getTitle() {
		return item.getTitle();
	}

	public String getDetailUrl() {
		if (item instanceof NormalNieuwsItem)
			return ((NormalNieuwsItem) item).getDetailUrl();
		else
			if (item instanceof TeaserNieuwsItem)
				return ((TeaserNieuwsItem) item).getDetailUrl();
			else
				return null;
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

	public boolean isCommentsPanelOpened() {
		return commentContainer.getVisibility() == View.VISIBLE;
	}

	public void hideComments() {
		commentContainer.setVisibility(View.GONE);
	}

}
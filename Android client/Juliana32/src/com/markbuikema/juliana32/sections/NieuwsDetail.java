package com.markbuikema.juliana32.sections;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activities.MainActivity;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.NormalNieuwsItem;
import com.markbuikema.juliana32.tools.PhotoSharer;
import com.markbuikema.juliana32.tools.PictureRetriever;

public class NieuwsDetail {

	private static final String TAG = "NieuwsDetail";

	private static final String NEW_LINE = "NEWLINEREFERENCE1337";

	private static final String IMAGE = "IMAGEREFERENCE1337";

	private MainActivity act;
	private NieuwsItem item;

	private TextView title;
	private TextView subTitle;
	private TextView date;

	private LinearLayout photoContainer;

	private TextView content;

	public NieuwsDetail(final MainActivity act, NieuwsItem item) {
		this.act = act;
		this.item = item;

		View mainView = act.findViewById(R.id.nieuwsDetailView);

		title = (TextView) mainView.findViewById(R.id.nieuwsDetailTitle);
		subTitle = (TextView) mainView.findViewById(R.id.nieuwsDetailSubtitle);
		content = (TextView) mainView.findViewById(R.id.nieuwsContent);
		date = (TextView) mainView.findViewById(R.id.nieuwsDetailDate);

		title.setText(item.getTitle());
		
		
		subTitle.setText(Html.fromHtml("<i>" + item.getSubTitle() + "</i>"));
		
		String contentString = item.getContent();
		contentString = contentString.replaceAll(NEW_LINE, "\n");
		contentString = contentString.replaceAll(Character.toString((char) 65532), "");

		content.setText(android.text.Html.fromHtml(contentString.replace("\n", "<br />")));
		content.setMovementMethod(LinkMovementMethod.getInstance());

		if (item instanceof NormalNieuwsItem) {
			date.setText(((NormalNieuwsItem) item).getCreatedAtString());
		}

		photoContainer = (LinearLayout) act.getPhotoDrawerView().findViewById(R.id.newsPhotoContainer);
		populatePhotos();
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
		return item.getDetailUrl();
	}

	public boolean hasPhotos() {
		return item.getPhotoCount() > 0;
	}

}
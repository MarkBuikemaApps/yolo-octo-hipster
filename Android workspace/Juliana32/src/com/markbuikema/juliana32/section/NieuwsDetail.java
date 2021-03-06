package com.markbuikema.juliana32.section;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.markbuikema.juliana32.R;
import com.markbuikema.juliana32.activity.MainActivity;
import com.markbuikema.juliana32.adapter.NieuwsAdapter;
import com.markbuikema.juliana32.model.Comment;
import com.markbuikema.juliana32.model.FacebookNieuwsItem;
import com.markbuikema.juliana32.model.Like;
import com.markbuikema.juliana32.model.NieuwsItem;
import com.markbuikema.juliana32.model.UserInfo;
import com.markbuikema.juliana32.model.WebsiteNieuwsItem;
import com.markbuikema.juliana32.ui.Toaster;
import com.markbuikema.juliana32.util.FacebookHelper.CommentLoader;
import com.markbuikema.juliana32.util.Util;
import com.markbuikema.juliana32.util.Util.LinkifyExtra;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class NieuwsDetail {

	private static final String TAG = "NieuwsDetail";

	protected static final int MAX_COMMENT_LENGTH = 200;

	protected static final int MIN_COMMENT_LENGTH = 3;

	private static final long REFRESH_INTERVAL = 30000;
	private static final long REFRESH_DELAY = 10000;

	private MainActivity act;
	private NieuwsItem item;
	private View clickedView;

	private int actionBarHeight;

	private View likeView;
	private View commentView;

	private FrameLayout nieuwsDetailContainer;
	private View animationView;
	private View nieuwsDetailView;

	private EditText commentInputText;
	private ImageView commentInputPicture;
	private ProgressBar commentInputLoader;
	private TextView commentInputName;
	private LinearLayout commentInputWrapper;
	private Button facebookLoginButton;
	private LinearLayout commentsContainer;

	private Timer refreshScheduler;

	private AnimatorListener onDetailShown = new AnimatorListener1();
	private AnimatorListener onTransformInComplete = new AnimatorListener2();
	private AnimatorListener onFadedIn = new AnimatorListener3();
	private AnimatorListener onOutAnimationCompleted = new AnimatorListener4();
	private AnimatorListener onTransformOutComplete = new AnimatorListener5();
	private AnimatorListener onFadedOut = new AnimatorListener6();

	// TODO make it work when clickedView == null
	public NieuwsDetail( final MainActivity act, final View clickedView, final NieuwsItem item ) {
		this.act = act;
		this.item = item;
		this.clickedView = clickedView;

		actionBarHeight = act.getResources().getDimensionPixelSize( R.dimen.nieuws_header_margin );

		ViewPropertyAnimator.animate( clickedView ).alpha( 0 ).setDuration( 50 ).setStartDelay( 50 );

		// the view of an empty card that will transform from clicked view to
		// full
		// detail view
		animationView = act.findViewById( R.id.animationCard );

		// the container that contains the detail view and the animation view
		nieuwsDetailContainer = (FrameLayout) act.findViewById( R.id.nieuwsDetailContainer );

		// inflate a new instance of the nieuwsdetail view
		nieuwsDetailView = LayoutInflater.from( act ).inflate(
				item.isFromFacebook() ? R.layout.nieuwsdetail_facebook : R.layout.nieuwsdetail_website );
		nieuwsDetailView.setVisibility( View.INVISIBLE );

		// assign the like and comment views
		likeView = nieuwsDetailView.findViewById( R.id.likesView );
		commentView = nieuwsDetailView.findViewById( R.id.commentsView );

		// ensures only the animation view is in the container
		while ( nieuwsDetailContainer.getChildCount() > 1 )
			nieuwsDetailContainer.removeViewAt( 1 );

		// adds the nieuwsdetailview to the container
		LayoutParams lp = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL );
		nieuwsDetailContainer.addView( nieuwsDetailView, lp );

		// waits for the detail view to layout, continues initializing the
		// animation
		nieuwsDetailView.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				// remove the listener so it doesn't trigger on next layout
				Util.removeOnGlobalLayoutListener( nieuwsDetailView, this );

				putContent( nieuwsDetailView, item );

				// sets the starting position for the animationView
				int startX = clickedView.getLeft() - nieuwsDetailView.getLeft();
				int startY = clickedView.getTop() - nieuwsDetailView.getTop();
				int startWidth = clickedView.getWidth();
				int startHeight = clickedView.getHeight();

				LayoutParams animationParams = new LayoutParams( startWidth, startHeight );
				animationView.setLayoutParams( animationParams );
				ViewHelper.setTranslationX( animationView, startX );
				ViewHelper.setTranslationY( animationView, startY );
				animationView.requestLayout();

				ViewHelper.setAlpha( animationView, 0.0f );
				animationView.setVisibility( View.VISIBLE );
				ViewPropertyAnimator.animate( animationView ).alpha( 1.0f ).setListener( onFadedIn ).setDuration( 150 );

			}
		} );

		if ( item.isFromFacebook() ) {
			FacebookNieuwsItem fni = (FacebookNieuwsItem) item;

			LinearLayout likesContainer = (LinearLayout) likeView.findViewById( R.id.nieuwsitem_likes_container );
			populateLikes( act, likesContainer, fni );

			commentsContainer = (LinearLayout) commentView.findViewById( R.id.nieuwsitem_comments_container );
			populateComments( act, commentsContainer, fni );

			TextView likeCaption = (TextView) likeView.findViewById( R.id.nieuwsitem_likes_caption );
			likeCaption.setTypeface( Util.getRobotoCondensed( act ) );

			TextView commentCaption = (TextView) commentView.findViewById( R.id.nieuwsitem_comments_caption );
			commentCaption.setTypeface( Util.getRobotoCondensed( act ) );

			constructCommentsView();
			constructLikesView();

			onFacebookSessionChange( Session.getActiveSession() );

			refreshScheduler = new Timer();
			refreshScheduler.schedule( new TimerTask() {

				@Override
				public void run() {
					refreshComments();
				}
			}, REFRESH_DELAY, REFRESH_INTERVAL );
		}

	}

	private void constructLikesView() {
		// TODO Auto-generated method stub
	}

	private void constructCommentsView() {

		commentInputLoader = (ProgressBar) act.findViewById( R.id.commentInputLoader );
		commentInputPicture = (ImageView) act.findViewById( R.id.commentInputPicture );
		commentInputName = (TextView) act.findViewById( R.id.commentInputName );
		commentInputText = (EditText) act.findViewById( R.id.commentInputText );
		commentInputWrapper = (LinearLayout) act.findViewById( R.id.commentInput );
		facebookLoginButton = (Button) act.findViewById( R.id.facebookLoginButton );

		commentInputName.setTypeface( Util.getRobotoCondensed( act ) );
		commentInputText.setTypeface( Util.getRobotoLight( act ) );
		facebookLoginButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {
				act.onClickLogin();
			}
		} );

		commentInputText.setOnEditorActionListener( new OnEditorActionListener() {

			@Override
			public boolean onEditorAction( android.widget.TextView v, int actionId, KeyEvent event ) {
				String comment = commentInputText.getText().toString();

				if ( comment.length() < MIN_COMMENT_LENGTH ) {
					Toaster.toast( act, "Deze reactie is te kort." );
					commentInputText.requestFocus();
					return false;
				}

				if ( comment.length() > MAX_COMMENT_LENGTH ) {
					Toaster.toast( act, "Deze reactie is " + ( comment.length() - MAX_COMMENT_LENGTH ) + " tekens te lang." );
					commentInputText.requestFocus();
					return false;
				}

				postComment( comment );
				return true;
			}
		} );

	}

	public void hide() {
		if ( refreshScheduler != null )
			refreshScheduler.cancel();

		animationView.setVisibility( View.VISIBLE );
		ViewHelper.setAlpha( animationView, 0f );
		ViewPropertyAnimator.animate( animationView ).alpha( 1 ).setDuration( 150 ).setListener( onFadedOut ).start();
		if ( likeView != null )
			ViewPropertyAnimator.animate( likeView ).alpha( 0 ).setDuration( 150 );
		if ( commentView != null )
			ViewPropertyAnimator.animate( commentView ).alpha( 0 ).setDuration( 150 );
	}

	@TargetApi (Build.VERSION_CODES.HONEYCOMB_MR2 )
	private Point getCardDimensions() {
		Point dims = new Point();
		WindowManager wm = (WindowManager) act.getSystemService( Context.WINDOW_SERVICE );
		Display d = wm.getDefaultDisplay();
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 )
			d.getSize( dims );
		else {
			dims.x = d.getWidth();
			dims.y = d.getHeight();
		}

		int columnCount = act.getResources().getInteger( R.integer.columnCount );

		switch ( columnCount ) {
		case 2:
			dims.x = (int) ( dims.x * .75f );
			break;
		case 3:
			dims.x = (int) ( dims.x * .60f );
			break;
		}
		ViewGroup container = (ViewGroup) nieuwsDetailView.findViewById( R.id.detailContainer );
		int measuredHeight = container.getChildAt( 0 ).getMeasuredHeight();
		dims.y = Math.min( dims.y, measuredHeight );

		return dims;
	}

	@TargetApi (Build.VERSION_CODES.HONEYCOMB )
	private void putContent( View detailView, NieuwsItem item ) {

		ViewGroup detailContainer = (ViewGroup) detailView.findViewById( R.id.detailContainer );
		detailContainer.addView( getDetailView() );

		detailView.getLayoutParams().width = getCardDimensions().x;
		detailView.requestLayout();
	}

	@TargetApi (Build.VERSION_CODES.HONEYCOMB )
	private View getDetailView() {
		View view = null;
		if ( item instanceof WebsiteNieuwsItem )
			view = NieuwsAdapter.constructWebsiteView( act, item, null );
		else if ( item instanceof FacebookNieuwsItem )
			if ( item.isPhoto() )
				view = NieuwsAdapter.constructFacebookPhotoView( act, (FacebookNieuwsItem) item, null );
			else
				view = NieuwsAdapter.constructFacebookView( act, (FacebookNieuwsItem) item, null );

		TextView subTitleView = (TextView) view.findViewById( R.id.nieuwsitem_subtitle );
		TextView contentView = (TextView) view.findViewById( R.id.nieuwsitem_content );
		if ( contentView == null )
			contentView = subTitleView;
		contentView.setText( Util.trimTrailingWhitespace( LinkifyExtra.addLinksHtmlAware( item.getContent() ) ) );
		contentView.setMovementMethod( LinkMovementMethod.getInstance() );
		contentView.setLinksClickable( true );
		if ( Build.VERSION.SDK_INT >= 11 )
			contentView.setTextIsSelectable( true );

		contentView.setTypeface( Util.getRobotoSlabLight( act ) );

		subTitleView.setVisibility( View.GONE );
		contentView.setVisibility( View.VISIBLE );

		return view;
	}

	public String getDetailUrl() {
		if ( item instanceof WebsiteNieuwsItem )
			return ( (WebsiteNieuwsItem) item ).getDetailUrl();
		else
			return "http://www.facebook.com/" + ( (FacebookNieuwsItem) item ).getFbId().replace( "_", "/posts/" );
	}

	public boolean hasPhotos() {
		return item.isPhoto();
	}

	public boolean hasComments() {
		try {
			return ( (FacebookNieuwsItem) item ).getComments().size() > 0;
		} catch ( ClassCastException e ) {
			return false;
		}
	}

	public void onSaveInstanceState( Bundle outState ) {
		outState.putString( "nieuwsId", item.getId() );
	}

	public String getContent() {
		return item.getContent();
	}

	private static View getCommentView( final Context act, final Comment comment ) {
		View view = LayoutInflater.from( act ).inflate( R.layout.listitem_comment, null );

		TextView name = (TextView) view.findViewById( R.id.commentName );
		TextView date = (TextView) view.findViewById( R.id.commentDate );
		TextView text = (TextView) view.findViewById( R.id.commentMessage );
		final ImageView pic = (ImageView) view.findViewById( R.id.commentPicture );

		name.setTypeface( Util.getRobotoCondensed( act ) );
		date.setTypeface( Util.getRobotoCondensed( act ) );
		text.setTypeface( Util.getRobotoLight( act ) );

		name.setText( comment.getName() );

		String dateString = Util.getDateString( act, comment.getCreatedAt() );
		// Log.d("comment_date_adapter", dateString);
		date.setText( dateString );
		text.setText( comment.getText() );
		UrlImageViewHelper.setUrlDrawable( pic, comment.getImgUrl() );

		pic.setContentDescription( comment.getName() );
		view.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View arg0 ) {
				try {
					act.getPackageManager().getPackageInfo( "com.facebook.katana", 0 );
					act.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "fb://profile/" + comment.getUserId() ) ) );
				} catch ( Exception e ) {
					act.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://www.facebook.com/" + comment.getUserId() ) ) );
				}
			}
		} );

		return view;
	}

	private View getLikeView( final Context act, final Like like ) {
		final View view = LayoutInflater.from( act ).inflate( R.layout.listitem_like );
		final ImageView image = (ImageView) view.findViewById( R.id.like_image );
		UrlImageViewHelper.setUrlDrawable( image, like.getImgUrl() );

		image.setContentDescription( like.getName() );
		return view;
	}

	public static void populateComments( Context context, LinearLayout container, FacebookNieuwsItem item ) {
		if ( item.getComments().size() == 0 ) {
			container.setVisibility( View.GONE );
			return;
		} else
			container.setVisibility( View.VISIBLE );

		container.removeAllViews();
		for ( Comment comment : item.getComments() )
			container.addView( getCommentView( context, comment ) );

	}

	public void populateLikes( Context context, LinearLayout container, FacebookNieuwsItem item ) {
		if ( item.getLikeCount() == 0 ) {
			container.setVisibility( View.GONE );
			return;
		} else
			container.setVisibility( View.VISIBLE );

		while ( container.getChildCount() > 1 )
			container.removeViewAt( 1 );

		for ( Like like : item.getLikes() )
			container.addView( getLikeView( context, like ) );
	}

	public String getTitle() {
		return item.getTitle();
	}

	public void onFacebookSessionChange( Session session ) {
		facebookLoginButton.setVisibility( session == null || !session.isOpened() ? View.VISIBLE : View.GONE );
		commentInputWrapper.setVisibility( session == null || !session.isOpened() ? View.GONE : View.VISIBLE );

		if ( session == null )
			return;

		if ( session.isOpened() ) {
			act.getFacebookUser( new FacebookProfileCallback() {

				@Override
				public void onFacebookProfileRetrieved( UserInfo facebookUser ) {

					if ( facebookUser == null ) {
						commentInputLoader.setVisibility( View.GONE );
						// TODO let the user know he is not connected to
						// internet.
						return;
					}

					Log.d( "facebook_login", "onFacebookProfileRetrieved : " + facebookUser.getName() );

					commentInputName.setText( facebookUser.getName() );
					UrlImageViewHelper.setUrlDrawable( commentInputPicture, facebookUser.getImgUrl(), R.drawable.silhouette,
							new UrlImageViewCallback() {

								@Override
								public void onLoaded( ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache ) {
									commentInputLoader.setVisibility( View.GONE );
								}
							} );
				}
			} );
		}
	}

	protected void postComment( final String inputText ) {
		final Session s = Session.getActiveSession();
		if ( !s.isOpened() )
			return;

		if ( s.getPermissions().contains( "publish_stream" ) && s.getPermissions().contains( "publish_actions" ) ) {

			commentInputLoader.setVisibility( View.VISIBLE );

			JSONObject object = new JSONObject();
			try {
				object.put( "message", inputText );
			} catch ( JSONException e ) {
				e.printStackTrace();
			}
			GraphObject message = GraphObject.Factory.create( object );

			Request postRequest = Request.newPostRequest( s, item.getId() + "/comments", message, new Callback() {

				@Override
				public void onCompleted( Response response ) {
					String toastString;
					if ( response.getError() == null ) {
						toastString = act.getResources().getString( R.string.comment_post_success );

						commentInputText.setText( "" );
						commentInputLoader.setVisibility( View.GONE );

						refreshComments();

					} else
						toastString = act.getResources().getString( R.string.comment_post_failure );

					Toaster.toast( act, toastString );
				}

			} );
			postRequest.executeAsync();

		} else {
			s.addCallback( new StatusCallback() {

				@Override
				public void call( Session session, SessionState state, Exception exception ) {
					if ( session.getPermissions().contains( "publish_stream" ) && session.getPermissions().contains( "publish_actions" ) ) {
						postComment( inputText );
					} else {
						Toaster.toast( act, "U moet toestemming geven als u reacties wilt plaatsen." );
					}

					s.removeCallback( this );
				}
			} );

			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest( act, "publish_stream",
					"publish_actions" );
			s.requestNewPublishPermissions( newPermissionsRequest );
		}

	}

	protected void refreshComments() {
		new CommentLoader() {

			@Override
			protected void onPostExecute( List<Comment> result ) {
				FacebookNieuwsItem fbi = ( (FacebookNieuwsItem) item );
				fbi.setComments( result );
				populateComments( act, commentsContainer, fbi );
			}
		}.execute( item.getId() );
	}

	public interface FacebookProfileCallback {
		public void onFacebookProfileRetrieved( UserInfo facebookUser );
	}

	private class AnimatorListener1 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {
			animationView.setVisibility( View.GONE );

			if ( item.isFromFacebook() ) {
				ViewHelper.setAlpha( likeView, 0 );
				ViewHelper.setAlpha( commentView, 0 );

				likeView.setVisibility( View.VISIBLE );
				commentView.setVisibility( View.VISIBLE );

				ViewPropertyAnimator.animate( likeView ).alpha( 1 ).setDuration( 300 );
				ViewPropertyAnimator.animate( commentView ).alpha( 1 ).setStartDelay( 150 ).setDuration( 300 );

			}

		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {
		}
	}

	private class AnimatorListener2 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {
			ViewHelper.setAlpha( nieuwsDetailView, 0.0f );
			nieuwsDetailView.setVisibility( View.VISIBLE );
			ViewPropertyAnimator.animate( nieuwsDetailView ).alpha( 1 ).setDuration( 150 ).setListener( onDetailShown );
		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {
		}
	}

	private class AnimatorListener3 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {
			final Point dims = getCardDimensions();

			int transX = -animationView.getLeft();
			DisplayMetrics metrics = act.getResources().getDisplayMetrics();
			int width = metrics.widthPixels;
			int x = ( width - dims.x ) / 2;
			transX += x;

			ViewPropertyAnimator.animate( animationView ).translationY( actionBarHeight ).translationX( transX ).setDuration( 200 )
					.setListener( onTransformInComplete );

			final int initWidth = animationView.getWidth();
			final int additionalWidth = dims.x - initWidth;
			final int initHeight = animationView.getHeight();
			final int additionalHeight = dims.y - initHeight;

			Animation anim = new Animation() {
				@Override
				protected void applyTransformation( float interpolatedTime, Transformation t ) {
					animationView.getLayoutParams().width = (int) ( initWidth + additionalWidth * interpolatedTime );
					animationView.getLayoutParams().height = (int) ( initHeight + additionalHeight * interpolatedTime );
					animationView.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			anim.setDuration( 200 );

			animationView.startAnimation( anim );
		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {

		}
	}

	private class AnimatorListener4 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {
			animationView.setVisibility( View.GONE );
		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {
		}
	}

	private class AnimatorListener5 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {
			ViewHelper.setAlpha( clickedView, 1f );
			ViewPropertyAnimator.animate( animationView ).alpha( 0f ).setDuration( 150 ).setListener( onOutAnimationCompleted );
		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {
		}
	}

	private class AnimatorListener6 implements AnimatorListener {
		@Override
		public void onAnimationStart( Animator arg0 ) {
		}

		@Override
		public void onAnimationRepeat( Animator arg0 ) {
		}

		@Override
		public void onAnimationEnd( Animator arg0 ) {

			nieuwsDetailView.setVisibility( View.GONE );

			int transX = clickedView.getLeft() - animationView.getLeft();
			int transY = clickedView.getTop() - animationView.getTop();
			ViewPropertyAnimator.animate( animationView ).translationX( transX ).translationY( transY ).setDuration( 200 )
					.setListener( onTransformOutComplete );
			final int initWidth = animationView.getWidth();
			final int additionalWidth = clickedView.getWidth() - initWidth;
			final int initHeight = animationView.getHeight();
			final int additionalHeight = clickedView.getHeight() - initHeight;

			Animation anim = new Animation() {
				@Override
				protected void applyTransformation( float interpolatedTime, Transformation t ) {
					animationView.getLayoutParams().width = (int) ( initWidth + additionalWidth * interpolatedTime );
					animationView.getLayoutParams().height = (int) ( initHeight + additionalHeight * interpolatedTime );
					animationView.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			anim.setDuration( 200 );

			animationView.startAnimation( anim );
		}

		@Override
		public void onAnimationCancel( Animator arg0 ) {
		}
	}

}
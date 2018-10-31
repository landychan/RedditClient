package chan.landy.redditclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.drawable.ClipDrawable.HORIZONTAL;

public class SubredditsActivity extends AppCompatActivity {

    @BindView (R.id.expanded_image) ImageView expandedImageView;
    @BindView (R.id.subreddit_swipelayout) SwipeRefreshLayout subredditSwipeLayout;
    @BindView (R.id.subreddit_recyclerview) RecyclerView subredditRecyclerView;
    SubredditViewAdapter subredditAdapter;
    ArrayList<Submission> submissionsList;
    private DefaultPaginator<Submission> mSubredditPaginator;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        return true;
                    case R.id.navigation_dashboard:
                        return true;
                    case R.id.navigation_notifications:
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);
        ButterKnife.bind(this);

        submissionsList = new ArrayList<>();

        buildSubredditPaginator("");
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        subredditRecyclerView.setLayoutManager(layoutManager);
        subredditAdapter = new SubredditViewAdapter(submissionsList);
        subredditRecyclerView.setAdapter(subredditAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
        subredditRecyclerView.addItemDecoration(itemDecor);

        subredditRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Load more posts if the user scrolled to the end of the recyclerview
                if (!recyclerView.canScrollVertically(1)) {
                    loadSubreddit();
                }
            }
        });

        subredditSwipeLayout.setOnRefreshListener(() -> {
            submissionsList.clear();
            buildSubredditPaginator("");
            loadSubreddit();
            subredditSwipeLayout.setRefreshing(false);
        });

        loadSubreddit();

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    private void buildSubredditPaginator(String subreddit) {
        DefaultPaginator.Builder<Submission, SubredditSort> paginatorBuilder;
        if(subreddit.length() == 0) {
            paginatorBuilder = App.redditClient.frontPage();
        } else {
            paginatorBuilder = App.redditClient.subreddit(subreddit).posts().limit(50);
        }

        mSubredditPaginator = paginatorBuilder.build();

//        SubredditsActivity.this.setTitle();

    }

    public void loadSubreddit() {

        new LoadSubredditTask().execute("", "", "");
    }


    public class SubredditViewAdapter extends RecyclerView.Adapter<PostViewHolder> {

        ArrayList<Submission> submissions;

        public SubredditViewAdapter(ArrayList<Submission> submissionsList) {
            this.submissions = submissionsList;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_postviewholder, parent, false);
            // set the view's size, margins, paddings and layout parameters
            PostViewHolder postViewHolder = new PostViewHolder(v);
            postViewHolder.postThumbnail.setOnClickListener(v1 -> {
                switch(postViewHolder.postHint) {
                    case "link":
                        // open webview
                        break;
                    case "photo":
                        zoomImageFromThumb(postViewHolder.postThumbnail, postViewHolder.imageUri);
                        break;
                    default:
                        // go to comments?
                        break;

                }
            });
            return postViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int i) {

            Submission submission = submissions.get(i);

            postViewHolder.postTitle.setText(submission.getTitle());
            postViewHolder.postSubreddit.setText(submission.getSubreddit());
            postViewHolder.imageUri = submission.getUrl();

            String linkFlair = submission.getLinkFlairText();
//            postViewHolder.postUpvotes = submission.getVote();
            postViewHolder.postHint = submission.getPostHint();

            if(submission.hasThumbnail()) {
                postViewHolder.loadThumbnail(submission.getThumbnail());
            } else {
                postViewHolder.postThumbnail.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return submissions.size();
        }

        private void zoomImageFromThumb(final View thumbView, String uri) {
            // If there's an animation in progress, cancel it
            // immediately and proceed with this one.
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            // Load the high-resolution "zoomed-in" image.
            final ImageView expandedImageView = (ImageView) findViewById(
                    R.id.expanded_image);

            Picasso.get().load(uri).into(expandedImageView);

            // Calculate the starting and ending bounds for the zoomed-in image.
            // This step involves lots of math. Yay, math.
            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();

            // The start bounds are the global visible rectangle of the thumbnail,
            // and the final bounds are the global visible rectangle of the container
            // view. Also set the container view's offset as the origin for the
            // bounds, since that's the origin for the positioning animation
            // properties (X, Y).
            thumbView.getGlobalVisibleRect(startBounds);
            findViewById(R.id.container)
                    .getGlobalVisibleRect(finalBounds, globalOffset);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            // Adjust the start bounds to be the same aspect ratio as the final
            // bounds using the "center crop" technique. This prevents undesirable
            // stretching during the animation. Also calculate the start scaling
            // factor (the end scaling factor is always 1.0).
            float startScale;
            if ((float) finalBounds.width() / finalBounds.height()
                    > (float) startBounds.width() / startBounds.height()) {
                // Extend start bounds horizontally
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            } else {
                // Extend start bounds vertically
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }

            // Hide the thumbnail and show the zoomed-in view. When the animation
            // begins, it will position the zoomed-in view in the place of the
            // thumbnail.
            thumbView.setAlpha(0f);
            expandedImageView.setVisibility(View.VISIBLE);

            // Set the pivot point for SCALE_X and SCALE_Y transformations
            // to the top-left corner of the zoomed-in view (the default
            // is the center of the view).
            expandedImageView.setPivotX(0f);
            expandedImageView.setPivotY(0f);

            // Construct and run the parallel animation of the four translation and
            // scale properties (X, Y, SCALE_X, and SCALE_Y).
            AnimatorSet set = new AnimatorSet();
            set
                    .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                            startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                            startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                            startScale, 1f))
                    .with(ObjectAnimator.ofFloat(expandedImageView,
                            View.SCALE_Y, startScale, 1f));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;

            // Upon clicking the zoomed-in image, it should zoom back down
            // to the original bounds and show the thumbnail instead of
            // the expanded image.
            final float startScaleFinal = startScale;
            expandedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }

                    // Animate the four positioning/sizing properties in parallel,
                    // back to their original values.
                    AnimatorSet set = new AnimatorSet();
                    set.play(ObjectAnimator
                            .ofFloat(expandedImageView, View.X, startBounds.left))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.Y,startBounds.top))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_Y, startScaleFinal));
                    set.setDuration(mShortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });
                    set.start();
                    mCurrentAnimator = set;
                }
            });
        }

    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.post_title) TextView postTitle;
        @BindView(R.id.post_subreddit) TextView postSubreddit;
        @BindView(R.id.post_upvotes) TextView postUpvotes;
//        @BindView(R.id.post_flair) TextView postFlair;
        @BindView(R.id.post_thumbnail) AppCompatImageView postThumbnail;
        String imageUri;
        String postHint;

        public PostViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }

        private void loadThumbnail(String url) {
            Picasso.get()
                    .load(url)
//                .placeholder(R.drawable.user_placeholder)
//                .error(R.drawable.user_placeholder_error)
                    .into(this.postThumbnail);

        }
    }

    private class LoadSubredditTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String ...param) {
            SubredditsActivity.this.submissionsList.addAll(SubredditsActivity.this.mSubredditPaginator.next());
            return "";
        }
//
        @Override
        protected void onPostExecute(String result) {

            if(submissionsList.size() > 0) {
                SubredditsActivity.this.subredditAdapter.notifyDataSetChanged();
            }
        }

    }

}

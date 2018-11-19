package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.graphics.drawable.ClipDrawable.HORIZONTAL;

public class CommentsFragment extends Fragment {

    final String TAG = "CommentsFragment";
    @BindView(R.id.comments_recyclerview) RecyclerView commentsRecyclerView;
    CommentsAdapter commentsAdapter;
    private SubredditDataViewModel mSubredditViewModel;
    private SubmissionReference mSubmissionReference;
    private Submission mSubmission;
    private RootCommentNode rootCommentNode;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubredditViewModel = ViewModelProviders.of(getActivity()).get(SubredditDataViewModel.class);

        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), HORIZONTAL);
        commentsRecyclerView.addItemDecoration(itemDecor);

        return v;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    void getComments() {

        mSubmissionReference = App.redditClient.submission(mSubredditViewModel.selectedSubmissionId);
//        mSubmission = mSubmissionReference.inspect();
        disposables.add(getCommentsObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RootCommentNode>() {
                    @Override public void onComplete() {
                        Log.d(TAG, "onComplete()");
                        commentsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNext(RootCommentNode rootCommentNode) {
                        Log.d(TAG, "onNext()");
                        commentsAdapter.comments = rootCommentNode.getReplies();
                        commentsAdapter.it = rootCommentNode.walkTree().iterator();
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }
                }));
    }

    Observable<RootCommentNode> getCommentsObservable() {
        return Observable.defer(() -> {
            rootCommentNode = mSubmissionReference.comments();
            mSubmission = mSubmissionReference.inspect();
            return Observable.just(rootCommentNode);
        });
    }

    public class CommentsAdapter extends RecyclerView.Adapter<CommentsViewHolder> {

        List<CommentNode<Comment>> comments;
        Iterator<CommentNode<PublicContribution<?>>> it;

        public CommentsAdapter() {
            this.comments = new ArrayList<>();
        }

        @Override
        public CommentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View v;
            switch(viewType) {
                case 1:
                    v = View.inflate(getContext(), R.layout.layout_comment_title, null);
                    break;
                case 2:
                default:
                    v = View.inflate(getContext(), R.layout.layout_comment, null);
                    break;
            }

            return new CommentsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i) {

            if(i == 0) {
                commentsViewHolder.commentBody.setText(mSubmission.getTitle());
                commentsViewHolder.commentUsername.setText(mSubmission.getAuthor());
                commentsViewHolder.commentSubreddit.setText(mSubmission.getSubreddit());
                commentsViewHolder.commentFlair.setText(mSubmission.getAuthorFlairText());
                commentsViewHolder.commentUpvotes.setText(String.valueOf(mSubmission.getScore()));
                commentsViewHolder.commentTimestamp.setText(ClientUtils.getTimeAgo(mSubmission.getCreated().getTime()));
                if(Objects.equals(mSubmission.getPostHint(), "image")) {
                    Picasso.get().load(mSubmission.getUrl()).into(commentsViewHolder.commentsImage);
                    commentsViewHolder.commentsImage.setVisibility(View.VISIBLE);
                } else {
                    commentsViewHolder.commentsImage.setVisibility(View.GONE);
                }
            } else {
                CommentNode commentNode = it.next();
                if (commentNode != null) {
                    PublicContribution subject = commentNode.getSubject();
                    commentsViewHolder.commentUsername.setText(subject.getAuthor());
                    commentsViewHolder.commentUpvotes.setText(String.valueOf(subject.getScore()));
//            commentsViewHolder.commentFlair.setText(subject.getGilded());
                    commentsViewHolder.commentTimestamp.setText(ClientUtils.getTimeAgo(subject.getCreated().getTime()));
                    commentsViewHolder.commentBody.setText(subject.getBody());
                }
            }
        }

        @Override
        public int getItemCount() {
            return commentsAdapter.comments.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return 1;
            else
                return 2;
        }
    }


    class CommentsViewHolder extends RecyclerView.ViewHolder {

        @Nullable @BindView(R.id.comments_image) AppCompatImageView commentsImage;
        @Nullable @BindView(R.id.comment_subreddit) AppCompatTextView commentSubreddit;
        @BindView(R.id.layout_comment_layout) ConstraintLayout commentLayout;
        @BindView(R.id.comment_text) AppCompatTextView commentBody;
        @BindView(R.id.comment_username) AppCompatTextView commentUsername;
        @BindView(R.id.comment_upvotes) AppCompatTextView commentUpvotes;
        @BindView(R.id.comment_flair) AppCompatTextView commentFlair;
        @BindView(R.id.comment_timestamp) AppCompatTextView commentTimestamp;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

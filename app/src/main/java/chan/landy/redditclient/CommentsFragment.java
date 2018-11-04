package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        mSubmissionReference = App.redditClient.submission(mSubredditViewModel.selectedComment);
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
            return Observable.just(rootCommentNode);
        });
    }

    public class CommentsAdapter extends RecyclerView.Adapter<CommentsViewHolder> {

        List<CommentNode<Comment>> comments;
        Iterator<CommentNode<PublicContribution<?>>> it;

        public CommentsAdapter() {
            this.comments = new ArrayList<>();
        }

        @NonNull
        @Override
        public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = View.inflate(getContext(), R.layout.layout_comment, null);
            CommentsViewHolder commentsViewHolder = new CommentsViewHolder(v);
            return commentsViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i) {
            commentsViewHolder.commentTextView.setText(it.next().getSubject().getBody());
        }

        @Override
        public int getItemCount() {
            return commentsAdapter.comments.size();
        }
    }


    class CommentsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_comment_layout) ConstraintLayout commentLayout;
        @BindView(R.id.comment_text) TextView commentTextView;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

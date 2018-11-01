package chan.landy.redditclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.drawable.ClipDrawable.HORIZONTAL;

public class CommentsFragment extends Fragment {

    @BindView(R.id.comments_recyclerview) RecyclerView commentsRecyclerView;
    CommentsAdapter commentsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_comments, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), HORIZONTAL);
        commentsRecyclerView.addItemDecoration(itemDecor);

        return v;


    }

    public class CommentsAdapter extends RecyclerView.Adapter<CommentsViewHolder> {

        @NonNull
        @Override
        public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return 0;
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

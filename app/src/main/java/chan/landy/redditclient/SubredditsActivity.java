package chan.landy.redditclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;

import static android.graphics.drawable.ClipDrawable.HORIZONTAL;

public class SubredditsActivity extends AppCompatActivity {

    @BindView (R.id.subreddit_recyclerview) RecyclerView subredditRecyclerView;
    SubredditViewAdapter subredditAdapter;
    ArrayList<Submission> submissionsList;
    DefaultPaginator<Submission> subredditPaginator;

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

        DefaultPaginator.Builder<Submission, SubredditSort> paginatorBuilder = App.redditClient.frontPage();
        subredditPaginator = paginatorBuilder.build();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        subredditRecyclerView.setLayoutManager(layoutManager);
        subredditAdapter = new SubredditViewAdapter(submissionsList);
        subredditRecyclerView.setAdapter(subredditAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
        subredditRecyclerView.addItemDecoration(itemDecor);

        loadSubreddit();
    }

    public void loadSubreddit() {

        new LoadSubredditTask().execute("", "", "");


//        submissionsList.addAll(subredditPaginator.next());
//        subredditAdapter.notifyDataSetChanged();
    }


    public class SubredditViewAdapter extends RecyclerView.Adapter<PostViewHolder> {

        ArrayList<Submission> submissions;

        public SubredditViewAdapter(ArrayList<Submission> submissionsList) {
            this.submissions = submissionsList;
        }

        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_postviewholder, parent, false);
            // set the view's size, margins, paddings and layout parameters
            return new PostViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int i) {

            Submission submission = submissions.get(i);

            postViewHolder.postTitle.setText(submission.getTitle());
            postViewHolder.postSubreddit.setText(submission.getSubreddit());

        }

        @Override
        public int getItemCount() {
            return submissions.size();
        }

    }

    class PostViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.post_title) TextView postTitle;
        @BindView(R.id.post_subreddit) TextView postSubreddit;

        public PostViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    private class LoadSubredditTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String ...param) {
            SubredditsActivity.this.submissionsList.addAll(SubredditsActivity.this.subredditPaginator.next());
            return "";
        }
//
        @Override
        protected void onPostExecute(String result) {

            if(submissionsList != null) {
//                activity.startActivity(new Intent(activity, UserOverviewActivity.class));
                SubredditsActivity.this.subredditAdapter.notifyDataSetChanged();
            }
        }

    }

}

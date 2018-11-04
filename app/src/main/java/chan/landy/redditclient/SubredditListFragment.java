package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.pagination.Paginator;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.graphics.drawable.ClipDrawable.HORIZONTAL;

public class SubredditListFragment extends Fragment {

    private final String TAG = "SubredditListFragment";
    @BindView(R.id.subreddit_list) RecyclerView subredditList;

    private SubredditDataViewModel mSubredditViewModel;
    SubredditListAdapter subredditListAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubredditViewModel = ViewModelProviders.of(getActivity()).get(SubredditDataViewModel.class);

        getSubscribedSubreddits();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit_list, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        subredditListAdapter = new SubredditListAdapter();
        subredditList.setAdapter(subredditListAdapter);
        subredditList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), HORIZONTAL);
        subredditList.addItemDecoration(itemDecor);
        return view;
    }


    void getSubscribedSubreddits() {
        disposables.add(subscribedSubredditsObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<Subreddit>>() {
                    @Override public void onComplete() {
                        Log.d(TAG, "onComplete()");
                        subredditListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNext(ArrayList<Subreddit> subreddits) {
                        Log.d(TAG, "onNext()");
                        mSubredditViewModel.subscribedList.clear();
                        mSubredditViewModel.subscribedList.addAll(subreddits);
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }
                }));
    }

    Observable<ArrayList<Subreddit>> subscribedSubredditsObservable() {
        return Observable.defer(() -> {

            ArrayList<Subreddit> subList = new ArrayList<>();

            BarebonesPaginator<Subreddit> subreddits = App.redditClient.me().subreddits("subscriber")
                    .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                    .build();
            for (Listing<Subreddit> page : subreddits) {
                subList.addAll(page);
            }

            Collections.sort(subList, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
            return Observable.just(subList);
        });
    }


    public class SubredditListAdapter extends RecyclerView.Adapter<ListViewHolder> {

        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_subreddit_list_item, viewGroup, false);
            ListViewHolder listViewHolder = new ListViewHolder(v);

            listViewHolder.subredditLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return listViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder listViewHolder, int i) {
            Subreddit subreddit = mSubredditViewModel.subscribedList.get(i);

            listViewHolder.subredditTitle.setText(subreddit.getName());
        }

        @Override
        public int getItemCount() {
            return mSubredditViewModel.subscribedList.size();
        }
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.subreddit_list_layout) RelativeLayout subredditLayout;
        @BindView(R.id.subreddit_list_title) TextView subredditTitle;
        @BindView(R.id.subreddit_list_favicon) AppCompatButton subredditFavicon;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

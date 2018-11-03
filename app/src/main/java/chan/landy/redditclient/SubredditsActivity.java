package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SubredditsActivity extends AppCompatActivity {

    final String TAG = "SubredditsActivity";
    private SubredditDataViewModel mSubredditViewModel;

    private SubredditsFragment subredditsFragment;
    private SubredditListFragment subredditListFragment;
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                swapFragments(item.getItemId());
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);
        ButterKnife.bind(this);
        mSubredditViewModel = ViewModelProviders.of(this).get(SubredditDataViewModel.class);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        subredditsFragment = new SubredditsFragment();
        subredditListFragment = new SubredditListFragment();
        swapFragments(R.id.navigation_home);

    }


    private void swapFragments(int itemId) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (itemId) {
            case R.id.navigation_subreddit_list:
                if(subredditListFragment == null) {
                    subredditListFragment = new SubredditListFragment();
                }
                ft.replace(R.id.fragment_container, subredditListFragment);
                break;
            case R.id.navigation_home:
            default:
                if(subredditsFragment == null) {
                    subredditsFragment = new SubredditsFragment();
                }
                ft.replace(R.id.fragment_container, subredditsFragment);
                break;
        }

        ft.addToBackStack(null);
        ft.commit();

    }

}

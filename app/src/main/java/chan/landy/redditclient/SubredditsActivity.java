package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditsActivity extends AppCompatActivity implements SubredditsFragment.OnSubmissionClickedListener {

    final String TAG = "SubredditsActivity";
    private static final int NUM_PAGES = 3;
    private SubredditDataViewModel mSubredditViewModel;

    private SubredditsFragment subredditsFragment;
    private SubredditListFragment subredditListFragment;
    private CommentsFragment commentsFragment;
    private PagerAdapter fragmentAdapter;

    @BindView(R.id.fragment_container) ViewPager fragmentViewPager;
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch(item.getItemId()) {
            case R.id.navigation_home:
                fragmentViewPager.setCurrentItem(1, true);
                break;
            case R.id.navigation_subreddit_list:
                fragmentViewPager.setCurrentItem(0, true);
                break;
        }
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);
        ButterKnife.bind(this);
        mSubredditViewModel = ViewModelProviders.of(this).get(SubredditDataViewModel.class);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentViewPager.setOffscreenPageLimit(NUM_PAGES);
        fragmentViewPager.setAdapter(fragmentAdapter);
        fragmentViewPager.addOnPageChangeListener(new FragmentPageChangeListener());
    }

    // Interface from SubredditsFragment
    @Override
    public void loadCommentsinFragment() {
        if(!mSubredditViewModel.selectedComment.equals("")) {
            commentsFragment.getComments();
            fragmentViewPager.setCurrentItem(2, true);
        }
    }

    private class FragmentPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            Log.d(TAG, "onPageSelected " + position);
            switch(position) {
                case 0:
                    bottomNavigationView.setSelectedItemId(R.id.navigation_subreddit_list);
                    break;
                case 1:
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, String.format("%s %s %s", position, positionOffset, positionOffsetPixels));

            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.d(TAG, "onPageScrollStateChanged " + state);
            super.onPageScrollStateChanged(state);
        }
    }

    private class FragmentAdapter extends FragmentStatePagerAdapter {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0: //R.id.navigation_subreddit_list:
                    if(subredditListFragment == null) {
                        subredditListFragment = new SubredditListFragment();
                    }
                    return subredditListFragment;
                case 1: //R.id.navigation_home:
                    if(subredditsFragment == null) {
                        subredditsFragment = new SubredditsFragment();
                    }
                    return subredditsFragment;
                case 2:
                    if(commentsFragment == null) {
                        commentsFragment = new CommentsFragment();
                    }
                    return commentsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        if(fragmentViewPager.getCurrentItem() == 2) {
            fragmentViewPager.setCurrentItem(1, true);
            commentsFragment.commentsAdapter.comments.clear();
            commentsFragment.commentsAdapter.notifyDataSetChanged();
        } else {
            moveTaskToBack(true);
        }
    }
}

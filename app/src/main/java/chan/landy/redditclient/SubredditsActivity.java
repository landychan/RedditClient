package chan.landy.redditclient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditsActivity extends AppCompatActivity implements SubredditsFragment.OnSubmissionClickedListener, SubredditListFragment.OnSubredditClickedListener {

    final String TAG = "SubredditsActivity";
    private static final int MAX_NUM_PAGES= 3;
    private SubredditDataViewModel mSubredditViewModel;

    private SubredditsFragment subredditsFragment;
    private SubredditListFragment subredditListFragment;
    private CommentsFragment commentsFragment;
    private FragmentAdapter fragmentAdapter;

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
        fragmentViewPager.setAdapter(fragmentAdapter);
        initializeFragments(fragmentAdapter);
        fragmentViewPager.setOffscreenPageLimit(MAX_NUM_PAGES);
        fragmentViewPager.addOnPageChangeListener(new FragmentPageChangeListener());

    }

    // Interface from SubredditsFragment
    @Override
    public void loadCommentsinFragment() {
        if(!mSubredditViewModel.selectedSubmissionId.equals("")) {
            if(!fragmentAdapter.fragmentList.contains(commentsFragment)) {
                fragmentAdapter.addCommentsFragment();
            }
            commentsFragment.getComments();
            fragmentViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void loadSubredditPosts(String subreddit) {
        DefaultPaginator.Builder<Submission, SubredditSort> paginatorBuilder;
        if(subreddit.length() == 0) {
            paginatorBuilder = App.redditClient.frontPage().limit(50);
        } else {
            paginatorBuilder = App.redditClient.subreddit(subreddit).posts().limit(50);
        }

        subredditsFragment.subredditPaginator = paginatorBuilder.build();
        subredditsFragment.subredditAdapter.submissions.clear();
        subredditsFragment.subredditAdapter.notifyDataSetChanged();
        subredditsFragment.loadSubreddit();
        fragmentViewPager.setCurrentItem(1, true);

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
                    if(fragmentAdapter.getCount() == 3) {
                        fragmentAdapter.removeCommentsFragment();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            Log.d(TAG, String.format("%s %s %s", position, positionOffset, positionOffsetPixels));

            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.d(TAG, "onPageScrollStateChanged " + state);
            super.onPageScrollStateChanged(state);
        }
    }

    private class FragmentAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragmentList;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int i) {
            if(fragmentList.size() > i) {
                return fragmentList.get(i);
            } else {
                return null;
            }
//            switch (i) {
//                case 0: //R.id.navigation_subreddit_list:
//                    if(subredditListFragment == null) {
//                        subredditListFragment = new SubredditListFragment();
//                        fragmentList.add(subredditListFragment);
//                    }
//                    return subredditListFragment;
//                case 1: //R.id.navigation_home:
//                    if(subredditsFragment == null) {
//                        subredditsFragment = new SubredditsFragment();
//                        fragmentList.add(subredditsFragment);
//                    }
//                    return subredditsFragment;
//                case 2:
//                    if(commentsFragment == null) {
//                        commentsFragment = new CommentsFragment();
//                        fragmentList.add(commentsFragment);
//                    }
//
//                    return commentsFragment;
//                default:
//                    return null;
//            }
        }

        @Override
        public int getCount() {
            return this.fragmentList.size();
        }

        public void removeCommentsFragment() {
            this.fragmentList.remove(commentsFragment);
            this.notifyDataSetChanged();
        }

        public void addCommentsFragment() {
            this.fragmentList.add(commentsFragment);
            notifyDataSetChanged();
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

    private void initializeFragments(FragmentAdapter adapter) {

        if (subredditListFragment == null) {
            subredditListFragment = new SubredditListFragment();
            adapter.fragmentList.add(subredditListFragment);
        }
        if (subredditsFragment == null) {
            subredditsFragment = new SubredditsFragment();
            adapter.fragmentList.add(subredditsFragment);
        }
        if (commentsFragment == null) {
            commentsFragment = new CommentsFragment();
        }

        adapter.notifyDataSetChanged();
    }
}

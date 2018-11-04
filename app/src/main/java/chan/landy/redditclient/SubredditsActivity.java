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

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditsActivity extends AppCompatActivity {

    final String TAG = "SubredditsActivity";
    private static final int NUM_PAGES = 2;
    private SubredditDataViewModel mSubredditViewModel;

    private SubredditsFragment subredditsFragment;
    private SubredditListFragment subredditListFragment;
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
        fragmentViewPager.setAdapter(fragmentAdapter);
        fragmentViewPager.addOnPageChangeListener(new FragmentPageChangeListener());
//        subredditsFragment = new SubredditsFragment();
//        subredditListFragment = new SubredditListFragment();
//        swapFragments(R.id.navigation_home);

    }


    private void swapFragments(int itemId) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


        ft.addToBackStack(null);
        ft.commit();

    }

    private class FragmentPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
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
                default:
                    if(subredditsFragment == null) {
                        subredditsFragment = new SubredditsFragment();
                    }
                    return subredditsFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}

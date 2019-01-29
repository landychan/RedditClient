package chan.landy.redditclient;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;

public class SubredditDataViewModel extends ViewModel {

    public ArrayList<Submission> submissionsList;
    public ArrayList<Subreddit> subscribedList;
    public String selectedSubmissionId;

    public SubredditDataViewModel() {

        this.submissionsList = new ArrayList<>();
        this.subscribedList = new ArrayList<>();
    }



}

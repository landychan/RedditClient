package chan.landy.redditclient;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

public class SubredditDataViewModel extends ViewModel {

    public ArrayList<Submission> submissionsList;
    public String selectedComment;

    public SubredditDataViewModel() {
        this.submissionsList = new ArrayList<>();
    }



}

package clientreddit.landychan.com.clientforreddit;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedditRestClient {

    SharedPreferences pref;
    String token;
    Context context;
    private static String CLIENT_ID = "Py3Whh_pfsx51w";
    private static final String BASE_URL = "https://www.reddit.com/api/v1/";
    private static String REDIRECT_URI="YOUR reddit_uri(as per your reddit app preferences)";

    RedditRestClient(Context cnt){
        context = cnt;
    }

    private static OkHttpClient okHttpClient = new OkHttpClient();

    public static void get(String url) {
        Request req = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful() && response.body().contentLength() > 0) {

                    }
            }
        });

    }

}

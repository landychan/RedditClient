package clientreddit.landychan.com.clientforreddit;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedditRestClient {

    SharedPreferences pref;
    public static String token = "";
    Context context;
    private static String CLIENT_ID = "Py3Whh_pfsx51w";
    private static final String BASE_URL = "https://www.reddit.com/api/v1/";
    private static final String REDIRECT_URL = "https://localhost";
    private static String GRANT_TYPE="https://oauth.reddit.com/grants/installed_client";
    private static String GRANT_TYPE2="authorization_code";
    private static String TOKEN_URL ="access_token";
    private static String OAUTH_URL ="https://www.reddit.com/api/v1/authorize";
    private static String OAUTH_SCOPE="read";
    private static String DURATION = "permanent";
    private static String clientState;
    private static String SCOPE_STRING = "identity";

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

    public static void getOauthToken() {

        clientState = UUID.randomUUID().toString().split("-")[0];
        String url = "https://www.reddit.com/api/v1/authorize";
        HttpUrl tokenUrl = new HttpUrl.Builder()
                .scheme("https")
                .addPathSegment(url)
                .addQueryParameter("client_id", CLIENT_ID)
                .addQueryParameter("response_type", token)
                .addQueryParameter("state", clientState)
                .addQueryParameter("redirect_uri", REDIRECT_URL)
                .addQueryParameter("duration", DURATION)
                .addQueryParameter("scope", SCOPE_STRING)
                .build();

        Request req = new Request.Builder()
                .url(tokenUrl)
                .build();

        okHttpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body().contentLength() > 0) {
                    token = response.body().string();
                }
            }
        });

    }

}

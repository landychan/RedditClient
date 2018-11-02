package chan.landy.redditclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends Activity {

    final String TAG = "WebViewActivity";

    @BindView(R.id.webview_progressbar) ContentLoadingProgressBar progressBar;
    @BindView(R.id.activity_webview) WebView webView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        Bundle bun = getIntent().getExtras();
        if(bun.containsKey("postlink")) {
            webView.loadUrl(bun.getString("postlink"));
        } else {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }
}

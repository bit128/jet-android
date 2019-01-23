package lanxin.com.jet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class ActivityTemp extends Activity {

    protected WebView webView;
    protected JetResource jetResource;

    protected String pageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jetResource = JetResource.getInstance(ActivityTemp.this);
        //初始化资源
        pageUrl = getIntent().getStringExtra("pageUrl");
        //加载视图
        setContentView(R.layout.activity_temp);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        //绑定桥接器
        new JetBridge(this, webView);
        //页面路由
        route();
        //加载页面内容
        loadPageContent();
    }

    /**
     * 加载页面内容
     */
    protected void loadPageContent() {
        String pageContent = jetResource.loadPage(pageUrl);
        webView.loadData(pageContent, "text/html; charset=UTF-8", null);
    }

    /**
     * 资源路由器
     */
    private void route() {
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("app-page:")) {
                    if (url.equals("app-page:back")) {
                        finish();
                    } else {
                        String pageName = url.substring(9);
                        int i = pageName.indexOf("?");
                        if (i != -1) {
                            pageName = pageName.substring(0, i);
                        }
                        Intent intent = new Intent(ActivityTemp.this, ActivityTemp.class);
                        intent.putExtra("pageUrl", url);
                        startActivity(intent);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.startsWith("app-local:")) {
                    String fileName = url.substring(10, url.length());
                    String extName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    try {
                        InputStream inputStream = getResources().getAssets().open("image/"+fileName);
                        if (extName.equals("mp4")) {
                            return new WebResourceResponse("video/mp4", "UTF-8", inputStream);
                        } else {
                            return new WebResourceResponse("image/" + extName, "UTF-8", inputStream);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
    }
}

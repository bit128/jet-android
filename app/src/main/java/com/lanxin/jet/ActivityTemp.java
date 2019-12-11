package com.lanxin.jet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class ActivityTemp extends Activity {

    protected WebView webView;
    protected AlertDialog alertDialog;
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
        //默认js桥接（目的让js可以检测出平台）
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void initWebkit() {}
        }, "android");
        //绑定原生js弹窗
        bindJSWindow();
        //页面路由
        pageRoute();
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
    private void pageRoute() {
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
                        //尝试加载原生页面
                        Class activity = null;
                        String className = "Activity" + pageName.substring(0,1).toUpperCase() + pageName.substring(1);
                        try {
                            activity = Class.forName(getPackageName() + "." + className);
                        } catch (ClassNotFoundException e) {
                            Log.i("---->", "原生页面未找到");
                        }
                        Intent intent;
                        if (activity != null) {
                            intent = new Intent(ActivityTemp.this, activity);
                        } else {
                            intent = new Intent(ActivityTemp.this, ActivityTemp.class);
                        }
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

    /**
     * 绑定原生js弹窗
     */
    private void bindJSWindow() {

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message,JsResult result) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityTemp.this);
                alert.setMessage(message);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog = alert.create();
                if (! ActivityTemp.this.isDestroyed()) {
                    alertDialog.show();
                }
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityTemp.this);
                alert.setMessage(message);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        result.confirm();
                    }
                });
                alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        result.cancel();
                    }
                });
                //屏蔽点击空白窗口消失问题
                alert.setCancelable(false);
                //屏蔽全部按键，防止返回键关闭窗口
                alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        return true;
                    }
                });
                alertDialog = alert.create();
                if (! ActivityTemp.this.isDestroyed()) {
                    alertDialog.show();
                }
                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                Log.i("---->", "close the window.");
            }
        });
    }
}

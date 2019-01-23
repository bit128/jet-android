package lanxin.com.jet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class JetBridge {

    public static final int MSG_SET_CONTENT       = 1;
    public static final int MSG_GET_CONTENT       = 2;
    public static final int MSG_SELECT_FILE       = 3;
    public static final int MSG_UPLOAD_FILE       = 4;
    public static final int MSG_HTTP_REQUEST      = 5;

    private Context context;
    protected AlertDialog alertDialog;
    protected Handler handler;

    public JetBridge(Context context, WebView webView) {
        this.context = context;
        handler = new BridgeHandler(webView);
        bind(webView);
        jsWindow(webView);
    }

    public static class BridgeHandler extends Handler {

        private final WeakReference<WebView> mTarget;

        public BridgeHandler(WebView webView) {
            mTarget = new WeakReference<>(webView);
        }

        @Override
        public void handleMessage(Message msg) {
            WebView webView = mTarget.get();
            switch (msg.what) {
                case MSG_HTTP_REQUEST:
                    webView.loadUrl("javascript:window.httpRequestCallback(\'"+msg.obj.toString()+"\', \'"+msg.arg1+"\');");
                    break;
            }
        }
    }

    private void bind(WebView webView) {
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void setContent(String params) {
                Log.i("------> bridge params:", params);
            }
            @JavascriptInterface
            public void getContent(String params) {
                Log.i("------> bridge params:", params);
            }
            @JavascriptInterface
            public void selectFile(String params) {
                Log.i("------> bridge params:", params);
            }
            @JavascriptInterface
            public void uploadFile(String params) {
                Log.i("------> bridge params:", params);
            }
            @JavascriptInterface
            public void httpRequest(String params) {
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    JSONObject paramsObj = new JSONObject(params);
                    URL url = new URL(paramsObj.getString("url"));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    //connection.setRequestProperty("Host", HOST);
                    if (paramsObj.getString("method").toUpperCase().equals("POST")) {
                        connection.setRequestMethod("POST");
                        //构建post参数
                        String formDataString = "";
                        JSONObject formData = paramsObj.getJSONObject("formData");
                        Iterator iterator = formData.keys();
                        if (iterator.hasNext()) {
                            String key = iterator.next().toString();
                            formDataString += "&" + key + "=" + formData.getString(key);
                        }
                        if (formDataString.length() > 1) {
                            connection.setDoOutput(true);
                            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                            dos.write(formDataString.substring(1).getBytes("utf-8"));
                            dos.flush();
                            dos.close();
                        }
                    }
                    //获取服务器响应结果
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                        BufferedReader buffer = new BufferedReader(reader);
                        String inputLine;
                        while ((inputLine = buffer.readLine()) != null) {
                            stringBuilder.append(inputLine);
                        }
                        buffer.close();
                        reader.close();
                    }
                    connection.disconnect();
                    if (stringBuilder.toString().length() > 0) {
                        Message message = handler.obtainMessage(MSG_HTTP_REQUEST, stringBuilder.toString());
                        message.arg1 = paramsObj.isNull("trackId") ? 0 : paramsObj.getInt("trackId");
                        handler.sendMessage(message);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, "android");
    }

    /**
     * js原生弹窗
     */
    private void jsWindow(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message,JsResult result) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(message);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog = alert.create();
                alertDialog.show();
                result.confirm();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
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
                alertDialog.show();
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

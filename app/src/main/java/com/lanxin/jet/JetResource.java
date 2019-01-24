package com.lanxin.jet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JetResource {

    public static String DEFAULT_PAGE   = "app-page:home";
    public static String CONFIG_NAME    = "version.json";
    public static String SYNC_LIST      = "sync_list";

    public String serverHost;
    public boolean onlineSync           = false;
    public String cachePath             = "";

    private Context context;
    public static JetResource jetResource;
    public JSONObject localConfig;

    public static JetResource getInstance(Context context) {
        if (jetResource == null) {
            jetResource = new JetResource(context);

        } else {
            jetResource.context = context;
        }
        return jetResource;
    }

    private JetResource(Context context) {
        this.context = context;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            onlineSync = applicationInfo.metaData.getBoolean("onlineSync");
            //已经开启了在线同步功能
            if (onlineSync) {
                serverHost = applicationInfo.metaData.getString("serverHost");
                //加载或创建缓存目录
                if (cachePath.equals("")) {
                    if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                        cachePath = Environment.getExternalStorageDirectory().toString();
                        if (! cachePath.equals("")) {
                            cachePath += "/" + context.getPackageName() + "/caches/";
                            //加载本地配置文件
                            File file = new File(cachePath + CONFIG_NAME);
                            if (file.exists()) {
                                BufferedReader bufferedReader;
                                StringBuilder stringBuilder = new StringBuilder();
                                try {
                                    bufferedReader = new BufferedReader(new FileReader(file));
                                    String read;
                                    while ((read = bufferedReader.readLine()) != null) {
                                        stringBuilder.append(read);
                                    }
                                    bufferedReader.close();
                                    if (! stringBuilder.toString().equals("")) {
                                        localConfig = new JSONObject(stringBuilder.toString());
                                    }
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            onlineSync = false;
                        }
                    } else {
                        //无法获取内存卡路径
                        onlineSync = false;
                    }
                }
            } else {
                Log.i("======>", "未开启在线同步功能");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 加载页面内容
     * @param pageUrl 页面url
     * @return
     */
    public String loadPage(String pageUrl) {
        if (pageUrl == null || pageUrl.length() < 10) {
            pageUrl = DEFAULT_PAGE;
        }
        //解析url
        String pageName = pageUrl.substring(9);
        String pageParams = "";
        int i = pageName.indexOf("?");
        if (i != -1) {
            pageParams = pageName.substring(i + 1);
            pageName = pageName.substring(0, i);
        }
        String content = loadFile(pageName + ".html");
        //匹配资源文件
        Matcher matcher = Pattern.compile("<asset>([\\w\\-\\.]*?)<\\/asset>").matcher(content);
        while (matcher.find()) {
            String sourceFile = matcher.group(1);
            String extName = sourceFile.substring(sourceFile.lastIndexOf(".") + 1);
            if (extName.equals("css")) {
                content = content.replace(matcher.group(0), "<style type=\"text/css\">"
                        +loadFile(matcher.group(1))+"</style>");
            } else if (extName.equals("js")) {
                content = content.replace(matcher.group(0), "<script type=\"text/javascript\">"
                        +loadFile(matcher.group(1))+"</script>");
            }
        }
        return content;
    }

    /**
     * 加载文件
     * @param fileName 文件名
     * @return
     */
    private String loadFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader;
            String read;
            File file = new File(cachePath + fileName);
            if (file.exists() && onlineSync) {
                bufferedReader = new BufferedReader(new FileReader(file));
                while ((read = bufferedReader.readLine()) != null) {
                    stringBuilder.append(read);
                }
            } else {
                InputStream inputStream = context.getResources().getAssets().open(fileName);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                while ((read = bufferedReader.readLine()) != null) {
                    stringBuilder.append(read);
                }
                inputStreamReader.close();
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

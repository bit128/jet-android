package com.lanxin.jet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class JetNet {

    public static final String POST = "POST";
    public static final String GET = "GET";
    private static final String FORM_END = "\r\n";
    private static final String FORM_LINE = "--";
    private static final String FORM_BOUNDARY = "*****";
    private static final int BUFFER_SIZE = 1024;

    private String params;
    private String method;

    public JetNet() {
        this.method = GET;
    }

    /**
     * 设置请求方法
     * @param method GET | POST
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 设置post请求参数
     * @param params
     */
    public void setParams(String params) {
        this.params = params;
    }

    /**
     * 设置post请求参数
     * @param params
     */
    public void setParams(Map<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            stringBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
        }
        if (stringBuilder.length() > 0) {
            this.params = stringBuilder.toString().substring(1);
        }
        this.method = POST;
    }

    /**
     * 发送网络请求
     * @param url
     * @return
     */
    public String sendRequest(String url) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL urls = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //传递参数
            if (params != null && !params.equals("")) {
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(params.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
            }
            //获取服务器响应结果
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String read;
                while ((read = buffer.readLine()) != null) {
                    stringBuilder.append(read);
                }
                buffer.close();
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 上传（图片）文件
     * @param url
     * @param filePath
     */
    public String uploadFile(String url, String filePath) {
        String response = "";
        String fileName = filePath.substring(filePath.lastIndexOf("/")+1, filePath.length());
        String extName = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
        try {
            URL urls = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
            connection.setRequestMethod(POST);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + FORM_BOUNDARY);
            //输出流
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            //写入参数
            outputStream.writeBytes(FORM_LINE + FORM_BOUNDARY + FORM_END);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file_name\"; filename=" + fileName + FORM_END);
            if (extName.equals("jeg") || extName.equals("jpeg")) {
                outputStream.writeBytes("Content-Type: image/jpeg" + FORM_END);
            } else if (extName.equals("png")) {
                outputStream.writeBytes("Content-Type: image/png" + FORM_END);
            } else {
                outputStream.writeBytes("Content-Type: application/octet-stream" + FORM_END);
            }
            outputStream.writeBytes(FORM_END);
            //文件输入流
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.writeBytes(FORM_END);
            outputStream.writeBytes(FORM_LINE + FORM_BOUNDARY + FORM_END);
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            //获取服务器响应结果
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String read;
                while ((read = bufferedReader.readLine()) != null) {
                    response += read;
                }
                bufferedReader.close();
                inputStreamReader.close();
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}

package com.lanxin.jet;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JetFile {

    public final static String FILE_LOG     = "data.log";

    public String savePath;

    private static JetFile jetFile;

    public static JetFile getInstance(Context context) {
        if (jetFile == null) {
            jetFile = new JetFile(context);
        }
        return jetFile;
    }

    private JetFile(Context context) {
        String sdcard = "";
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            sdcard = Environment.getExternalStorageDirectory().toString();
        }
        if (! sdcard.equals("")) {
            savePath = sdcard + "/" + context.getPackageName() + "/";
        }
    }

    /**
     * 写入文件内容
     * @param fileName
     * @param content
     * @return
     */
    public boolean write(String fileName, String content) {
        boolean flag = false;
        File file = new File(savePath + fileName);
        try {
            if (! file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes("UTF-8"));
            outputStream.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 追加文件内容
     * @param fileName
     * @param content
     * @return
     */
    public boolean append(String fileName, String content) {
        boolean flag = false;
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(savePath + fileName, true);
            fileWriter.write(content);
            fileWriter.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 读取文件内容
     * @param fileName
     * @return
     */
    public String read(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(savePath + fileName);
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String block;
                while ((block = bufferedReader.readLine()) != null) {
                    stringBuilder.append(block);
                }
                bufferedReader.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
}
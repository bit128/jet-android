package lanxin.com.jet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class ActivityMain extends Activity {

    public static int REQ_PERM_CODE = 106;

    private JetResource jetResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jetResource = JetResource.getInstance(getApplicationContext());
        //请求存储权限
        if (jetResource.onlineSync
                && ContextCompat.checkSelfPermission(ActivityMain.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("需要您授予存储卡读写权限")
                    .setMessage("我们需要使用存储卡来保存页面相关数据")
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ActivityMain.this, new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, REQ_PERM_CODE);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            redirectHome();
        }
    }

    private void redirectHome() {
        if (jetResource.onlineSync) {
            JetVersion jetVersion = new JetVersion(jetResource);
            jetVersion.syncVersion();
        }
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Intent intent = new Intent(ActivityMain.this, ActivityTemp.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERM_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("-->", "获取存储权限成功");
                redirectHome();
            } else {
                Log.e("-->", "获取存储权限失败: "+permissions[0]);
                finish();
            }
        }
    }
}

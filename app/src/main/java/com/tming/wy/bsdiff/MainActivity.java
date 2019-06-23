package com.tming.wy.bsdiff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);

        findViewById(R.id.sample_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(BuildConfig.VERSION_NAME);
    }

    @SuppressLint("StaticFieldLeak")
    public void update() {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                // 获取旧版本路径（正在运行的apk路径）
//                String oldApk = getApplicationInfo().sourceDir;
                String patch = new File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath();
                String oldApk = new File(Environment.getExternalStorageDirectory(), "old.apk").getAbsolutePath();
                String output = createNewApk().getAbsolutePath();
                Log.e("oldApk---->>", oldApk);
                Log.e("patch----->>", patch);
                Log.e("output---->>", output);
                if (!new File(patch).exists()) {
                    return null;
                }
                BsPatcher.bsPatch(oldApk, patch, output);
                return new File(output);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                // 已经合成了，调用该方法，安装新版本apk
                if (file != null) {
                    if (!file.exists()) return;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationInfo().packageName + ".fileprovider", file);
                        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    }
                    MainActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "差分包不存在！", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /**
     * 创建合成后的新版本apk文件
     *
     * @return
     */
    private File createNewApk() {
        File newApk = new File(Environment.getExternalStorageDirectory(), "bsdiff.apk");
        if (!newApk.exists()) {
            try {
                newApk.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newApk;
    }

    private static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

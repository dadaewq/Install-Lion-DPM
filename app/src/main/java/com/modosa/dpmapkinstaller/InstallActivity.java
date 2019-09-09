package com.modosa.dpmapkinstaller;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.modosa.dpmapkinstaller.utils.PackageInstallerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallActivity extends Activity {
    private final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private boolean istemp = false;

    private Uri uri;
    private boolean needrequest;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String pkgInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        uri = intent.getData();

        needrequest = (Build.VERSION.SDK_INT >= 23) && ((uri + "").contains("file://"));

        init();

    }


    private void init() {
        String apkPath;
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean needconfirm = sharedPreferences.getBoolean("needconfirm", true);

        apkPath = preInstall();
        pkgInfo = getApkInfo(apkPath);
        if (needconfirm) {
            Context context = new ContextThemeWrapper(InstallActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            CharSequence[] items = new CharSequence[]{getString(R.string.items)};
            boolean[] checkedItems = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.dialog_title) + " " + pkgInfo);


            builder.setMultiChoiceItems(items, checkedItems, (dialogInterface, i, b) -> {
                if (b) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("needconfirm", false);
                    editor.apply();
                } else {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("needconfirm", true);
                    editor.apply();
                }
            });
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                startInstall(apkPath);
                finish();
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> finish());
            builder.setCancelable(false);
            builder.show();


        } else {
            startInstall(apkPath);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needrequest) {
            confirmPermission();
        }
    }

    private String preInstall() {
        String apkPath = null;
        if (uri != null) {
            Log.e("--getData--", uri + "");
            String CONTENT = "content://";
            String FILE = "file://";
            if (uri.toString().contains(FILE)) {
                confirmPermission();
                apkPath = uri.getPath();
            } else if (uri.toString().contains(CONTENT)) {
                apkPath = createApkFromUri(this);
            }
            return apkPath;
        } else {
            finish();
            return "";
        }
    }

    private void startInstall(String apkPath) {
        Log.d("Start install", apkPath + "");
        if (apkPath != null) {
            final File apkFile = new File(apkPath);


            new Thread(() -> {
                showToast(getString(R.string.start_install) + apkFile.getPath());
                try {
                    if (PackageInstallerUtil.installPackage(this, Uri.fromFile(apkFile), null)) {
                        showToast(pkgInfo + " " + getString(R.string.success_install));
                    } else {
                        showToast(pkgInfo + " " + getString(R.string.failed_install));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (istemp) {
                    deleteSingleFile(apkFile);
                }
                finish();
            }).start();
        } else {
            showToast(getString(R.string.failed_read));
            finish();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0x233);
    }

    private void confirmPermission() {
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean judge = (permissionRead == 0);
        if (!judge) {
            requestPermission();
        }
    }


    private String getApkInfo(String apkSourcePath) {
        if (apkSourcePath == null) {
            return "";
        } else {
            PackageManager pm = getPackageManager();
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkSourcePath, PackageManager.GET_ACTIVITIES);
            System.out.println(pkgInfo);
            if (pkgInfo != null) {
                pkgInfo.applicationInfo.sourceDir = apkSourcePath;
                pkgInfo.applicationInfo.publicSourceDir = apkSourcePath;
                return pm.getApplicationLabel(pkgInfo.applicationInfo).toString() + "_" + pkgInfo.versionName + "(" + pkgInfo.versionCode + ")";
            }
            return "";
        }
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    private String createApkFromUri(Context context) {
        istemp = true;
        File tempFile = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".apk");
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                OutputStream fos = new FileOutputStream(tempFile);
                byte[] buf = new byte[4096 * 1024];
                int ret;
                while ((ret = is.read(buf)) != -1) {
                    fos.write(buf, 0, ret);
                    fos.flush();
                }
                fos.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile.getAbsolutePath();
    }

    private void deleteSingleFile(File file) {
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--DELETE--", "deleteSingleFile" + file.getAbsolutePath() + " OK！");
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

}

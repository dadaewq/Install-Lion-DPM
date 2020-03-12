package com.modosa.dpmapkinstaller.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.util.PackageInstallerUtil;

import java.io.File;

/**
 * @author dadaewq
 */
public class InstallActivity extends AbstractInstallActivity {

    private final Context context = this;
    private String realPath;

    @SuppressLint("PrivateApi")
    private static String getSystemProperty() {
        try {
            return (String) Class.forName("android.os.SystemProperties")
                    .getDeclaredMethod("get", String.class)
                    .invoke(null, "ro.miui.ui.version.name");
        } catch (Exception e) {
            Log.w("CheckMiui", "Unable to use SystemProperties.get", e);
            return null;
        }
    }

    private static boolean isMiui() {
        return !TextUtils.isEmpty(getSystemProperty());
    }

    @Override
    public void startInstall(String apkPath) {
        Log.d("Start install", apkPath + "");
        if (apkPath != null) {
            realPath = apkPath;
            apkFile = new File(apkPath);
            new InstallApkTask().start();
        } else {
            showToast0(R.string.tip_failed_read);
        }
        finish();
    }

    @Override
    protected void startUninstall(String pkgName) {
        Log.d("Start uninstall", pkgName);
        new UninstallApkTask().start();
    }

    private class InstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            showToast0(String.format(getString(R.string.tip_start_install), apkinfo[0]));
            try {
                String result = PackageInstallerUtil.installPackage(context, Uri.fromFile(apkFile), null);
                if (result == null) {
                    showToast0(String.format(getString(R.string.tip_success_install), apkinfo[0]));
                } else {
                    String err = String.format("%s: %s %s | %s | Android %s \n", getString(R.string.installer_device), Build.BRAND, Build.MODEL, isMiui() ? "MIUI" : " ", Build.VERSION.RELEASE) +
                            String.format(alertDialogMessage + "\n%s", result);
                    copyErr(err);
                    showToast1(String.format(getString(R.string.tip_failed_install_witherror), apkinfo[0], result));
                }
                if (show_notification) {
                    Log.e("packagename", apkinfo[1]);
                    Intent intent = new Intent()
                            .setComponent(new ComponentName(context, NotifyActivity.class))
                            .putExtra("packageName", apkinfo[1])
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (result == null) {
                        intent.putExtra("channelId", "1")
                                .putExtra("channelName", getString(R.string.app_name))
                                .putExtra("contentTitle", String.format(getString(R.string.tip_success_install), apkinfo[0]));
                    } else {
                        intent.putExtra("channelId", "4")
                                .putExtra("channelName", getString(R.string.channalname_fail))
                                .putExtra("realPath", realPath)
                                .putExtra("contentTitle", String.format(getString(R.string.tip_failed_install), apkinfo[0]));
                    }
                    startActivity(intent);
                }
                deleteCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class UninstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            Log.d("Start uninstall", pkgName);

            try {
                String result = PackageInstallerUtil.uninstallPackage(context, pkgName);
                if (result == null) {
                    showToast0(String.format(getString(R.string.tip_success_uninstall), packageLable));
                } else {
                    copyErr(String.format("%s: %s %s | %s | Android %s \n\n%s\n%s", getString(R.string.installer_device), Build.BRAND, Build.MODEL, isMiui() ? "MIUI" : " ", Build.VERSION.RELEASE, alertDialogMessage, result));
                    showToast1(String.format(getString(R.string.tip_failed_uninstall_witherror), packageLable, result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

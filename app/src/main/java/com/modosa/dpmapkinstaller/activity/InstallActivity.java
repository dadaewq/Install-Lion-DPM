package com.modosa.dpmapkinstaller.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.utils.PackageInstallerUtil;

import java.io.File;

public class InstallActivity extends AbstractInstallActivity {

    @SuppressLint("PrivateApi")
    private static String getSystemProperty() {
        try {
            return (String) Class.forName("android.os.SystemProperties")
                    .getDeclaredMethod("get", String.class)
                    .invoke(null, "ro.miui.ui.version.name");
        } catch (Exception e) {
            Log.w("SAIUtils", "Unable to use SystemProperties.get", e);
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
            apkFile = new File(apkPath);
            new Thread(() -> {
                Looper.prepare();
                showToast0(String.format(getString(R.string.start_install), apkinfo[0]));
                try {
                    String result = PackageInstallerUtil.installPackage(this, Uri.fromFile(apkFile), null);
                    if (result == null) {
                        deleteCache();
                        showToast0(String.format(getString(R.string.success_install), apkinfo[0]));
                    } else {
                        deleteCache();
                        String err = String.format("%s: %s %s | %s | Android %s \n", getString(R.string.installer_device), Build.BRAND, Build.MODEL, isMiui() ? "MIUI" : "Not MIUI", Build.VERSION.RELEASE) +
                                String.format(alertDialogMessage + "\n%s", result);
                        copyErr(err);
                        showToast1(String.format(getString(R.string.failed_install), apkinfo[0], result));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
            ).start();
            finish();
        } else {
            showToast0(getString(R.string.failed_read));
            finish();
        }
    }

    @Override
    protected void startUninstall(String pkgName) {
        Log.d("Start uninstall", pkgName);
        new Thread(() -> {
            Looper.prepare();
            try {
                String result = PackageInstallerUtil.uninstallPackage(this, pkgName);
                if (result == null) {
                    showToast0(String.format(getString(R.string.success_uninstall), packageLable));
                } else {
                    copyErr(String.format("%s: %s %s | %s | Android %s \n\n%s\n%s", getString(R.string.installer_device), Build.BRAND, Build.MODEL, isMiui() ? "MIUI" : "Not MIUI", Build.VERSION.RELEASE, alertDialogMessage, result));
                    showToast1(String.format(getString(R.string.failed_uninstall), packageLable, result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Looper.loop();
        }
        ).start();
        finish();
    }

}

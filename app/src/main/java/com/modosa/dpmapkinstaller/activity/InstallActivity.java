package com.modosa.dpmapkinstaller.activity;


import android.net.Uri;
import android.util.Log;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.utils.PackageInstallerUtil;

import java.io.File;


public class InstallActivity extends AbstractInstallActivity {
    private File apkFile;

    @Override
    public void startInstall(String apkPath) {
        Log.d("Start install", apkPath + "");
        if (apkPath != null) {
            apkFile = new File(apkPath);

            new Thread(() -> {
                showToast0(String.format(getString(R.string.start_install), apkinfo[0]));
                try {
                    if (PackageInstallerUtil.installPackage(this, Uri.fromFile(apkFile), null)) {
                        deleteCache();
                        showToast0(String.format(getString(R.string.success_install), apkinfo[0]));
                    } else {
                        deleteCache();
                        showToast1(apkinfo[1] + " " + getString(R.string.failed_install));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ).start();
        } else {
            showToast0(getString(R.string.failed_read));
            finish();
        }
    }


    private void deleteCache() {
        if (istemp) {
            deleteSingleFile(apkFile);
        }
    }

    @Override
    protected void startUninstall(String pkgname) {
        Log.d("Start uninstall", pkgname);
        new Thread(() -> {
            try {
                if (PackageInstallerUtil.uninstallPackage(this, pkgname)) {
                    showToast0(String.format(getString(R.string.success_uninstall), pkgLable));
                } else {
                    showToast1(String.format(getString(R.string.failed_uninstall), pkgLable));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ).start();
    }

}

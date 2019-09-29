package com.modosa.dpmapkinstaller;


import android.net.Uri;
import android.util.Log;

import com.modosa.dpmapkinstaller.utils.PackageInstallerUtil;

import java.io.File;
import java.io.IOException;

public class InstallActivity extends AbstractInstallActivity {

    @Override
    public void startInstall(String apkPath) {
        Log.d("Start install", apkPath + "");
        if (apkPath != null) {
            final File apkFile = new File(apkPath);
            apkinfo = getApkPkgInfo(apkPath);

            new Thread(() -> {
                showToast(getString(R.string.start_install) + apkinfo[1]);
                try {
                    if (PackageInstallerUtil.installPackage(this, Uri.fromFile(apkFile), null)) {
                        showToast(apkinfo[1] + " " + getString(R.string.success_install));
                    } else {
                        showToast(apkinfo[1] + " " + getString(R.string.failed_install));
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

}

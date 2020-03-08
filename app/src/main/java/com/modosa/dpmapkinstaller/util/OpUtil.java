package com.modosa.dpmapkinstaller.util;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.receiver.AdminReceiver;

/**
 * @author dadaewq
 */
public class OpUtil {
    public static String getCommand(Context context) {
        ComponentName componentName = AdminReceiver.getComponentName(context);
        StringBuilder stringBuilder;

        String cPackageName = componentName.getPackageName();
        String cClassName = componentName.getClassName();

        if (cClassName.startsWith(cPackageName)) {
            stringBuilder = new StringBuilder(cClassName);
            stringBuilder.insert(cPackageName.length(), "/");
        } else {
            stringBuilder = new StringBuilder(componentName.flattenToString());
        }
        return "adb shell dpm set-device-owner " + stringBuilder.toString();
    }

    public static void showAlertDialog(Context context, AlertDialog alertDialog) {
        Window window = alertDialog.getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                window.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.Background, null)));
            } else {
                window.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.Background)));
            }
        }
        alertDialog.show();
    }

}

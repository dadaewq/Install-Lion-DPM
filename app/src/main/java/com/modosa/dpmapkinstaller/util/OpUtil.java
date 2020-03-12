package com.modosa.dpmapkinstaller.util;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;
import android.widget.Toast;

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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                window.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.Background, null)));
            } else {

                window.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.Background)));
            }
        }
        alertDialog.show();
    }


    public static boolean getComponentState(Context context, ComponentName componentName) {
        PackageManager pm = context.getPackageManager();
        return (pm.getComponentEnabledSetting(componentName) == (PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) || pm.getComponentEnabledSetting(componentName) == (PackageManager.COMPONENT_ENABLED_STATE_ENABLED));
    }

    public static void setComponentState(Context context, ComponentName componentName, boolean isenable) {
        PackageManager pm = context.getPackageManager();
        int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (isenable) {
            flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }

        pm.setComponentEnabledSetting(componentName, flag, PackageManager.DONT_KILL_APP);
    }


    public static void showToast0(Context context, final String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast0(Context context, final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast1(Context context, final String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast1(Context context, final int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }


}

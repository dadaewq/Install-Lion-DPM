package com.modosa.dpmapkinstaller.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.modosa.dpmapkinstaller.R;


public class AdminReceiver extends android.app.admin.DeviceAdminReceiver {
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), AdminReceiver.class);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.e("onEnabled", intent + "");
        Toast.makeText(context, context.getString(R.string.start_activating), Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.e("onDisableRequested", intent + "");
        return "取消吗";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.e("onDisabled", intent + "");
    }
}

package com.modosa.dpmapkinstaller.fragment;

import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.util.OpUtil;

import java.util.Objects;

/**
 * @author dadaewq
 */
public class SettingsFragment extends PreferenceFragment {
    private Context context;
    private Preference show_deviceowner_state;
    private DevicePolicyManager devicePolicyManager;
    private String command;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        addPreferencesFromResource(R.xml.pref_settings);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private boolean isDeviceOwner() {
        return devicePolicyManager.isDeviceOwnerApp(context.getPackageName());
    }

    private void init() {

        show_deviceowner_state = findPreference("show_deviceowner_state");

        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        refreshStatus();

    }

    private void refreshStatus() {

        if (isDeviceOwner()) {
            show_deviceowner_state.setTitle(R.string.title_is_deviceowner);
            show_deviceowner_state.setSummary(null);
            show_deviceowner_state.setOnPreferenceClickListener(v -> false);

        } else {
            command = OpUtil.getCommand(context);
            show_deviceowner_state.setTitle(R.string.title_not_deviceowner);
            show_deviceowner_state.setSummary(String.format(getString(R.string.summary_clicktocopycmd), command));
            show_deviceowner_state.setOnPreferenceClickListener(v -> {
                copyCommand();
                return false;
            });
        }

    }

    private void copyCommand() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, command);
        Objects.requireNonNull(clipboard).setPrimaryClip(clipData);
        Toast.makeText(context, command, Toast.LENGTH_SHORT).show();
    }


}

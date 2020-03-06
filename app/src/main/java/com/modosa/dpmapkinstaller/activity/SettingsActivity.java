package com.modosa.dpmapkinstaller.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.receiver.AdminReceiver;

import java.util.Objects;


/**
 * @author dadaewq
 */
public class SettingsActivity extends Activity {
    private final String[] userRestrictionsKeys = {UserManager.DISALLOW_INSTALL_APPS, UserManager.DISALLOW_UNINSTALL_APPS, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES};
    private boolean isSuccess = false;
    private String command;
    private DevicePolicyManager devicePolicyManager;
    private UserManager userManager;
    private ComponentName componentName;
    private SharedPreferences sharedPreferences;
    private String orgName;
    private TextView showOrgName;
    private long exitTime = 0;
    private Switch[] switches;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isDeviceOwner()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isDeviceOwner() && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            menu.findItem(R.id.RebootDevice).setVisible(false);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.UserRestrictions:
                showDialogUserRestriction();
                break;
            case R.id.RebootDevice:
                devicePolicyManager.reboot(componentName);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long intervals = 2000;
            if ((System.currentTimeMillis() - exitTime) > intervals) {
                Toast.makeText(getApplicationContext(), getString(R.string.exit_tip), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init() {
        componentName = AdminReceiver.getComponentName(this);
        StringBuilder stringBuilder;

        String cPackageName = componentName.getPackageName();
        String cClassName = componentName.getClassName();

        if (cClassName.startsWith(cPackageName)) {
            stringBuilder = new StringBuilder(cClassName);
            stringBuilder.insert(cPackageName.length(), "/");
        } else {
            stringBuilder = new StringBuilder(componentName.flattenToString());
        }

        command = "adb shell dpm set-device-owner " + stringBuilder.toString();

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        userManager = (UserManager) getSystemService(USER_SERVICE);
        TextView status = findViewById(R.id.status);
        TextView cmd = findViewById(R.id.cmd);
        showOrgName = findViewById(R.id.showOrgName);
        Button releaseDeviceOwner = findViewById(R.id.releasedpm);
        ScrollView scrollView = findViewById(R.id.Extrafunction);
        switches = new Switch[userRestrictionsKeys.length];
        LinearLayout[] linearLayouts = new LinearLayout[userRestrictionsKeys.length];
        switches[0] = findViewById(R.id.switch0);
        switches[1] = findViewById(R.id.switch1);
        switches[2] = findViewById(R.id.switch2);
        linearLayouts[0] = findViewById(R.id.linearLayout0);
        linearLayouts[1] = findViewById(R.id.linearLayout1);
        linearLayouts[2] = findViewById(R.id.linearLayout2);

        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        if (isDeviceOwner()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                devicePolicyManager.setOrganizationName(componentName, sharedPreferences.getString("orgName", ""));
                showOrgName.setVisibility(View.VISIBLE);
                showOrgName.setOnClickListener(view -> editOrgName());
                orgName = getString(R.string.show_OrgName) + ":" + sharedPreferences.getString("orgName", "");
                showOrgName.setText(orgName);

            }
            status.setText(getString(R.string.isDeviceOwner));

            cmd.setVisibility(View.GONE);
            releaseDeviceOwner.setVisibility(View.VISIBLE);
            releaseDeviceOwner.setOnClickListener(view -> releaseDeviceOwner());
            scrollView.setVisibility(View.VISIBLE);

            if (userManager.getUserRestrictions().isEmpty()) {
                Log.e("UserRestrictions", "isEmpty ");
            } else {
                int i = 0;
                for (String key : userManager.getUserRestrictions().keySet()) {
                    i++;
                    Log.e("Bundle " + i, key + "");
                }
            }

            refreshSwitch();
            for (int i = 0; i < userRestrictionsKeys.length; i++) {
                int finalI = i;

                linearLayouts[i].setOnClickListener(v -> {
                    boolean is = switches[finalI].isChecked();
                    if (is) {
                        switches[finalI].setChecked(false);
                    } else {
                        switches[finalI].setChecked(true);
                    }
                });

                switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (switches[finalI].isChecked()) {
                        addUserRestrictions(userRestrictionsKeys[finalI]);
                    } else {
                        clearUserRestrictions(userRestrictionsKeys[finalI]);
                    }
                });
            }

        } else {
            if (userManager.getUserRestrictions().isEmpty()) {
                Log.e("UserRestrictions", "isEmpty ");
            } else {
                int i = 0;
                for (String key : userManager.getUserRestrictions().keySet()) {
                    i++;
                    Log.e("Bundle " + i, key + "");
                }
            }

            status.setText(getString(R.string.notDeviceOwner));
            cmd.setOnClickListener(view -> copyCommand());
            cmd.setText(command);

        }
    }

    private void addUserRestrictions(String key) {
        devicePolicyManager.addUserRestriction(componentName, key);
    }

    private void clearUserRestrictions(String key) {
        devicePolicyManager.clearUserRestriction(componentName, key);
    }

    private boolean checkUserRestriction(String key) {
        return userManager.hasUserRestriction(key);
    }

    private void showToast0(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    private void showToast0(final int stringId) {
        runOnUiThread(() -> Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show());
    }

    private void copyCommand() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, command);
        Objects.requireNonNull(clipboard).setPrimaryClip(clipData);
        Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT).show();
    }

    private boolean isDeviceOwner() {
        return devicePolicyManager.isDeviceOwnerApp(getPackageName());
    }

    private void opUserRestriction(String key, int whichButton) {
        if ("".equals(key)) {
            showToast0(R.string.empty_tip);
        } else {
            isSuccess = false;
            if (whichButton == AlertDialog.BUTTON_POSITIVE) {
                addUserRestrictions(key);
                if (checkUserRestriction(key)) {
                    isSuccess = true;
                }

            } else if (whichButton == AlertDialog.BUTTON_NEGATIVE) {
                clearUserRestrictions(key);
                if (!checkUserRestriction(key)) {
                    isSuccess = true;
                }
            }
            Log.e(key, "" + checkUserRestriction(key));

            showToast0(isSuccess ? "Success" : "Fail");
        }
    }

    private void showDialogUserRestriction() {

        final EditText keyOfRestriction = new EditText(this);
        keyOfRestriction.setHint(R.string.restrictionkey_tip);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.UserRestrictions)
                .setView(keyOfRestriction)
                .setNeutralButton(R.string.close, null)
                .setNegativeButton(R.string.clear, null)
                .setPositiveButton(R.string.add, null)
                .show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> opUserRestriction(keyOfRestriction.getText().toString().trim(), AlertDialog.BUTTON_POSITIVE));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> opUserRestriction(keyOfRestriction.getText().toString().trim(), AlertDialog.BUTTON_NEGATIVE));

        alertDialog.setOnDismissListener(dialog -> {
            if (isSuccess) {
                isSuccess = false;
                refreshSwitch();
            }
        });
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.Background)));
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void editOrgName() {
        String getOrgName = sharedPreferences.getString("orgName", "");

        final EditText inputname = new EditText(this);
        inputname.setText(getOrgName);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.show_OrgName)
                .setView(inputname)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("orgName", inputname.getText() + "");
                    editor.apply();
                    devicePolicyManager.setOrganizationName(componentName, sharedPreferences.getString("orgName", ""));
                    orgName = getString(R.string.show_OrgName) + ":" + sharedPreferences.getString("orgName", "");
                    showOrgName.setText(orgName);
                });
        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.Background)));
        }
        alertDialog.show();
    }

    private void releaseDeviceOwner() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.release_DPM)
                .setMessage(R.string.release_tip)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (clearDeviceOwner()) {
                        showToast0(R.string.success_deactivate);
                        finish();
                    } else {
                        showToast0(R.string.failed_deactivate);
                    }
                });
        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.Background)));
        }
        alertDialog.show();

    }

    private void refreshSwitch() {
        for (int i = 0; i < userRestrictionsKeys.length; i++) {
            if (checkUserRestriction(userRestrictionsKeys[i])) {
                switches[i].setChecked(true);
            } else {
                switches[i].setChecked(false);
            }
        }
    }

    private boolean clearDeviceOwner() {
        if (isDeviceOwner()) {
            devicePolicyManager.clearDeviceOwnerApp(getPackageName());
            return !isDeviceOwner();
        } else {
            return false;
        }
    }

}

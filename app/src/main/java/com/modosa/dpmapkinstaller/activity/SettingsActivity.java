package com.modosa.dpmapkinstaller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.modosa.dpmapkinstaller.util.OpUtil;

import java.util.List;
import java.util.Objects;


/**
 * @author dadaewq
 */
public class SettingsActivity extends Activity {
    private final String sp_orgName = "orgName";
    private final String sp_confirmWarning = "confirmWarning";
    private final String[] userRestrictionsKeys = {UserManager.DISALLOW_INSTALL_APPS, UserManager.DISALLOW_UNINSTALL_APPS, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES};
    private boolean isSuccess = false;
    private String command;
    private DevicePolicyManager devicePolicyManager;
    private UserManager userManager;
    private ComponentName adminComponentName;
    private SharedPreferences sharedPreferences;
    private long exitTime = 0;
    private Switch[] switches;
    private ComponentName[] allComponentName;
    private CharSequence[] appInfo;
    private ComponentName defaultApp;

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
        if (isDeviceOwner()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                menu.findItem(R.id.RebootDevice).setVisible(true);
                menu.findItem(R.id.SetLockScreenInfo).setVisible(true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                menu.findItem(R.id.SetOrganizationName).setVisible(true);
            }
        }


        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.HideIcon:
                showDialogHideIcon();
                break;
            case R.id.ClearAllowedList:
                showDialogClearAllowedList();
                break;
            case R.id.SetOrganizationName:
                showDialogSetOrganizationName();
                break;
            case R.id.SetLockScreenInfo:
                showDialogSetLockScreenInfo();
                break;
            case R.id.LockDefaultLauncher:
            case R.id.LockDefaultPackageInstaller:
                if (sharedPreferences.getBoolean(sp_confirmWarning, false)) {
                    showDialogLockDefaultApplication(item.getItemId());
                } else {
                    showWarningLockDefaultApplication(item.getItemId());
                }
                break;
            case R.id.UserRestrictions:
                showDialogUserRestriction();
                break;
            case R.id.RebootDevice:
                devicePolicyManager.reboot(adminComponentName);
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
                Toast.makeText(this, getString(R.string.tip_exit), Toast.LENGTH_SHORT).show();
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
        adminComponentName = AdminReceiver.getComponentName(this);

        command = OpUtil.getCommand(this);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        userManager = (UserManager) getSystemService(USER_SERVICE);
        TextView status = findViewById(R.id.status);
        TextView cmd = findViewById(R.id.cmd);
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

//        int i = 0;
//        for (String key : userManager.getUserRestrictions().keySet()) {
//            i++;
//            Log.e("Bundle " + i, key + "");
//        }

        if (isDeviceOwner()) {
            status.setText(getString(R.string.tv_is_deviceowner));

            cmd.setVisibility(View.GONE);
            releaseDeviceOwner.setVisibility(View.VISIBLE);
            releaseDeviceOwner.setOnClickListener(view -> showDialogDeactivateDeviceOwner());
            scrollView.setVisibility(View.VISIBLE);

            refreshSwitch();
            int i;
            for (i = 0; i < userRestrictionsKeys.length; i++) {
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

            status.setText(getString(R.string.tv_not_deviceowner));
            cmd.setOnClickListener(view -> copyCommand());
            cmd.setText(command);
        }
    }

    private void addUserRestrictions(String key) {
        devicePolicyManager.addUserRestriction(adminComponentName, key);
    }

    private void clearUserRestrictions(String key) {
        devicePolicyManager.clearUserRestriction(adminComponentName, key);
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

    private void changeState(ComponentName c, boolean enable) {
        PackageManager pm = this.getPackageManager();

        int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (enable) {
            flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

        }
        pm.setComponentEnabledSetting(c,
                flag,
                PackageManager.DONT_KILL_APP);

    }

    private void opUserRestriction(String key, int whichButton) {
        if ("".equals(key)) {
            showToast0(R.string.tip_empty);
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


    private ComponentName getDefaultApplication(Intent intent) {

        final ResolveInfo res = getPackageManager().resolveActivity(intent, 0);

        if (res != null && res.activityInfo != null && !"android".equals(res.activityInfo.packageName)) {
            return new ComponentName(res.activityInfo.packageName, res.activityInfo.name);
        }
        return null;
    }

    private void queryIntentActivities(Intent intent) {

        defaultApp = getDefaultApplication(intent);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
        int size = list.size();
        allComponentName = new ComponentName[size];
        appInfo = new CharSequence[size];


        int i;
        for (i = 0; i < size; i++) {
            String packageName = list.get(i).activityInfo.packageName;
            String activityName = list.get(i).activityInfo.name;
            allComponentName[i] = new ComponentName(packageName, activityName);
            appInfo[i] = list.get(i).activityInfo.loadLabel(packageManager) + "(" + packageName + ")";

            if (allComponentName[i].equals(defaultApp)) {
                appInfo[i] = new StringBuilder("* ").append(appInfo[i]);
            }
        }
    }

    private void showDialogHideIcon() {

        View checkBoxView = View.inflate(this, R.layout.confirm_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.confirm_checkbox);
        checkBox.setText(R.string.HideIcon);

        ComponentName mainComponentName = new ComponentName(this, "com.modosa.dpmapkinstaller.activity.MainActivity");
        PackageManager pm = getPackageManager();
        boolean isEnabled = (pm.getComponentEnabledSetting(mainComponentName) == (PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) || pm.getComponentEnabledSetting(mainComponentName) == (PackageManager.COMPONENT_ENABLED_STATE_ENABLED));

        checkBox.setChecked(!isEnabled);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.HideIcon)
                .setMessage(R.string.message_HideIcon)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> changeState(mainComponentName,
                        !checkBox.isChecked()));

        AlertDialog alertDialog = builder.create();


        OpUtil.showAlertDialog(this, alertDialog);

    }

    private void showDialogClearAllowedList() {

        View checkBoxView = View.inflate(this, R.layout.confirm_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.confirm_checkbox);
        checkBox.setText(R.string.checkbox_ClearAllowedList);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.ClearAllowedList)
                .setMessage(R.string.message_ClearAllowedList)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences("allowsource", Context.MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                });

        AlertDialog alertDialog = builder.create();


        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isChecked));

        OpUtil.showAlertDialog(this, alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDialogSetOrganizationName() {
        String getOrgName = sharedPreferences.getString(sp_orgName, "");
        final EditText editOraName = new EditText(this);
        editOraName.setText(getOrgName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.title_SetOrganizationName)
                .setView(editOraName)
                .setNeutralButton(R.string.close, null)
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(sp_orgName, null);
                    editor.apply();
                    devicePolicyManager.setOrganizationName(adminComponentName, null);
                })
                .setPositiveButton(R.string.set, (dialog, which) -> {
                    String getEditName = editOraName.getText().toString() + "";
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(sp_orgName, getEditName);
                    editor.apply();
                    Toast.makeText(this, getEditName, Toast.LENGTH_SHORT).show();
                    devicePolicyManager.setOrganizationName(adminComponentName, getEditName);
                });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnDismissListener(dialog -> {
            if (isSuccess) {
                isSuccess = false;
                refreshSwitch();
            }
        });

        OpUtil.showAlertDialog(this, alertDialog);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDialogSetLockScreenInfo() {

        final EditText editInfo = new EditText(this);
        CharSequence getLockScreenInfo = devicePolicyManager.getDeviceOwnerLockScreenInfo();
        if (getLockScreenInfo == null) {
            editInfo.setHint("");
        } else {
            editInfo.setText(getLockScreenInfo);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.title_SetLockScreenInfo)
                .setView(editInfo)
                .setNeutralButton(R.string.close, null)
                .setNegativeButton(R.string.clear, (dialog, which) -> devicePolicyManager.setDeviceOwnerLockScreenInfo(adminComponentName, null))
                .setPositiveButton(R.string.set, (dialog, which) -> devicePolicyManager.setDeviceOwnerLockScreenInfo(adminComponentName, editInfo.getText().toString()));

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnDismissListener(dialog -> {
            if (isSuccess) {
                isSuccess = false;
                refreshSwitch();
            }
        });

        OpUtil.showAlertDialog(this, alertDialog);

    }

    private void showWarningLockDefaultApplication(int lockitemid) {
        View checkBoxView = View.inflate(this, R.layout.confirm_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.confirm_checkbox);
        checkBox.setText(R.string.checkbox_lockdefault);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.title_lockdefault)
                .setMessage(R.string.message_lockdefault)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(sp_confirmWarning, true);
                    editor.apply();
                    showDialogLockDefaultApplication(lockitemid);
                });

        AlertDialog alertDialog = builder.create();


        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isChecked));

        OpUtil.showAlertDialog(this, alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

    }

    private void showDialogLockDefaultApplication(int itemid) {
        Intent intent;
        IntentFilter filter;
        int titleid;
        switch (itemid) {
            case R.id.LockDefaultLauncher:
                intent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .addCategory(Intent.CATEGORY_DEFAULT);

                filter = new IntentFilter(Intent.ACTION_MAIN);
                filter.addCategory(Intent.CATEGORY_HOME);
                filter.addCategory(Intent.CATEGORY_DEFAULT);

                titleid = R.string.LockDefaultLauncher;
                break;
            case R.id.LockDefaultPackageInstaller:
                intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse("content://test/0/test.apk"), "application/vnd.android.package-archive");

                filter = new IntentFilter(Intent.ACTION_VIEW);
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                filter.addDataScheme(ContentResolver.SCHEME_CONTENT);
                try {
                    filter.addDataType("application/vnd.android.package-archive");
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    e.printStackTrace();
                }

                titleid = R.string.LockDefaultLauncher;
                break;

            default:
                return;
        }
        queryIntentActivities(intent);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(titleid)
                .setItems(appInfo, (dialog, which) -> {
                    if (defaultApp != null) {
                        devicePolicyManager.clearPackagePersistentPreferredActivities(adminComponentName, defaultApp.getPackageName());
                    }
                    devicePolicyManager.addPersistentPreferredActivity(adminComponentName, filter, allComponentName[which]);
                    showToast0(appInfo[which].toString());
                })
                .setNeutralButton(R.string.close, null)
                .setPositiveButton(R.string.clear_lock, (dialog, which) -> {
                    if (defaultApp != null) {
                        devicePolicyManager.clearPackagePersistentPreferredActivities(adminComponentName, defaultApp.getPackageName());
                    }
                });

        AlertDialog alertDialog = builder.create();
        OpUtil.showAlertDialog(this, alertDialog);
    }

    private void showDialogUserRestriction() {

        final EditText keyOfRestriction = new EditText(this);
        keyOfRestriction.setHint(R.string.hint_restrictionkey);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.UserRestrictions)
                .setView(keyOfRestriction)
                .setNeutralButton(R.string.close, null)
                .setNegativeButton(R.string.clear, null)
                .setPositiveButton(R.string.add, null);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnDismissListener(dialog -> {
            if (isSuccess) {
                isSuccess = false;
                refreshSwitch();
            }
        });

        OpUtil.showAlertDialog(this, alertDialog);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> opUserRestriction(keyOfRestriction.getText().toString().trim(), AlertDialog.BUTTON_POSITIVE));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> opUserRestriction(keyOfRestriction.getText().toString().trim(), AlertDialog.BUTTON_NEGATIVE));

    }


    private void showDialogDeactivateDeviceOwner() {

        View checkBoxView = View.inflate(this, R.layout.confirm_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.confirm_checkbox);
        checkBox.setText(R.string.checkbox_deactivate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_deactivate)
                .setMessage(R.string.message_deactivate)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (clearDeviceOwner()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(sp_confirmWarning, false);
                        editor.apply();
                        showToast0(R.string.tip_success_deactivate);
                        finish();
                    } else {
                        showToast0(R.string.tip_failed_deactivate);
                    }
                });

        AlertDialog alertDialog = builder.create();
        OpUtil.showAlertDialog(this, alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isChecked));
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

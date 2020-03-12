package com.modosa.dpmapkinstaller.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;

import com.modosa.dpmapkinstaller.R;
import com.modosa.dpmapkinstaller.fragment.OtherSettingsFragment;
import com.modosa.dpmapkinstaller.fragment.SettingsFragment;
import com.modosa.dpmapkinstaller.receiver.AdminReceiver;
import com.modosa.dpmapkinstaller.util.OpUtil;


/**
 * @author dadaewq
 */
public class SettingsActivity extends Activity implements OtherSettingsFragment.MyListener {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponentName;
    private long exitTime = 0;
    private boolean isMain = true;
    private FragmentManager fragmentManager;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        fragmentManager = getFragmentManager();

        Fragment fragmentMain = fragmentManager.findFragmentByTag("main");
        Fragment fragmentOther = fragmentManager.findFragmentByTag("other");

        if (fragmentMain == null && fragmentOther == null) {
            fragmentManager.beginTransaction().replace(R.id.framelayout, new SettingsFragment(), "main").commit();
        } else if (fragmentOther != null) {
            isMain = false;

            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(!isMain);
                actionBar.setTitle(null);
            }
        }


        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMain) {
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (isMain && isDeviceOwner() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            menu.findItem(R.id.RebootDevice).setVisible(true);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                swtichIsMainFragment(true);
                break;
            case R.id.HideIcon:
                showDialogHideIcon();
                break;
            case R.id.ClearAllowedList:
                showDialogClearAllowedList();
                break;
            case R.id.RebootDevice:
                devicePolicyManager.reboot(adminComponentName);
                break;
            case R.id.OtherSettings:
                if (isDeviceOwner()) {
                    swtichIsMainFragment(false);
                } else {
                    showToast0(R.string.title_not_deviceowner);
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void switchToMain() {
        swtichIsMainFragment(true);
    }

    @Override
    public void onBackPressed() {
        if (isMain) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - exitTime) < 2000) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, R.string.tip_exit, Toast.LENGTH_SHORT).show();
                exitTime = currentTime;
            }
        } else {
            swtichIsMainFragment(true);
        }
    }

    private void swtichIsMainFragment(boolean isMain) {

        this.isMain = isMain;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!isMain);
        }

        invalidateOptionsMenu();
        if (isMain) {
            if (actionBar != null) {
                actionBar.setTitle(R.string.app_name);
            }
            fragmentManager.beginTransaction().replace(R.id.framelayout, new SettingsFragment(), "main").commit();

        } else {
            if (actionBar != null) {
                actionBar.setTitle(null);
            }
            fragmentManager.beginTransaction().replace(R.id.framelayout, new OtherSettingsFragment(), "other").commit();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init() {
        adminComponentName = AdminReceiver.getComponentName(this);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
    }

    private void showToast0(final int stringId) {
        runOnUiThread(() -> Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show());
    }


    private boolean isDeviceOwner() {
        return devicePolicyManager.isDeviceOwnerApp(getPackageName());
    }


    private void showDialogHideIcon() {

        View checkBoxView = View.inflate(this, R.layout.confirm_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.confirm_checkbox);
        checkBox.setText(R.string.HideIcon);

        ComponentName mainComponentName = new ComponentName(this, "com.modosa.dpmapkinstaller.activity.MainActivity");

        boolean isEnabled = OpUtil.getComponentState(this, mainComponentName);

        checkBox.setChecked(!isEnabled);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.HideIcon)
                .setMessage(R.string.message_HideIcon)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> OpUtil.setComponentState(this, mainComponentName,
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

}

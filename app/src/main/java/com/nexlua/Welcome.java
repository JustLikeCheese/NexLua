package com.nexlua;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Welcome extends Activity {
    public static String oldVersionName, newVersionName;
    public static long oldUpdateTime, newUpdateTime;
    public static boolean isVersionChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (LuaConfig.WELCOME_THEME != 0) setTheme(LuaConfig.WELCOME_THEME);
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText(new String(new char[]{'P', 'o', 'w', 'e', 'r', 'e', 'd', ' ', 'b', 'y', ' ', 'C', 'h', 'e', 'e', 's', 'e'}));
        view.setTextColor(0xff888888);
        view.setGravity(Gravity.TOP);
        setContentView(view);
        isVersionChanged = checkVersionChanged();
        if (isVersionChanged) {
            // 版本更新、触发权限申请
            final String[] REQUIRED_PERMISSIONS = LuaConfig.REQUIRED_PERMISSIONS;
            if (Build.VERSION.SDK_INT >= 23 && REQUIRED_PERMISSIONS != null) {
                ArrayList<String> permissions = new ArrayList<String>();
                for (String requiredPermission : REQUIRED_PERMISSIONS)
                    if (checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED)
                        permissions.add(requiredPermission);
                if (!permissions.isEmpty()) {
                    requestPermissions(permissions.toArray(new String[0]), 0);
                    return;
                }
            }
        }
        startActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startActivity();
    }

    public void startActivity() {
        Intent intent = new Intent(Welcome.this, Main.class);
        intent.putExtra(LuaContext.LUA_PATH, new File(getFilesDir(), LuaConfig.LUA_ENTRY).getPath());
        if (isVersionChanged) {
            intent.putExtra("isVersionChanged", true);
            intent.putExtra("newVersionName", newVersionName);
            intent.putExtra("oldVersionName", oldVersionName);
            AssetExtractor.extractAssets(this, new AssetExtractor.ExtractCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess() {
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(IOException e) {
                }
            });
            return;
        }
        startActivity(intent);
        finish();
    }

    public boolean checkVersionChanged() {
        try {
            // 获取包信息
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            long lastUpdateTime = packageInfo.lastUpdateTime;
            // 获取历史包信息
            SharedPreferences info = getSharedPreferences("appInfo", 0);
            String oldVersionName = info.getString("versionName", "");
            long oldUpdateTime = info.getLong("lastUpdateTime", 0);
            // 如果软件安装的时间与历史更新时间不同 / 版本名称不同, 触发更新
            if (oldUpdateTime == 0 || oldUpdateTime != lastUpdateTime || !versionName.equals(oldVersionName)) {
                SharedPreferences.Editor edit = info.edit();
                edit.putLong("lastUpdateTime", lastUpdateTime);
                edit.putString("versionName", versionName);
                edit.apply();
                Welcome.oldUpdateTime = lastUpdateTime;
                Welcome.newUpdateTime = lastUpdateTime;
                Welcome.newVersionName = versionName;
                Welcome.oldVersionName = oldVersionName;
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "package is null", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}

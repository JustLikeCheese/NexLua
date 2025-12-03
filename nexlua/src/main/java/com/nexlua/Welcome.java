package com.nexlua;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class Welcome extends LuaActivity {
    public static String oldVersionName, newVersionName;
    public static long oldUpdateTime, newUpdateTime;
    public static boolean isVersionChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        intent.putExtra(LuaIntent.NAME, new LuaIntent(config.welcome));
        super.onCreate(savedInstanceState);
        isVersionChanged = checkVersionChanged();
    }

    public void checkPermissions(String... requiredPermissions) {
        if (Build.VERSION.SDK_INT >= 23 && requiredPermissions != null) {
            ArrayList<String> permissions = new ArrayList<>();
            for (String requiredPermission : requiredPermissions)
                if (checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED)
                    permissions.add(requiredPermission);
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[0]), 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startActivity(new Intent(this, Main.class));
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
            if (oldUpdateTime == 0 || oldUpdateTime != lastUpdateTime || (versionName != null && !versionName.equals(oldVersionName))) {
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
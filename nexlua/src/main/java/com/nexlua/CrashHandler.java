package com.nexlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@SuppressLint("StaticFieldLeak")
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";
    private static final CrashHandler INSTANCE = new CrashHandler();

    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    private final Map<String, String> infos = new LinkedHashMap<>();
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        if (context == null) return;
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // 如果自定义处理失败，则调用默认Handler
            if (!handleException(throwable) && mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, throwable);
            }
        } catch (Exception e) {
            if (mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private boolean handleException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        collectDeviceInfo(mContext);
        saveCrashInfoToFile(throwable);
        return true;
    }

    public void collectDeviceInfo(Context context) {
        if (context == null) return;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            String versionName = packageInfo.versionName == null ? "null" : packageInfo.versionName;
            String versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = String.valueOf(packageInfo.getLongVersionCode());
            } else {
                versionCode = String.valueOf(packageInfo.versionCode);
            }
            infos.put("versionName", versionName);
            infos.put("versionCode", versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "An error occurred when collecting package info", e);
        }
        collectFields(Build.class);
        collectFields(Build.VERSION.class);
    }

    private void collectFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(null);
                String valueStr = (value == null) ? "null" : value.toString();
                infos.put(fieldName, valueStr);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred when collecting crash info for field: " + field.getName(), e);
            }
        }
    }

    private void saveCrashInfoToFile(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        while (cause != null) {
            pw.append("\nCaused by: ");
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        sb.append(sw);
        String crashContent = sb.toString();
        Log.e("crash", "Crash detected:\n" + crashContent);
        try {
            File crashDir = new File(mContext.getExternalFilesDir(null), "crash");
            crashDir.mkdirs();
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            File file = new File(crashDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(crashContent.getBytes());
            }
            Log.i(TAG, "Crash log saved to: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "An error occurred while writing crash file", e);
        }
    }
}
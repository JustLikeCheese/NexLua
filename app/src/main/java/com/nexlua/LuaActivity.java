package com.nexlua;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.Lua.LuaType;
import com.luajava.LuaException;
import com.luajava.LuaException.LuaError;
import com.luajava.luajit.LuaJit;
import com.luajava.value.LuaValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class LuaActivity extends Activity implements LuaBroadcastReceiver.OnReceiveListener, LuaContext {

    private final static String ARG = "arg";
    private final static String DATA = "data";
    private final static String NAME = "name";
    private static final ArrayList<String> prjCache = new ArrayList<String>();
    private String luaDir;
    private Handler handler;
    private TextView status;
    private String luaCpath;
    private LuaDexLoader mLuaDexLoader;
    private int mWidth;
    private int mHeight;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private Lua L;
    private String luaPath;
    private final StringBuilder toastbuilder = new StringBuilder();
    private Toast toast;
    private LinearLayout layout;
    private boolean isSetViewed;
    private long lastShow;
    private Menu optionsMenu;
    private LuaValue mOnKeyDown;
    private LuaValue mOnKeyUp;
    private LuaValue mOnKeyLongPress;
    private LuaValue mOnTouchEvent;
    private String localDir;

    private String odexDir;

    private String libDir;

    private String luaExtDir;

    private LuaBroadcastReceiver mReceiver;

    private String luaLpath;

    private String luaLibDir;

    private boolean isUpdata;

    private boolean mDebug = true;
    private LuaResources mResources;
    private Resources.Theme mTheme;
    private String luaFileName = "main";
    private static String sKey;
    private static final HashMap<String, LuaActivity> sLuaActivityMap = new HashMap<String, LuaActivity>();
    private LuaValue mOnKeyShortcut;

    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        // TODO: Implement this method
        return mLuaDexLoader.getClassLoaders();
    }

    public HashMap<String, String> getLibrarys() {
        return mLuaDexLoader.getLibrarys();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(null);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;

        layout = new LinearLayout(this);
        status = new TextView(this);
        status.setTextColor(Color.BLACK);
        status.setTextIsSelectable(true);
        list = new ListView(this);
        list.setFastScrollEnabled(true);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (convertView == null)
                    view.setTextIsSelectable(true);
                return view;
            }
        };
        list.setAdapter(adapter);
        layout.addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // 定义文件夹
        LuaApplication app = (LuaApplication) getApplication();
        localDir = app.getLuaDir();
        odexDir = app.getOdexDir();
        libDir = app.getLibDir();
        luaLibDir = app.getLuaLibDir();
        luaCpath = app.getLuaCpath();
        luaDir = localDir;
        luaLpath = app.getLuaLpath();
        luaExtDir = app.getLuaExtDir();

        handler = new MainHandler();


        try {
            Intent intent = getIntent();
            // 获取传入 main 函数的参数
            Object[] mainArgs = (Object[]) intent.getSerializableExtra(ARG);
            if (mainArgs == null) mainArgs = new Object[0];
            // 获取当前 Lua 脚本路径, 并截取文件名到 luaFileName
            luaPath = getLuaPath();
            luaFileName = new File(luaPath).getName();
            int idx = luaFileName.lastIndexOf(".");
            if (idx > 0) luaFileName = luaFileName.substring(0, idx);
            // 拼接 luaLpath
            luaLpath = (luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;") + luaLpath;
            initLua();

            mLuaDexLoader = new LuaDexLoader(this);
            mLuaDexLoader.loadLibs();
            //MultiDex.installLibs(this);
            sLuaActivityMap.put(luaFileName, this);
            doFile(luaPath, mainArgs);
            if (!luaFileName.equals("main"))
                runFunc("main", mainArgs);
            runFunc(luaFileName, mainArgs);
            runFunc("onCreate", savedInstanceState);
            if (!isSetViewed) {
                TypedArray array = getTheme().obtainStyledAttributes(new int[]{
                        android.R.attr.colorBackground,
                        android.R.attr.textColorPrimary,
                        android.R.attr.textColorHighlightInverse,
                });
                int backgroundColor = array.getColor(0, 0xFF00FF);
                int textColor = array.getColor(1, 0xFF00FF);
                array.recycle();
                status.setTextColor(textColor);
                layout.setBackgroundColor(backgroundColor);
                setContentView(layout);
            }
        } catch (Exception e) {
            sendMsg(e.getMessage());
            setContentView(layout);
            return;
        }
        // 绑定事件
        // onKeyShortcut
        LuaValue luaValue = L.get("onKeyShortcut");
        if (luaValue.type() != LuaType.NIL) mOnKeyShortcut = luaValue;
        // onKeyDown
        luaValue = L.get("onKeyDown");
        if (luaValue.type() != LuaType.NIL) mOnKeyDown = luaValue;
        // onKeyUp
        luaValue = L.get("onKeyUp");
        if (luaValue.type() != LuaType.NIL) mOnKeyUp = luaValue;
        // onKeyLongPress
        luaValue = L.get("onKeyLongPress");
        if (luaValue.type() != LuaType.NIL) mOnKeyLongPress = luaValue;
        // onTouchEvent
        luaValue = L.get("onTouchEvent");
        if (luaValue.type() != LuaType.NIL) mOnTouchEvent = luaValue;
        // onAccessibilityEvent
        luaValue = L.get("onAccessibilityEvent");
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (mOnKeyShortcut != null) {
            try {
                LuaValue[] ret = mOnKeyShortcut.call(keyCode, event);
                if (ret != null && ret.length > 0 && ret[0].toBoolean())
                    return true;
            } catch (LuaException e) {
                sendError("onKeyShortcut", e);
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    public void initMain() {
        prjCache.add(getLocalDir());
    }

    public String getLuaPath() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null)
            return null;
        String path = uri.getPath();
        if (!new File(path).exists() && new File(getLuaPath(path)).exists())
            path = getLuaPath(path);

        luaPath = path;
        File f = new File(path);

        luaDir = new File(luaPath).getParent();
        if (f.getName().equals("main.lua") && new File(luaDir, "init.lua").exists()) {
            if (!prjCache.contains(luaDir))
                prjCache.add(luaDir);
        } else {
            String parent = luaDir;
            while (parent != null) {
                if (prjCache.contains(parent)) {
                    luaDir = parent;
                    break;
                } else {
                    if (new File(parent, "main.lua").exists() && new File(parent, "init.lua").exists()) {
                        luaDir = parent;
                        if (!prjCache.contains(luaDir))
                            prjCache.add(luaDir);
                        break;
                    }
                }
                parent = new File(parent).getParent();
            }
        }
        return path;
    }


    @Override
    public String getLuaPath(String path) {
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaPath(String dir, String name) {
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String path) {
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String dir, String name) {
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaLpath() {
        // TODO: Implement this method
        return luaLpath;
    }

    @Override
    public String getLuaCpath() {
        // TODO: Implement this method
        return luaCpath;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Lua getLua() {
        return L;
    }

    public String getLocalDir() {
        return localDir;
    }

    @Override
    public String getLuaExtDir() {
        return luaExtDir;
    }

    @Override
    public void setLuaExtDir(String dir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = new File(sdDir, dir).getAbsolutePath();
        } else {
            File[] fs = new File("/storage").listFiles();
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null)
                    continue;
                if (ls.length > 5)
                    luaExtDir = new File(f, dir).getAbsolutePath();
            }
            if (luaExtDir == null)
                luaExtDir = getDir(dir, Context.MODE_PRIVATE).getAbsolutePath();
        }
        File d = new File(luaExtDir);
        if (!d.exists())
            d.mkdirs();
    }

    @Override
    public String getLuaExtDir(String name) {
        File dir = new File(getLuaExtDir(), name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;
        return dir.getAbsolutePath();
    }

    @Override
    public String getLuaDir() {
        return luaDir;
    }

    public void setLuaDir(String dir) {
        luaDir = dir;
    }

    @Override
    public String getLuaDir(String name) {
        File dir = new File(luaDir + "/" + name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;
        return dir.getAbsolutePath();
    }


    public DexClassLoader loadApp(String path) throws LuaException {
        return mLuaDexLoader.loadApp(path);
    }

    public DexClassLoader loadDex(String path) throws LuaException {
        return mLuaDexLoader.loadDex(path);
    }

    public void loadResources(String path) {
        mLuaDexLoader.loadResources(path);
    }

    @Override
    public AssetManager getAssets() {
        if (mLuaDexLoader != null && mLuaDexLoader.getAssets() != null)
            return mLuaDexLoader.getAssets();
        return super.getAssets();
    }

    public LuaResources getLuaResources() {
        Resources superRes = super.getResources();
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            superRes = mLuaDexLoader.getResources();
        mResources = new LuaResources(getAssets(), superRes.getDisplayMetrics(),
                superRes.getConfiguration());
        mResources.setSuperResources(superRes);
        return mResources;
    }

    public Resources getSuperResources() {
        return super.getResources();
    }

    @Override
    public Resources getResources() {
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            return mLuaDexLoader.getResources();
        if (mResources != null)
            return mResources;
        return super.getResources();
    }

    /*@Override
    public Resources.Theme getTheme() {
        if (mLuaDexLoader != null && mLuaDexLoader.getTheme() != null)
            return mLuaDexLoader.getTheme();
        return super.getTheme();
    }
*/
    public Object loadLib(String name) throws LuaException {
        int i = name.indexOf(".");
        String fn = name;
        if (i > 0)
            fn = name.substring(0, i);
        File f = new File(libDir + "/lib" + fn + ".so");
        if (!f.exists()) {
            f = new File(luaDir + "/lib" + fn + ".so");
            if (!f.exists())
                throw new LuaException(LuaError.FILE, "can not find lib " + name);
            LuaUtil.copyFile(luaDir + "/lib" + fn + ".so", libDir + "/lib" + fn + ".so");
        }
        LuaValue require = L.get("require");
        return require.call(name);
    }

    public void createShortcut(String text, String name) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setClassName(getPackageName(), LuaActivity.class.getName());
        intent.setData(Uri.parse(text));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            ShortcutInfo si = new ShortcutInfo.Builder(this, text)
                    .setIcon(Icon.createWithResource(this, R.drawable.icon))
                    .setShortLabel(name)
                    .setIntent(intent)
                    .build();
            try {
                scm.requestPinShortcut(si, null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "添加快捷方式出错", Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent addShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this,
                    R.drawable.icon);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            addShortcut.putExtra("duplicate", 0);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            sendBroadcast(addShortcut);
            Toast.makeText(this, "已添加快捷方式", Toast.LENGTH_SHORT).show();
        }
    }

    public void createShortcut(String text, String name, String icon) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setClassName(getPackageName(), LuaActivity.class.getName());
        intent.setData(Uri.parse(text));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            ShortcutInfo si = new ShortcutInfo.Builder(this, text)
                    .setIcon(Icon.createWithFilePath(icon))
                    .setShortLabel(name)
                    .setIntent(intent)
                    .build();
            try {
                scm.requestPinShortcut(si, null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "添加快捷方式出错", Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent addShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            //Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            addShortcut.putExtra("duplicate", 0);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeFile(icon));
            sendBroadcast(addShortcut);
            Toast.makeText(this, "已添加快捷方式", Toast.LENGTH_SHORT).show();
        }
    }

    private String getType(File file) {
        int lastDot = file.getName().lastIndexOf(46);
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(LuaBroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(LuaBroadcastReceiver.OnReceiveListener ltr, IntentFilter filter) {
        LuaBroadcastReceiver receiver = new LuaBroadcastReceiver(ltr);
        return super.registerReceiver(receiver, filter);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(IntentFilter filter) {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = new LuaBroadcastReceiver(this);
        return super.registerReceiver(mReceiver, filter);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            super.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        runFunc("onReceive", context, intent);
    }

    @Override
    public void onContentChanged() {
        // TODO: Implement this method
        super.onContentChanged();
        isSetViewed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        runFunc("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        runFunc("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        runFunc("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        runFunc("onStop");
    }

    public static LuaActivity getActivity(String name) {
        return sLuaActivityMap.get(name);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        sLuaActivityMap.remove(luaFileName);
        runFunc("onDestroy");
        super.onDestroy();
        System.gc();
        L.gc();
        L.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            String name = data.getStringExtra(NAME);
            if (name != null) {
                Object[] res = (Object[]) data.getSerializableExtra(DATA);
                if (res == null) {
                    runFunc("onResult", name);
                } else {
                    Object[] arg = new Object[res.length + 1];
                    arg[0] = name;
                    System.arraycopy(res, 0, arg, 1, res.length);
                    Object ret = runFunc("onResult", arg);
                    if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
                        return;
                }
            }
        }
        runFunc("onActivityResult", requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        runFunc("onRequestPermissionsResult", requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mOnKeyDown != null) {
            try {
                LuaValue[] ret = mOnKeyDown.call(keyCode, event);
                if (ret != null && ret.length > 0 && ret[0].toBoolean())
                    return true;
            } catch (LuaException e) {
                sendError("onKeyDown", e);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mOnKeyUp != null) {
            try {
                LuaValue[] ret = mOnKeyUp.call(keyCode, event);
                if (ret != null && ret.length > 0 && ret[0].toBoolean())
                    return true;
            } catch (LuaException e) {
                sendError("onKeyUp", e);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mOnKeyLongPress != null) {
            try {
                LuaValue[] ret = mOnKeyLongPress.call(keyCode, event);
                if (ret != null && ret.length > 0 && ret[0].toBoolean())
                    return true;
            } catch (LuaException e) {
                sendError("onKeyLongPress", e);
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnTouchEvent != null) {
            try {
                LuaValue[] ret = mOnTouchEvent.call(event);
                if (ret != null && ret.length > 0 && ret[0].toBoolean())
                    return true;
            } catch (LuaException e) {
                sendError("onTouchEvent", e);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        runFunc("onCreateOptionsMenu", menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Implement this method
        Object ret = null;
        if (!item.hasSubMenu())
            ret = runFunc("onOptionsItemSelected", item);
        if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
            return true;
        return super.onOptionsItemSelected(item);
    }

    public Menu getOptionsMenu() {
        return optionsMenu;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // TODO: Implement this method
        if (!item.hasSubMenu())
            runFunc("onMenuItemSelected", featureId, item);
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // TODO: Implement this method
        runFunc("onCreateContextMenu", menu, v, menuInfo);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO: Implement this method
        runFunc("onContextItemSelected", item);
        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO: Implement this method
        super.onConfigurationChanged(newConfig);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //wm.getDefaultDisplay().getRealMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
        runFunc("onConfigurationChanged", newConfig);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public HashMap<String, Object> getGlobalData() {
        return ((LuaApplication) getApplication()).getGlobalData();
    }

    @Override
    public Object getSharedData() {
        return LuaApplication.getInstance().getSharedData();
    }

    @Override
    public Object getSharedData(String key) {
        return LuaApplication.getInstance().getSharedData(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        return LuaApplication.getInstance().getSharedData(key, def);
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        return LuaApplication.getInstance().setSharedData(key, value);
    }

    public boolean bindService(int flag) {
        ServiceConnection conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName comp, IBinder binder) {
                // TODO: Implement this method
                // runFunc("onServiceConnected", comp, ((LuaService.LuaBinder) binder).getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName comp) {
                // TODO: Implement this method
                runFunc("onServiceDisconnected", comp);
            }
        };
        return bindService(conn, flag);
    }

    public boolean bindService(ServiceConnection conn, int flag) {
        // TODO: Implement this method
        // Intent service = new Intent(this, LuaService.class);
        // service.putExtra("luaDir", luaDir);
        // service.putExtra("luaPath", luaPath);
        // return super.bindService(service, conn, flag);
        return false;
    }

    public boolean stopService() {
        // return stopService(new Intent(this, LuaService.class));
        return false;
    }

    public ComponentName startService() {
        return startService(null, null);
    }

    public ComponentName startService(Object[] arg) {
        return startService(null, arg);
    }

    public ComponentName startService(String path) {
        return startService(path, null);
    }

    public ComponentName startService(String path, Object[] arg) {
        // TODO: Implement this method
        // Intent intent = new Intent(this, LuaService.class);
        // intent.putExtra("luaDir", luaDir);
        // intent.putExtra("luaPath", luaPath);
        // if (path != null) {
        //     if (path.charAt(0) != '/')
        //          intent.setData(Uri.parse("file://" + luaDir + "/" + path + ".lua"));
        //     else
        //         intent.setData(Uri.parse("file://" + path));
        // }

        // if (arg != null)
        //     intent.putExtra(ARG, arg);

        // return super.startService(intent);
        return null;
    }

    public void newActivity(String path) throws FileNotFoundException {
        newActivity(1, path, new Object[0]);
    }

    public void newActivity(String path, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, arg);
    }

    public void newActivity(int req, String path) throws FileNotFoundException {
        newActivity(req, path, new Object[0]);
    }

    public void newActivity(int req, String path, Object[] arg) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/')
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists())
            path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        intent.setData(Uri.parse("file://" + path));

        if (arg != null)
            intent.putExtra(ARG, arg);
        else
            startActivityForResult(intent, req);
        // overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void newActivity(String path, int in, int out) throws FileNotFoundException {
        newActivity(1, path, in, out, new Object[0]);
    }

    public void newActivity(String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, in, out, arg);
    }

    public void newActivity(int req, String path, int in, int out) throws FileNotFoundException {
        newActivity(req, path, in, out, new Object[0]);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/')
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists())
            path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);
        intent.setData(Uri.parse("file://" + path));
        if (arg != null)
            intent.putExtra(ARG, arg);
        else
            startActivityForResult(intent, req);
        overridePendingTransition(in, out);
    }

    @SuppressLint("ObsoleteSdkInt")
    public void finish(boolean finishTask) {
        if (!finishTask) {
            super.finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = getIntent();
            if (intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
                finishAndRemoveTask();
            else
                super.finish();
        } else {
            super.finish();
        }
    }
//
//    public LuaAsyncTask newTask(LuaObject func) throws LuaException {
//        return newTask(func, null, null);
//    }
//
//    public LuaAsyncTask newTask(LuaObject func, LuaObject callback) throws LuaException {
//        return newTask(func, null, callback);
//    }
//
//    public LuaAsyncTask newTask(LuaObject func, LuaObject update, LuaObject callback) throws LuaException {
//        return new LuaAsyncTask(this, func, update, callback);
//    }
//
//    public LuaThread newThread(LuaObject func) throws LuaException {
//        return newThread(func, new Object[0]);
//    }
//
//    public LuaThread newThread(LuaObject func, Object[] arg) throws LuaException {
//        LuaThread thread = new LuaThread(this, func, true, arg);
//        return thread;
//    }
//
//    public LuaTimer newTimer(LuaObject func) throws LuaException {
//        return newTimer(func, new Object[0]);
//    }
//
//    public LuaTimer newTimer(LuaObject func, Object[] arg) throws LuaException {
//        return new LuaTimer(this, func, arg);
//    }
//
//    public LuaAsyncTask task(long delay, LuaObject func) throws LuaException {
//        return task(delay, null, null);
//    }
//
//    public LuaAsyncTask task(long delay, Object[] arg, LuaObject func) throws LuaException {
//        LuaAsyncTask task = new LuaAsyncTask(this, delay, func);
//        task.execute(arg);
//        return task;
//    }
//
//    public LuaAsyncTask task(LuaObject func) throws LuaException {
//        return task(func, null, null, null);
//    }
//
//    public LuaAsyncTask task(LuaObject func, Object[] arg) throws LuaException {
//        return task(func, arg, null, null);
//    }
//
//    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject callback) throws LuaException {
//        return task(func, null, null, callback);
//    }
//
//    public LuaAsyncTask task(LuaObject func, LuaObject update, LuaObject callback) throws LuaException {
//        return task(func, null, update, callback);
//    }
//
//    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject update, LuaObject callback) throws LuaException {
//        LuaAsyncTask task = new LuaAsyncTask(this, func, update, callback);
//        task.execute(arg);
//        return task;
//    }
//
//    public LuaThread thread(LuaObject func) throws LuaException {
//        LuaThread thread = newThread(func, new Object[0]);
//        thread.start();
//        return thread;
//    }
//
//    public LuaThread thread(LuaObject func, Object[] arg) throws LuaException {
//        LuaThread thread = new LuaThread(this, func, true, arg);
//        thread.start();
//        return thread;
//    }
//
//    public LuaTimer timer(LuaObject func, long period) throws LuaException {
//        return timer(func, 0, period, new Object[0]);
//    }
//
//    public LuaTimer timer(LuaObject func, long period, Object[] arg) throws LuaException {
//        return timer(func, 0, period, arg);
//    }
//
//    public LuaTimer timer(LuaObject func, long delay, long period) throws LuaException {
//        return timer(func, delay, period, new Object[0]);
//    }
//
//    public LuaTimer timer(LuaObject func, long delay, long period, Object[] arg) throws LuaException {
//        LuaTimer timer = new LuaTimer(this, func, arg);
//        timer.start(delay, period);
//        return timer;
//    }
//
//    public Ticker ticker(final LuaObject func, long period) throws LuaException {
//        Ticker timer = new Ticker();
//        timer.setOnTickListener(new Ticker.OnTickListener() {
//            @Override
//            public void onTick() {
//                try {
//                    func.call();
//                } catch (LuaException e) {
//                    e.printStackTrace();
//                    sendError("onTick", e);
//                }
//            }
//        });
//        timer.start();
//        timer.setPeriod(period);
//        return timer;
//    }
//
//    public Bitmap loadBitmap(String path) throws IOException {
//        return LuaBitmap.getBitmap(this, path);
//    }

    public void result(Object[] data) {
        Intent res = new Intent();
        res.putExtra(NAME, getIntent().getStringExtra(NAME));
        res.putExtra(DATA, data);
        setResult(0, res);
        finish();
    }

    //初始化lua使用的Java函数
    private void initLua() throws Exception {
        L = new LuaJit();
        L.openLibraries();
        L.pushJavaObject(this);
        L.setGlobal("activity");
        L.getGlobal("activity");
        L.setGlobal("this");
//        L.push(luaExtDir);
//        L.setField(-2, "luaextdir");
//        L.push(luaDir);
//        L.setField(-2, "luadir");
//        L.push(luaPath);
//        L.setField(-2, "luapath");
//        L.pop(1);
        initENV();
        // LuaPrint
        LuaPrint print = new LuaPrint(this);
        L.push(print);
        L.setGlobal("print");
        // package
//        L.getGlobal("package");
//        L.push(luaLpath);
//        L.setField(-2, "path");
//        L.push(luaCpath);
//        L.setField(-2, "cpath");
//        L.pop(1);
//
//        JavaFunction set = new JavaFunction(L) {
//            @Override
//            public int execute() throws LuaException {
//                LuaThread thread = (LuaThread) L.toJavaObject(2);
//
//                thread.set(L.toString(3), L.toJavaObject(4));
//                return 0;
//            }
//        };
//        set.register("set");
//
//        JavaFunction call = new JavaFunction(L) {
//            @Override
//            public int execute() throws LuaException {
//                LuaThread thread = (LuaThread) L.toJavaObject(2);
//
//                int top = L.getTop();
//                if (top > 3) {
//                    Object[] args = new Object[top - 3];
//                    for (int i = 4; i <= top; i++) {
//                        args[i - 4] = L.toJavaObject(i);
//                    }
//                    thread.call(L.toString(3), args);
//                } else if (top == 3) {
//                    thread.call(L.toString(3));
//                }
//
//                return 0;
//            }
//
//            ;
//        };
//        call.register("call");

    }

    public void setDebug(boolean isDebug) {
        mDebug = isDebug;
    }

    private void initENV() throws LuaException {
        String initLua = luaDir + "/init.lua";
        if (!new File(initLua).exists())
            return;
        Lua l = null;
        FileInputStream fileInputStream = null;
        try {
            l = new LuaJit();
            l.openLibraries();
            l.eval(new String(LuaUtil.readAll(initLua)));
            // appname
            LuaValue title = l.get("appname");
            if (title.type() == LuaType.NIL) {
                title = l.get("app_name");
                if (title.type() != LuaType.NIL) {
                    setTitle(title.toString());
                }
            } else {
                setTitle(title.toString());
            }
            // debug_mode
            mDebug = l.get("debugmode").toBoolean() || l.get("debug_mode").toBoolean();
            // theme
            LuaValue theme = l.get("theme");
            switch (theme.type()) {
                case NUMBER:
                    setTheme((int) theme.toInteger());
                    break;
                case STRING:
                    setTheme(android.R.style.class.getField(theme.toString()).getInt(null));
                    break;
            }
            return;
        } catch (Exception e) {
            sendMsg(e.getMessage());
        } finally {
            if (l != null) l.close();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription tDesc = null;
//            try {
//                tDesc = new ActivityManager.TaskDescription(title.toString(), loadBitmap(getLuaPath("icon.png")));
//            } catch (IOException e) {
//                e.printStackTrace();
            tDesc = new ActivityManager.TaskDescription(title.toString());
//            }
            setTaskDescription(tDesc);
        }
    }


    //运行lua脚本
    public Object doFile(String filePath) {
        return doFile(filePath, new Object[0]);
    }

    public Object doFile(String filePath, Object[] args) {
        int ok = 0;
        try {
            if (filePath.charAt(0) != '/')
                filePath = luaDir + "/" + filePath;

            Object ret = doString(new String(LuaUtil.readAll(filePath)));
            Intent res = new Intent();
            res.putExtra(DATA, (Boolean) ret);
            setResult(ok, res);
            return ret;
        } catch (LuaException | IOException ignored) {

        }
        return null;
    }

    public Object doAsset(String name, Object[] args) {
        int ok = 0;
        try {
            return doString(new String(LuaUtil.readAsset(this, name)), args);
        } catch (Exception e) {
            setTitle(errorReason(ok));
            setContentView(layout);
            sendMsg(e.getMessage());
        }

        return null;
    }

    //运行lua函数
    public Object runFunc(String funcName, Object... args) {
        if (L == null) return null;
        synchronized (L) {
            try {
                // 清空 Lua 栈
                L.setTop(0);
                L.getGlobal(funcName);
                if (!L.isFunction(-1)) return null;
                // 压 traceback 作为错误处理
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                // 压入参数
                for (Object arg : args) {
                    L.push(arg, Lua.Conversion.SEMI);
                }
                L.pCall(args.length, 1);
                return L.toObject(-1);
            } catch (LuaException e) {
                sendError(funcName, e);
            } finally {
                L.setTop(0);
            }
        }

        return null;
    }


    //运行lua代码
    public Object doString(String funcSrc, Object... args) throws LuaException {
        L.setTop(0);
        L.load(funcSrc);
        for (Object arg : args) {
            L.push(arg, Lua.Conversion.SEMI);
        }
        L.pCall(args.length, 1);
        return L.toObject(-1);
    }

//读取asset文件

    //生成错误信息
    private String errorReason(int error) {
        switch (error) {
            case 6:
                return "error error";
            case 5:
                return "GC error";
            case 4:
                return "Out of memory";
            case 3:
                return "Syntax error";
            case 2:
                return "Runtime error";
            case 1:
                return "Yield error";
        }
        return "Unknown error " + error;
    }

    public void showLogs() {
        new AlertDialog.Builder(this)
                .setTitle("Logs")
                .setAdapter(adapter, null)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    //显示信息
    public void sendMsg(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, msg);
        message.setData(bundle);
        message.what = 0;
        handler.sendMessage(message);
        Log.i("lua", msg);
    }

    @Override
    public void sendError(String title, Exception msg) {
        Object ret = runFunc("onError", title, msg);
        if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
            return;
        else
            sendMsg(title + ": " + msg.getMessage());
    }

    //显示toast
    @SuppressLint("ShowToast")
    public void showToast(String text) {
        long now = System.currentTimeMillis();
        if (toast == null || now - lastShow > 1000) {
            toastbuilder.setLength(0);
            toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toastbuilder.append(text);
            toast.show();
        } else {
            toastbuilder.append("\n");
            toastbuilder.append(text);
            toast.setText(toastbuilder.toString());
            toast.setDuration(Toast.LENGTH_LONG);
        }
        lastShow = now;
    }

    private void setField(String key, Object value) {
        synchronized (L) {
            try {
                L.set(key, value);
            } catch (LuaException e) {
                sendError("setField", e);
            }
        }
    }

    public void call(String func) {
        push(2, func);

    }

    public void call(String func, Object[] args) {
        if (args.length == 0)
            push(2, func);
        else
            push(3, func, args);
    }

    public void set(String key, Object value) {
        push(1, key, new Object[]{value});
    }

    public Object get(String key) throws LuaException {
        synchronized (L) {
            L.getGlobal(key);
            return L.toJavaObject(-1);
        }
    }

    public void push(int what, String s) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);

    }

    public void push(int what, String s, Object[] args) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        bundle.putSerializable("args", args);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);

    }


    public class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {
                    String data = msg.getData().getString(DATA);
                    if (mDebug)
                        showToast(data);
                    status.append(data + "\n");
                    adapter.add(data);
                }
                break;
                case 1: {
                    Bundle data = msg.getData();
                    setField(data.getString(DATA), ((Object[]) data.getSerializable("args"))[0]);
                }
                break;
                case 2: {
                    String src = msg.getData().getString(DATA);
                    runFunc(src);
                }
                break;
                case 3: {
                    String src = msg.getData().getString(DATA);
                    Serializable args = msg.getData().getSerializable("args");
                    runFunc(src, (Object[]) args);
                }
            }
        }
    }
}

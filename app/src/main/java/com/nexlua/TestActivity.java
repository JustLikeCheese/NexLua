package com.nexlua;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.luajava.CFunction;
import com.luajava.JuaAPI;
import com.luajava.Lua;
import com.luajava.LuaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.github.justlikecheese.nextoast.NexToast;

public class TestActivity extends Activity {
    public static final Lua L = new Lua();
    public static final String[] LUA_TESTS;

    static {
        LUA_TESTS = new String[]{
                "Test Java Class", "test-class.lua",
                "Test Java Object", "test-object.lua"
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.openLibraries();
        L.setHandler(this::sendException);
        L.push(new CFunction() {
            private final StringBuilder output = new StringBuilder();
            @Override
            public int __call(Lua L) {
                int top = L.getTop();
                if (top > 0) {
                    output.append(L.ltoString(1));
                    for (int i = 2; i <= top; i++) {
                        output.append("\t");
                        output.append(L.ltoString(i));
                    }
                    showToast(output.toString());
                    output.setLength(0);
                } else {
                    showToast("");
                }
                return 0;
            }
        });
        L.setGlobal("print");
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setFitsSystemWindows(true);
        setContentView(layout);
        for (int idx = 0; idx < LUA_TESTS.length; idx += 2) {
            try {
                String code = readAssets("test/" + LUA_TESTS[idx + 1]);
                Button button = new Button(this);
                button.setText(LUA_TESTS[idx]);
                button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            L.doString(code);
                        } catch (LuaException e) {
                            sendException(e);
                        }
                    }
                });
                layout.addView(button);
            } catch (Exception e) {
                sendException(e);
            }
        }
    }

    public String readAssets(String fileName) {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        String content = null;
        try {
            inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            content = sb.toString();
        } catch (IOException e) {
            sendException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    sendException(e);
                }
            }
        }
        return content;
    }

    public void sendException(Exception e) {
        if (e instanceof LuaException) {
            LuaException luaException = (LuaException) e;
            showDialog(luaException.getType(), luaException.getMessage());
        } else {
            showDialog("Exception", JuaAPI.getStackTrace(e));
        }
    }

    public void showDialog(String name, String message) {
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    NexToast mToast;
    StringBuilder mToastBuilder = new StringBuilder();
    long mToastTime;
    public void showToast(String message) {
        long now = System.currentTimeMillis();
        if (mToast == null || now - mToastTime > 1000) {
            mToastBuilder.setLength(0);
            mToast = NexToast.makeText(this, message, Toast.LENGTH_LONG);
            mToastBuilder.append(message);
            mToast.show();
        } else {
            mToastBuilder.append("\n");
            mToastBuilder.append(message);
            mToast.setText(mToastBuilder.toString());
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToastTime = now;
    }
}

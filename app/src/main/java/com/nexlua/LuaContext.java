package com.nexlua;


import android.content.Context;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.LuaException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface LuaContext {
    String LUA_PATH = "path";
    String LUA_ARG = "arg";
    String LUA_NEW_ACTIVITY_NAME = "name";
    String LUA_NEW_ACTIVITY_DATA = "data";

    ArrayList<ClassLoader> getClassLoaders();

    Lua getLua();

    File getLuaFile();

    File getLuaDir();

    String getLuaLpath();

    String getLuaCpath();

    Context getContext();

    default void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    default void sendMessage(String message) {
        showToast(message);
    }

    default void sendError(String title, String error) {
        showToast(error);
    }

    default void sendError(Exception e) {
        if (e instanceof LuaException) {
            LuaException luaException = (LuaException) e;
            sendError(luaException.getType(), e.getMessage());
        } else {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendError(e.getClass().getSimpleName(), sw.toString());
        }
    }

    default void initializeLua() {
        Lua L = getLua();
        L.openLibraries();
        // traceback
        L.traceback(true);
        L.setExternalLoader(new LuaModuleLoader(this));
    }

    default void doModule(LuaModule luaModule, String name, Object... args) {
        doString((ByteBuffer) luaModule.load(this), name, args);
    }

    default void doString(String code, String name, Object... args) {
        doString(code.getBytes(), name, args);
    }

    default void doString(byte[] bytes, String name, Object... arg) {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
        directBuffer.put(bytes);
        directBuffer.flip();
        doString(directBuffer, name, arg);
    }

    default void doString(ByteBuffer directBuffer, String name, Object... args) {
        final Lua L = getLua();
        synchronized (L) {
            final int oldTop = L.getTop();
            try {
                L.load(directBuffer, name);
                if (args != null) {
                    for (Object arg : args) L.push(arg, Lua.Conversion.SEMI);
                    L.pCall(args.length, 0);
                } else {
                    L.pCall(0, 0);
                }
            } catch (Exception e) {
                sendError(e);
            } finally {
                L.setTop(oldTop);
            }
        }
    }

    default void doFile(File filePath) {
        doFile(filePath, new Object[0]);
    }

    default void doFile(File file, Object... args) {
        try {
            doString(LuaUtil.readFileBuffer(file), file.getPath(), args);
        } catch (IOException e) {
            sendError(e);
        }
    }

    default void doAsset(String name, Object[] args) {
        try {
            doString(LuaUtil.readAssetBuffer(name), name, args);
        } catch (IOException e) {
            sendError(e);
        }
    }
}

package com.nexlua;


import android.content.Context;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.LuaException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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
}

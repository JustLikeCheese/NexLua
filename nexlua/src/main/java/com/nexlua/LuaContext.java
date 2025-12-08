package com.nexlua;

import android.content.Context;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;
import com.luajava.value.LuaValue;

import java.util.ArrayList;

public interface LuaContext extends LuaHandler {
    ArrayList<ClassLoader> getClassLoaders();

    Lua getLua();

    LuaConfig getConfig();

    String getLuaDir();

    String getLuaPath();

    String getLuaLpath();

    String getLuaCpath();

    Context getContext();

    void showToast(String message);

    void sendMessage(String message);

    void sendError(Exception e);
}

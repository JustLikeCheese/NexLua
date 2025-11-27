package com.nexlua;

import android.content.Context;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;

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

    void initialize(Lua L) throws LuaException;

    void showToast(String message);

    void sendMessage(String message);

    void sendError(Exception e);
}

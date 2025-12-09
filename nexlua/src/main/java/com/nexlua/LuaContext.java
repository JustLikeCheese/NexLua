package com.nexlua;

import android.content.Context;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;
import com.luajava.value.referable.LuaFunction;

import java.util.ArrayList;

public interface LuaContext extends LuaHandler, LuaContextUtils {
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

    default boolean runFunc(String funcName, Object... args) {
        return LuaContextUtils.runFunc(getLua(), funcName, args);
    }

    default Object runFunc(String funcName, Class<?> clazz, Object... args) {
        return LuaContextUtils.runFunc(getLua(), funcName, clazz, args);
    }
}

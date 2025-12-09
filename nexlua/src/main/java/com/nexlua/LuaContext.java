package com.nexlua;

import android.content.Context;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;
import com.luajava.value.referable.LuaFunction;

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

    default boolean runFunc(String funcName, Object... args) {
        Object result = runFunc(funcName, boolean.class, args);
        return result != null && (boolean) result;
    }

    default Object runFunc(String funcName, Class<?> clazz, Object... args) {
        if (funcName != null) {
            Lua L = getLua();
            try {
                L.getGlobal(funcName);
                if (L.isFunction(-1)) {
                    L.pCall(args, Lua.Conversion.SEMI, 1);
                    Object result = L.toJavaObject(1, clazz);
                    L.pop(1);
                    return result;
                }
            } catch (LuaException e) {
                sendError(e);
            }
        }
        return null;
    }

    default boolean onLuaEvent(LuaFunction function, Object... args) {
        Object result = onLuaEvent(function, boolean.class, args);
        return result != null && (boolean) result;
    }

    default Object onLuaEvent(LuaFunction function, Class<?> clazz, Object... args) {
        if (function != null) {
            final Lua L = function.state();
            try {
                function.pCall(args, Lua.Conversion.SEMI, 1);
                Object result = L.toJavaObject(1, clazz);
                L.pop(1);
                return result;
            } catch (LuaException e) {
                L.sendError(e);
            }
        }
        return null;
    }
}

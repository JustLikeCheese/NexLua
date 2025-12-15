package com.nexlua;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.referable.LuaFunction;

public interface LuaContextUtils {
    default boolean onLuaEvent(LuaFunction function, Object... args) {
        if (function != null) {
            return onLuaEvent(function.state(), function, args);
        }
        return false;
    }

    default Object onLuaEvent(LuaFunction function, Class<?> clazz, Object... args) {
        if (function != null) {
            return onLuaEvent(function.state(), function, clazz, args);
        }
        return null;
    }

    static boolean runFunc(Lua L, String funcName, Object... args) {
        Object result = runFunc(L, funcName, boolean.class, args);
        return result != null && (boolean) result;
    }

    static Object runFunc(Lua L, String funcName, Class<?> clazz, Object... args) {
        if (funcName != null) {
            try {
                L.getGlobal(funcName);
                if (L.isFunction(-1)) {
                    L.pCall(args, Lua.Conversion.SEMI, 1);
                    Object result = L.toJavaObject(1, clazz);
                    L.pop(1);
                    return result;
                }
            } catch (Exception e) {
                L.sendError(e);
            }
        }
        return null;
    }

    static boolean onLuaEvent(Lua L, LuaFunction function, Object... args) {
        Object result = onLuaEvent(L, function, boolean.class, args);
        return result != null && (boolean) result;
    }

    static Object onLuaEvent(Lua L, LuaFunction function, Class<?> clazz, Object... args) {
        if (function != null) {
            try {
                function.pCall(args, Lua.Conversion.SEMI, 1);
                Object result = L.toJavaObject(1, clazz);
                L.pop(1);
                return result;
            } catch (Exception e) {
                L.sendError(e);
            }
        }
        return null;
    }
}

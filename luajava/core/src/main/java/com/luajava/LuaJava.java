package com.luajava;

import com.luajava.util.ClassUtils;

public class LuaJava {
    public static int bindClass(long ptr, String name) throws ClassNotFoundException, LuaException {
        Lua L = Jua.get(ptr);
        L.push(ClassUtils.forName(name));
        return 1;
    }
}

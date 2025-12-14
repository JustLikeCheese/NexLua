package com.nexlua.module;

import androidx.annotation.NonNull;

import com.luajava.CFunction;
import com.luajava.Lua;

import java.io.Serializable;

public interface LuaModule extends Serializable, CFunction {
    int __call(Lua L) throws Exception;

    default int run(Lua L) throws Exception {
        L.push(getAbsolutePath());
        return __call(L);
    }

    @NonNull
    String getAbsolutePath();
}

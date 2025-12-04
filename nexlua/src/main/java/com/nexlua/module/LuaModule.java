package com.nexlua.module;

import androidx.annotation.NonNull;

import com.luajava.Lua;
import com.nexlua.LuaContext;

import java.io.Serializable;

public interface LuaModule extends Serializable {
    int load(Lua L, LuaContext luaContext);

    @NonNull String getAbsolutePath();
}

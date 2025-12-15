package com.nexlua.module;

import androidx.annotation.NonNull;

import com.luajava.CFunction;
import com.luajava.Lua;

import java.io.Serializable;

public interface LuaModule extends Serializable {
    int load(Lua L) throws Exception;

    default int run(Lua L) throws Exception {
        L.push(getAbsolutePath());
        return load(L);
    }

    @NonNull
    String getAbsolutePath();

    class Loader implements CFunction {
        private final LuaModule module;

        public Loader(LuaModule module) {
            this.module = module;
        }

        @Override
        public int __call(Lua L) throws Exception {
            return module.load(L);
        }
    }
}

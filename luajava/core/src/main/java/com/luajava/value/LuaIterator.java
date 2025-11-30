package com.luajava.value;

import com.luajava.Lua;
import com.luajava.LuaException;

public class LuaIterator {

    @FunctionalInterface
    public interface Ipairs {
        /**
         * Stack: -1 is value
         *
         * @param L Lua
         * @param index Current index
         * @return false continue; true break
         */
        boolean iterate(Lua L, int index) throws LuaException;
    }

    @FunctionalInterface
    public interface Pairs {
        /**
         * Stack: -2 is key, -1 is value
         *
         * @param L Lua
         * @return false continue; true break
         */
        boolean iterate(Lua L) throws LuaException;
    }
}
package com.luajava;

import com.luajava.value.LuaValue;

/**
 * Interface for functions implemented in Java.
 */
public interface JFunction {
    /**
     * Implements the function body
     *
     * <p>
     * Unlike {@link com.luajava.CFunction#__call(Lua)}, before actually calling this function,
     * the library converts all the arguments to {@link LuaValue LuaValues} and pops them off the stack.
     * </p>
     *
     * @param L    the Lua state
     * @param args the arguments
     * @return the return values (nullable)
     */
    LuaValue[] call(Lua L, LuaValue[] args);
}

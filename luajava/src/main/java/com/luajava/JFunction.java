/*
 * Copyright (C) 2025 JustLikeCheese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.luajava;

import com.luajava.value.LuaValue;

/**
 * Interface for functions implemented in Java.
 */
public abstract class JFunction implements CFunction {

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
    public abstract LuaValue[] call(Lua L, LuaValue[] args);

    @Override
    public int __call(Lua L) {
        LuaValue[] args = new LuaValue[L.getTop()];
        for (int i = 0; i < args.length; i++) {
            args[args.length - i - 1] = L.get();
        }
        LuaValue[] results = this.call(L, args);
        if (results != null) {
            for (LuaValue result : results) {
                L.push(result);
            }
            return results.length;
        }
        return 0;
    }
}

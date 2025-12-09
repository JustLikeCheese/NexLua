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

package com.luajava.value;

import com.luajava.Lua;

/**
 * LuaJIT2.1 ROLLING METAMETHOD
 * __index, __newindex
 * __concat, __call, __tostring
 * __len, __eq, __lt, __le
 * __pairs, __ipairs, __metatable, __gc, __mode
 * __add, __sub, __mul, __div, __mod, __pow, __unm
 */
public interface LuaMetatable {
    enum MetaName {
        __index, __newindex, __concat, __call, __tostring,
        __len, __eq, __lt, __le, __pairs, __ipairs,
        __metatable, __gc, __mode,
        __add, __sub, __mul, __div, __mod, __pow, __unm
    }

    default int __index(Lua L) {
        return 0;
    }

    default int __newindex(Lua L) {
        return 0;
    }

    default int __concat(Lua L) {
        return 0;
    }

    default int __call(Lua L) {
        return 0;
    }

    default int __tostring(Lua L) {
        return 0;
    }

    default int __len(Lua L) {
        return 0;
    }

    default int __eq(Lua L) {
        return 0;
    }

    default int __lt(Lua L) {
        return 0;
    }

    default int __le(Lua L) {
        return 0;
    }

    default int __pairs(Lua L) {
        return 0;
    }

    default int __ipairs(Lua L) {
        return 0;
    }

    default int __metatable(Lua L) {
        return 0;
    }

    default int __gc(Lua L) {
        return 0;
    }

    default int __mode(Lua L) {
        return 0;
    }

    default int __add(Lua L) {
        return 0;
    }

    default int __sub(Lua L) {
        return 0;
    }

    default int __mul(Lua L) {
        return 0;
    }

    default int __div(Lua L) {
        return 0;
    }

    default int __mod(Lua L) {
        return 0;
    }

    default int __pow(Lua L) {
        return 0;
    }

    default int __unm(Lua L) {
        return 0;
    }
}

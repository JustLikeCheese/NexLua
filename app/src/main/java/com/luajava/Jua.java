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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Jua {
    private static final Map<Long, Lua> states = new ConcurrentHashMap<>();

    private Jua() {
    }

    public static Lua get(long ptr) {
        return states.get(ptr);
    }

    public static long add(Lua L) {
        long ptr = L.getPointer();
        states.put(ptr, L);
        return ptr;
    }

    public static void remove(long ptr) {
        states.remove(ptr);
    }

    public static void remove(Lua L) {
        remove(L.getPointer());
    }

    public static void clear() {
        states.clear();
    }

    public static int size() {
        return states.size();
    }
}
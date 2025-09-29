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
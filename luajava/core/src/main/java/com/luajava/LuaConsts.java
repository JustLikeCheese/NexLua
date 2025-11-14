package com.luajava;

public final class LuaConsts {
    /**
     * Private constructor to prevent instantiation.
     */
    private LuaConsts() {
    }
// ----------------------------------------------------------------------------
// Constants from lua.h
// ----------------------------------------------------------------------------
    /**
     * Version and author information.
     */
    public static final String LUA_VERSION = "Lua 5.1";
    public static final String LUA_RELEASE = "Lua 5.1.4";
    public static final String LUA_COPYRIGHT = "Copyright (C) 1994-2008 Lua.org, PUC-Rio";
    public static final String LUA_AUTHORS = "R. Ierusalimschy, L. H. de Figueiredo & W. Celes";
    public static final int LUA_VERSION_NUM = 501;
    /**
     * Mark for precompiled code (`<esc>Lua').
     */
    public static final String LUA_SIGNATURE = "\033Lua";
    /**
     * Option for multiple returns in lua_pcall and lua_call.
     */
    public static final int LUA_MULTRET = -1;
    /**
     * Pseudo-indices for the Lua stack.
     */
    public static final int LUA_REGISTRYINDEX = -10000;
    public static final int LUA_ENVIRONINDEX = -10001;
    public static final int LUA_GLOBALSINDEX = -10002;
    /**
     * Thread status codes.
     */
    public static final int LUA_OK = 0;
    public static final int LUA_YIELD = 1;
    public static final int LUA_ERRRUN = 2;
    public static final int LUA_ERRSYNTAX = 3;
    public static final int LUA_ERRMEM = 4;
    public static final int LUA_ERRERR = 5;
    /**
     * Basic Lua types.
     */
    public static final int LUA_TNONE = -1;
    public static final int LUA_TNIL = 0;
    public static final int LUA_TBOOLEAN = 1;
    public static final int LUA_TLIGHTUSERDATA = 2;
    public static final int LUA_TNUMBER = 3;
    public static final int LUA_TSTRING = 4;
    public static final int LUA_TTABLE = 5;
    public static final int LUA_TFUNCTION = 6;
    public static final int LUA_TUSERDATA = 7;
    public static final int LUA_TTHREAD = 8;
    /**
     * Minimum Lua stack available to a C function.
     */
    public static final int LUA_MINSTACK = 20;
    /**
     * Garbage Collector options.
     */
    public static final int LUA_GCSTOP = 0;
    public static final int LUA_GCRESTART = 1;
    public static final int LUA_GCCOLLECT = 2;
    public static final int LUA_GCCOUNT = 3;
    public static final int LUA_GCCOUNTB = 4;
    public static final int LUA_GCSTEP = 5;
    public static final int LUA_GCSETPAUSE = 6;
    public static final int LUA_GCSETSTEPMUL = 7;
    public static final int LUA_GCISRUNNING = 9;
    /**
     * Debug hook event codes.
     */
    public static final int LUA_HOOKCALL = 0;
    public static final int LUA_HOOKRET = 1;
    public static final int LUA_HOOKLINE = 2;
    public static final int LUA_HOOKCOUNT = 3;
    public static final int LUA_HOOKTAILRET = 4;
    /**
     * Debug hook event masks.
     */
    public static final int LUA_MASKCALL = 1;      // (1 << LUA_HOOKCALL)
    public static final int LUA_MASKRET = 2;       // (1 << LUA_HOOKRET)
    public static final int LUA_MASKLINE = 4;      // (1 << LUA_HOOKLINE)
    public static final int LUA_MASKCOUNT = 8;     // (1 << LUA_HOOKCOUNT)
// ----------------------------------------------------------------------------
// Constants from lauxlib.h
// ----------------------------------------------------------------------------
    /**
     * Pre-defined references.
     */
    public static final int LUA_NOREF = -2;
    public static final int LUA_REFNIL = -1;
    /**
     * Extra error code for luaL_load.
     * (LUA_ERRERR + 1) = (5 + 1) = 6
     */
    public static final int LUA_ERRFILE = 6;
// ----------------------------------------------------------------------------
// Constants from luajit.h
// ----------------------------------------------------------------------------
    /**
     * LuaJIT version and info.
     */
    public static final String LUAJIT_VERSION = "LuaJIT 2.1.1748459687";
    public static final int LUAJIT_VERSION_NUM = 20199;
    public static final String LUAJIT_COPYRIGHT = "Copyright (C) 2005-2025 Mike Pall";
    public static final String LUAJIT_URL = "https://luajit.org/";
    /**
     * Modes for luaJIT_setmode.
     */
    public static final int LUAJIT_MODE_ENGINE = 0;
    public static final int LUAJIT_MODE_DEBUG = 1;
    public static final int LUAJIT_MODE_FUNC = 2;
    public static final int LUAJIT_MODE_ALLFUNC = 3;
    public static final int LUAJIT_MODE_ALLSUBFUNC = 4;
    public static final int LUAJIT_MODE_TRACE = 5;
    public static final int LUAJIT_MODE_WRAPCFUNC = 16; // 0x10
    public static final int LUAJIT_MODE_MAX = 17;
    /**
     * Flags for luaJIT_setmode.
     */
    public static final int LUAJIT_MODE_OFF = 0;      // 0x0000
    public static final int LUAJIT_MODE_ON = 256;     // 0x0100
    public static final int LUAJIT_MODE_FLUSH = 512;  // 0x0200
    public static final int LUAJIT_MODE_MASK = 255;   // 0x00ff
// ----------------------------------------------------------------------------
// Constants from luaconf.h (Build configuration and limits)
// ----------------------------------------------------------------------------
    /**
     * Various tunable limits.
     */
    public static final int LUAI_MAXSTACK = 65500;
    public static final int LUAI_MAXCSTACK = 8000;
    public static final int LUAI_GCPAUSE = 200;
    public static final int LUAI_GCMUL = 200;
    public static final int LUA_MAXCAPTURES = 32;
    /**
     * Size of lua_Debug.short_src.
     */
    public static final int LUA_IDSIZE = 60;
}
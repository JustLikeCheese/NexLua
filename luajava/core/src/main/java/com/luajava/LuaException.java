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

import com.luajava.value.referable.LuaUserdata;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A wrapper around a Lua error message
 */
public class LuaException extends Exception {
    public final LuaError type;
    public final Throwable cause;

    public LuaException(LuaError type, String message, Throwable cause) {
        super(message);
        this.type = type;
        this.cause = cause;
    }

    public LuaException(LuaError type, String message) {
        this(type, message, null);
    }

    public LuaException(LuaError type, Exception cause) {
        this(type, cause.getMessage(), cause);
    }

    public LuaException(String message, Exception cause) {
        this(LuaError.JAVA, message, cause);
    }

    public LuaException(String message) {
        this(LuaError.JAVA, message, null);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public void printStackTrace(PrintStream stream) {
        stream.println(getMessage());
        for (StackTraceElement traceElement : getStackTrace()) {
            stream.println("\tat " + traceElement);
        }
        if (cause != null) {
            stream.print("Caused by: ");
            cause.printStackTrace(stream);
        }
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        writer.println(getMessage());
        for (StackTraceElement traceElement : getStackTrace()) {
            writer.println("\tat " + traceElement);
        }
        if (cause != null) {
            writer.print("Caused by: ");
            cause.printStackTrace(writer);
        }
    }

    public final String getType() {
        switch (type) {
            case FILE:
                return "IO Error";
            case GC:
                return "GC Error";
            case HANDLER:
                return "Handler Error";
            case MEMORY:
                return "Memory Error";
            case OK:
                return "No Error";
            case JAVA:
            case RUNTIME:
                return "Runtime Error";
            case SYNTAX:
                return "Syntax Error";
            case YIELD:
                return "Thread Error";
        }
        return "Unknown Error";
    }

    /**
     * Lua-relevant error types.
     *
     * <p>
     * Integer values of Lua error codes may vary between Lua versions.
     * This library handles the conversion from the Lua integers to interpretable Java enum values.
     * </p>
     */
    public enum LuaError {
        /**
         * a file-related error
         */
        FILE,
        /**
         * error while running a __gc metamethod
         */
        GC,
        /**
         * error while running the message handler
         */
        HANDLER,
        /**
         * memory allocation error
         */
        MEMORY,
        /**
         * no errors
         */
        OK,
        /**
         * a runtime error
         */
        RUNTIME,
        /**
         * syntax error during precompilation
         */
        SYNTAX,

        /**
         * the thread (coroutine) yields
         */
        YIELD,

        /**
         * unknown error code
         */
        UNKNOWN,

        /**
         * a Java-side error
         */
        JAVA;

        public static LuaError from(int code, boolean runtime) throws LuaException {
            if (runtime) {
                if (code == LuaConsts.LUA_OK)
                    return LuaError.OK;
                else
                    return LuaError.RUNTIME;
            } else {
                return LuaError.from(code);
            }
        }

        public static LuaError from(int code) throws LuaException {
            switch (code) {
                case LuaConsts.LUA_OK:
                    return LuaException.LuaError.OK;
                case LuaConsts.LUA_YIELD:
                    return LuaException.LuaError.YIELD;
                case LuaConsts.LUA_ERRRUN:
                    return LuaException.LuaError.RUNTIME;
                case LuaConsts.LUA_ERRSYNTAX:
                    return LuaException.LuaError.SYNTAX;
                case LuaConsts.LUA_ERRMEM:
                    return LuaException.LuaError.MEMORY;
                case LuaConsts.LUA_ERRERR:
                    return LuaException.LuaError.HANDLER;
                default:
                    return LuaError.UNKNOWN;
            }
        }
    }

    public static LuaException from(Lua L, int code, boolean runtime) throws LuaException {
        LuaError error = LuaError.from(code, runtime);
        if (LuaError.OK == error) return null;
        String message = L.toString(-1);
        LuaUserdata userdata = L.getLuaUserdata(Lua.JAVA_GLOBAL_THROWABLE);
        L.pop(1);
        if (userdata != null && userdata.isJavaObject(Throwable.class)) {
            Object throwable = userdata.toJavaObject(Throwable.class);
            if (throwable != null) {
                return new LuaException(error, message, (Throwable) throwable);
            }
        }
        return new LuaException(error, message);
    }

    public static String getType(Exception e) {
        if (e instanceof LuaException) {
            return ((LuaException) e).getType();
        }
        return e.getClass().getSimpleName();
    }

    public static String getStackTrace(Exception e) {
        if (e instanceof LuaException) {
            return e.getMessage();
        }
        return JuaAPI.getStackTrace(e);
    }

    public static String getFullMessage(Exception e) {
        return getType(e) + ": " + getStackTrace(e);
    }
}

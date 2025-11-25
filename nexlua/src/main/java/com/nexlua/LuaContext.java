package com.nexlua;
import android.content.Context;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import io.github.justlikecheese.nextoast.NexToast;

public interface LuaContext extends LuaHandler {
    ArrayList<ClassLoader> getClassLoaders();

    Lua getLua();

    LuaConfig getConfig();

    String getLuaDir();

    String getLuaPath();

    String getLuaLpath();

    String getLuaCpath();

    Context getContext();

    void initialize(Lua L);

    default void showToast(String message) {
        NexToast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    default void sendMessage(String message) {
        showToast(message);
    }

    default void sendError(String title, String error) {
        showToast(error);
    }

    @Override
    default void sendError(Exception e) {
        if (e instanceof LuaException) {
            LuaException luaException = (LuaException) e;
            sendError(luaException.getType(), e.getMessage());
        } else {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendError(e.getClass().getSimpleName(), sw.toString());
        }
    }
}

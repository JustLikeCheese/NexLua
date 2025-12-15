package com.nexlua;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.luajava.ExternalLoader;
import com.luajava.Lua;
import com.nexlua.module.LuaAssetsModule;
import com.nexlua.module.LuaStringModule;
import com.nexlua.module.LuaFileModule;
import com.nexlua.module.LuaModule;
import com.nexlua.module.LuaResourceModule;

import java.io.File;
import java.util.ArrayList;

public class LuaConfig implements ExternalLoader {
    protected static File FILES_DIR;
    protected ArrayList<LuaModule> LUA_MODULES;
    protected LuaModule welcome;
    protected LuaModule application;

    public LuaConfig(LuaContext luaContext) {
        if (FILES_DIR == null) {
            Context context = luaContext.getContext().getApplicationContext();
            FILES_DIR = context.getFilesDir();
            baseCpath = context.getApplicationInfo().nativeLibraryDir + "/lib?.so;";
            baseLpath = "";
        }
        LUA_MODULES = new ArrayList<>();
    }

    public void registerWelcome(LuaModule welcome) {
        this.welcome = welcome;
    }

    public void registerApplication(LuaModule application) {
        this.application = application;
    }

    public LuaModule register(LuaModule module) {
        LUA_MODULES.add(module);
        return module;
    }

    public LuaModule registerAssetsModule(String path, String fileName) {
        return register(new LuaAssetsModule(path, fileName));
    }

    public LuaModule registerResourceModule(String path, int resource) {
        return register(new LuaResourceModule(path, resource));
    }

    public LuaModule registerFileModule(String path, File file) {
        return register(new LuaFileModule(path, file));
    }

    public LuaModule registerStringModule(String path, String content) {
        return register(new LuaStringModule(path, content));
    }

    protected String baseCpath, baseLpath;
    public @NonNull String getBaseCpath() {
        return baseCpath;
    }

    public @NonNull String getBaseLpath() {
        return baseLpath;
    }

    public @NonNull String getLuaLpath(String luaDir) {
        return getBaseLpath() + luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;";
    }

    public @NonNull String getLuaCpath(String luaDir) {
        return getBaseCpath() + luaDir + "/lib?.so;";
    }

    public @Nullable LuaModule getModule(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return null;
        }
        for (LuaModule entry : LUA_MODULES) {
            final String entryPath = entry.getAbsolutePath();
            if (entryPath.equals(absolutePath)) {
                return entry;
            }
        }
        File file = new File(absolutePath);
        if (file.exists() && file.isFile() && file.canRead()) {
            return registerFileModule(absolutePath, file);
        }
        return null;
    }

    private static final String LUA_NAME_SEP = ".";
    private static final String DIR_SEP = File.separator;

    public @Nullable LuaModule getModule(String name, String packagePath) {
        if (name == null || packagePath == null) {
            return null;
        }
        String moduleFilePath = name.replace(LUA_NAME_SEP, DIR_SEP);
        String[] templates = packagePath.split(";", -1);
        for (String template : templates) {
            if (template.isEmpty()) {
                continue;
            }
            String filename = template.replace("?", moduleFilePath);
            LuaModule module = getModule(filename);
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    public @Nullable LuaModule getModule(LuaContext context, String name) {
        return getModule(name, context.getLuaLpath());
    }

    @Override
    public int load(Lua L, String moduleName) throws Exception {
        L.getGlobal("package");
        if (!L.isTable(-1)) {
            L.pop(1);
            return 0;
        }
        L.getField(-1, "path");
        if (!L.isString(-1)) {
            L.pop(2);
            return 0;
        }
        String packagePath = L.toString(-1);
        L.pop(1);
        LuaModule module = getModule(moduleName, packagePath);
        if (module != null) {
            return L.push(new LuaModule.Loader(module));
        }
        return 0;
    }
}

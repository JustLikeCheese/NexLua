package com.nexlua;

import com.luajava.Lua;
import com.nexlua.module.LuaAssetsModule;
import com.nexlua.module.LuaDexModule;
import com.nexlua.module.LuaFileModule;
import com.nexlua.module.LuaModule;
import com.nexlua.module.LuaResourceModule;

import java.io.File;
import java.util.ArrayList;

public class LuaConfig {
    protected static File FILES_DIR;
    protected ArrayList<LuaModule> LUA_MODULES;
    protected LuaModule welcome;
    protected LuaModule application;

    public LuaConfig(LuaContext context) {
        if (FILES_DIR == null) {
            FILES_DIR = context.getContext().getFilesDir();
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

    public LuaModule registerAssets(String path, String fileName) {
        return register(new LuaAssetsModule(path, fileName));
    }

    public LuaModule register(String path, int resource) {
        return register(new LuaResourceModule(path, resource));
    }

    public LuaModule register(String path, File file) {
        return register(new LuaFileModule(path, file));
    }

    public LuaModule register(String path, String content) {
        return register(new LuaDexModule(path, content));
    }

    public LuaModule get(String path) {
        for (LuaModule entry : LUA_MODULES) {
            final String entryPath = entry.getPath();
            if (entryPath.equals(path) || entryPath.equals(path + ".lua")) {
                return entry;
            }
        }
        return null;
    }

    public int load(LuaContext context, Lua L, String moduleName) {
        LuaModule module;
        String path = context.getLuaDir() + "/" + moduleName;
        File file1 = new File(moduleName);
        File file2 = new File(path);
        if (file1.isAbsolute()) {
            module = get(moduleName);
            if (module == null && file1.exists()) {
                module = register(file1.getAbsolutePath(), file1);
            }
        } else {
            module = get(path);
            if (module == null && file2.exists()) {
                module = register(file2.getAbsolutePath(), file2);
            }
        }
        if (module != null) {
            int nResult = module.load(L, context);
            return nResult > 0 ? nResult : 1;
        }
        return 0;
    }
}

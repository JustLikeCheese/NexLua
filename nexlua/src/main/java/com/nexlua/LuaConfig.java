package com.nexlua;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        return register(new LuaDexModule(path, content));
    }

    public @Nullable LuaModule get(@NonNull String luaDir, @NonNull String absolutePath) {
        for (LuaModule entry : LUA_MODULES) {
            final String entryPath = entry.getAbsolutePath();
            if (entryPath.equals(absolutePath)) {
                return entry;
            }
        }
        File file1 = new File(FILES_DIR, luaDir + "/" + absolutePath);
        File file2 = new File(file1.getAbsolutePath());
        if (file2.exists()) {
            return registerFileModule(absolutePath, file2);
        } else if (file1.exists()) {
            return registerFileModule(absolutePath, file1);
        } else {
            return null;
        }
    }

    public @Nullable LuaModule get(@NonNull LuaContext context, @NonNull String absolutePath) {
        return get(context.getLuaDir(), absolutePath);
    }

    public int load(LuaContext context, Lua L, String moduleName) {
        LuaModule module = get(context.getLuaDir(), moduleName);
        if (module != null) {
            int nResult = module.load(L, context);
            return nResult > 0 ? nResult : 1;
        }
        return 0;
    }
}

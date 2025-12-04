package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaContext;
import com.nexlua.LuaUtil;

public final class LuaAssetsModule extends LuaAbstractModule {
    private final String assetsName;

    public LuaAssetsModule(String path, String assetsName) {
        super(path);
        this.assetsName = assetsName;
    }

    @Override
    public int load(Lua L, LuaContext luaContext) {
        int top = L.getTop();
        try {
            L.loadStringBuffer(LuaUtil.readAsset(assetsName), "@" + assetsName);
            return L.pCall(0, -1);
        } catch (Exception e) {
            luaContext.sendError(e);
        } finally {
            L.setTop(top);
        }
        return 0;
    }
}

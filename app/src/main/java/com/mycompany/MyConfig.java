package com.mycompany;

import com.nexlua.LuaConfig;
import com.nexlua.LuaContext;

public class MyConfig extends LuaConfig {
    public MyConfig(LuaContext context) {
        super(context);
        welcome = registerAssetsModule(FILES_DIR + "/welcome.lua", "welcome.lua");
        application = registerAssetsModule(FILES_DIR + "/application.lua", "application.lua");
    }
}

package com.mycompany;

import com.nexlua.LuaApplication;
import com.nexlua.LuaConfig;

public class MyApplication extends LuaApplication {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public LuaConfig getConfig() {
        if (config == null) {
            config = new MyConfig(this);
        }
        return config;
    }
}

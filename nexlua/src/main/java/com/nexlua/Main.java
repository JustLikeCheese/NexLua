package com.nexlua;

import android.content.Intent;
import android.os.Bundle;


public class Main extends LuaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        intent.putExtra(LuaIntent.NAME, new LuaIntent(config.welcome));
        super.onCreate(savedInstanceState);
    }
}
package com.nexlua;

import android.content.Intent;
import android.os.Bundle;

public class Main extends LuaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent.getBooleanExtra("isVersionChanged", false))
                runFunc("isVersionChanged", intent.getStringExtra("newVersionName"), intent.getStringExtra("oldVersionName"));
        }
    }
}

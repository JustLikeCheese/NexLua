require "import"

import {
    "android.widget.*",
    "android.content.Intent",
    "java.lang.Object",
    "java.io.File",
    "com.androlua.LuaEditor",
    "com.nexlua.module.LuaFileModule",
    "utils"
}

activity.setTitle("NexLua+")
activity.setTheme(getTheme())
activity.setHomeAsUpEnabled(false)
activity.getWindow().setSoftInputMode(0x10)


local luadir = File("/sdcard/AndroLua")
local luatemp = File(luadir, "temp.lua")

if not luadir.exists() then
    luadir.mkdirs()
end

if not luatemp.exists() then
    writeFile(luatemp, "print('hello world')")
end

local layout = LinearLayout(activity)
local editor = LuaEditor(activity)

layout.setFitsSystemWindows(true)
layout.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
layout.addView(editor)

activity.setContentView(layout)

-- Editor API
function load()
    editor.setText(readFile(luatemp))
end

function save()
    writeFile(luatemp, editor.getText())
end

function run()
    save()
    local module = LuaFileModule(luatemp.path, luatemp)
    activity.newActivity(module)
end

-- Menu
function onCreateOptionsMenu(menu)
    menu.add("Run").setOnMenuItemClickListener(function()
        run()
    end).setShowAsAction(1)
    menu.add("Save").setOnMenuItemClickListener(function()
        toast("Saved")
        save()
    end).setShowAsAction(1)
    menu.add("Theme").setOnMenuItemClickListener(function()
        setDarkTheme(not isDarkTheme())
        activity.recreate()
        end).setShowAsAction(1)
    return true
end

-- Activity Lifecycle
function bindLifecycle(t)
    for k, v in pairs(t) do
        _G[k] = v
    end
end

bindLifecycle {
    onCreate = load,
    onRestoreInstanceState = load,

    onStop = save,
    onDestroy = save,
    onPause = save,
    onSaveInstanceState = save
}
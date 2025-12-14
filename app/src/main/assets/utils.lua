import {
    "android.os.Build",
    "android.app.AlertDialog",
    "android.widget.Toast",
    "android.content.Context"
}
android = {R=luajava.bindClass("android.R")}

local version = Build.VERSION.SDK_INT;
local hour = tonumber(os.date("%H"))

function toast(msg)
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
end

local function day()
    if version >= 21 then
        return (android.R.style.Theme_Material_Light)
    else
        return (android.R.style.Theme_Holo_Light)
    end
end

local function night()
    if version >= 21 then
        return (android.R.style.Theme_Material)
    else
        return (android.R.style.Theme_Holo)
    end
end

local prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE)
function isDarkTheme()
    return prefs.getBoolean("night_mode", false)
end

function setDarkTheme(theme)
    prefs.edit().putBoolean("night_mode", theme).apply()
end

function getTheme()
    if isDarkTheme() then
        return night()
    else
        return day()
    end
end

function onError(exception, title, message)
    local dialog = AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", null)
        .create()
    dialog.show()
    return true;
end

function writeFile(path, content)
    local file = io.open(tostring(path), "w")
    if not file then
        print("Error: Cannot write to " .. tostring(path))
        return false
    end
    file:write(tostring(content))
    file:close()
    return true
end

function readFile(path)
    local file = io.open(tostring(path), "r")
    if not file then
        print("Error: Cannot read from " .. tostring(path))
        return ""
    end
    local content = file:read("*a")
    file:close()
    return content
end
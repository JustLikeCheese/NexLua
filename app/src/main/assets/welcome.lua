require "import"
import {
    "com.nexlua.LuaUtil",
    "java.io.File",
    "android.os.Build",
    "android.os.Environment",
    "android.provider.Settings",
    "android.content.Intent",
    "android.net.Uri",
    "android.content.pm.PackageManager",
    "java.lang.String"
}

local REQUEST_CODE_RUNTIME_PERMISSION = 100
local REQUEST_CODE_MANAGE_STORAGE = 101

local cfg = activity.config
function registerAssets(fileName)
    cfg.registerAssetsModule(activity.getLuaDir(fileName), fileName)
end

local function loadMainLua()
    registerAssets("main.lua")
    registerAssets("main2.lua")
    registerAssets("utils.lua")
    activity.newActivity("main")
    activity.finish()
end

local function checkStoragePermission()
    if Build.VERSION.SDK_INT >= 30 then
        if Environment.isExternalStorageManager() then
            loadMainLua()
        else
            local intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.setData(Uri.fromParts("package", activity.getPackageName(), nil))
            activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
        end
    else
        if context.checkSelfPermission(WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED then
            loadMainLua()
        else
            local permissions = String{"android.permission.WRITE_EXTERNAL_STORAGE"}
            activity.requestPermissions(permissions, REQUEST_CODE_RUNTIME_PERMISSION)
        end
    end
end

function onRequestPermissionsResult(requestCode, permissions, grantResults)
    if requestCode == REQUEST_CODE_RUNTIME_PERMISSION then
        if grantResults and grantResults.length > 0 and grantResults[0] == PackageManager.PERMISSION_GRANTED then
            loadMainLua()
        else
            activity.showToast("存储权限被拒绝，应用可能无法正常工作")
        end
        return true
    end
    return false
end

function onActivityResult(requestCode, resultCode, data)
    if requestCode == REQUEST_CODE_MANAGE_STORAGE then
        if Build.VERSION.SDK_INT >= 30 and Environment.isExternalStorageManager() then
            loadMainLua()
        else
            activity.showToast("全部文件访问权限被拒绝")
        end
        return true
    end
    return false
end

checkStoragePermission()
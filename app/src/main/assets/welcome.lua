local LuaUtil = luajava.bindClass "com.nexlua.LuaUtil"
local File = luajava.bindClass "java.io.File"

local LuaUtil = luajava.bindClass "com.nexlua.LuaUtil"
local File = luajava.bindClass "java.io.File"

function updateFile(fileName)
    local assetPath = fileName
    local targetFile = File(this.luaDir .. "/" .. fileName)
    if not targetFile.exists() then
        print("create " .. fileName)
        LuaUtil.copyAssetsFile(assetPath, targetFile)
        return
    end
    local assetsMD5 = LuaUtil.getAssetDigest(assetPath, "MD5")
    local fileMD5 = LuaUtil.getFileDigest(targetFile, "MD5")
    if assetsMD5 ~= fileMD5 then
        print("update " .. fileName)
        LuaUtil.copyAssetsFile(assetPath, targetFile)
    else
        print(fileName .. " is up to date")
    end
end

function main()
    updateFile("main.lua")
    updateFile("test.lua")
    activity.newActivity("main")
    activity.finish()
end
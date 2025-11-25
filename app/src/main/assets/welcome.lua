print(type(this.luaDir))
print(this.luaDir)
local path = this.luaDir .. "/main.lua"
print(type(path))
print(path)

local LuaUtil = luajava.bindClass "com.nexlua.LuaUtil"
local File = luajava.bindClass "java.io.File"
LuaUtil.copyAssetsFile("main.lua", File(this.luaDir .. "/main.lua"))

dofile(this.luaDir .. "/main.lua")
local TestClass = luajava.bindClass("com.mycompany.TestClass")
local NO_RESULT = function() end
local NO_NIL = function() end
local test = function(name, func, value)
    xpcall(function()
        local result = func()
        if result == value or (value == NO_NIL and result ~= nil) then
            print(name .. " ✅" )
        else
            print(name .. " ❌:" .. result)
        end
    end, function(e)
        if value == NO_RESULT then
            print(name .. " ✅")
        else
            print(name .. " ❌:\n" .. e)
        end
    end)
end

print("LuaJava Library Testing:")

test("luajava.bindClass", function()
    return TestClass
end, NO_NIL)
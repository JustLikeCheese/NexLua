local TestClass = luajava.bindClass("com.mycompany.TestClass")
local NO_RESULT = function() end
local NO_NIL = function() end
local test = function(name, func, value)
    xpcall(function()
        local result = func()
        if result == value or (value == NO_NIL and result ~= nil) then
            print(name .. " ✅" .. result)
        else
            print(name .. " ❌:" .. result)
        end
    end, function(e)
        if value == NO_RESULT then
            print(name .. " ✅" .. result)
        else
            print(name .. " ❌:\n" .. e)
        end
    end)
end

print("LuaJava Library Testing:")

test("luajava.bindClass", function()
    return TestClass
end, NO_NIL)

test("luajava.bindMethod", function()
    local ArrayList = luajava.bindClass("java.util.ArrayList")
    local ArrayList_new = luajava.bindMethod(ArrayList, "new")

    local Object = luajava.bindClass("java.lang.Object")
    local int = luajava.bindClass("int")
    local ArrayList_add1 = luajava.bindMethod(ArrayList, "add", Object)
    local ArrayList_add2 = luajava.bindMethod(ArrayList, "add", int, Object)

    local myArray = ArrayList_new()
    ArrayList_add1(myArray, "Hello")
    ArrayList_add2(myArray, 0, "World")
    return myArray.toString()
end, NO_NIL)
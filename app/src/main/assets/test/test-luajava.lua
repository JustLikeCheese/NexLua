local TestClass = luajava.bindClass("com.mycompany.TestClass")
local Arrays = luajava.bindClass("java.util.Arrays")
local NO_RESULT = function() end
local NO_NIL = function() end
local test = function(name, func, value)
    xpcall(function()
        local result = func()
        if result == value or (value == NO_NIL and result ~= nil) then
            print(name .. " ✅" .. tostring(result))
        else
            print(name .. " ❌:" .. tostring(result))
        end
    end, function(e)
        if value == NO_RESULT then
            print(name .. " ✅" .. tostring(result))
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

test("luajava.instanceof", function()
    local ArrayList = luajava.bindClass("java.util.ArrayList")
    local arrayList = ArrayList()
    if luajava.instanceof(arrayList, ArrayList) then
        return true
    else
        return false
    end
end, NO_NIL)

test("luajava.toJavaObject", function()
    local Integer = luajava.bindClass("java.lang.Integer")
    local object = luajava.toJavaObject(1, Integer)
    return object.getClass()
end, NO_NIL)

test("luajava.toJavaArray", function()
    local Integer = luajava.bindClass("java.lang.Integer")
    local array = luajava.toJavaArray({1, 2, 3}, Integer)
    return array .. " (" .. Arrays.toString(array) .. ")"
end, NO_NIL)

test("luajava.toJavaMap", function()
    local String = luajava.bindClass("java.lang.String")
    local int = luajava.bindClass("int")
    local map = luajava.toJavaMap({
        key1 = 1,
        key2 = 2
    }, String, int)
    return map
end, NO_NIL)

test("luajava.toString", function()
    local String = luajava.bindClass("java.lang.String")
    local int = luajava.bindClass("int")
    local map = luajava.toJavaMap({
        key1 = 1,
        key2 = 2
    }, String, int)
    return luajava.toString(map)
end, NO_NIL)

test("luajava.asTable", function()
    require "dump"
    local Integer = luajava.bindClass("java.lang.Integer")
    local array = luajava.toJavaArray({1, 2, 3}, Integer)
    local t = luajava.asTable(array)
    return tostring(t) .. " (" .. dump(t) .. ")"
end, NO_NIL)

test("luajava.newInstance", function()
    return luajava.newInstance("java.util.ArrayList")
end, NO_NIL)

test("luajava.createArray", function()
    local String = luajava.bindClass("java.lang.String")
    local array = luajava.createArray(String, 10)
    for i = 0, #array - 1 do
        array[i] = "Hello " .. i
    end
    return array .. " (#array: " .. #array .. ")" .. " (" .. Arrays.toString(array) .. ")"
end, NO_NIL)

test("luajava.createProxy", function()
    local View_OnClickListener = luajava.bindClass("android.view.View$OnClickListener")
    return luajava.createProxy(View_OnClickListener, {
        onClick = function(view)
            print("Button clicked!")
        end
    })
end, NO_NIL)

test("luajava.unwrap", function()
    local View_OnClickListener = luajava.bindClass("android.view.View$OnClickListener")
    local proxy = luajava.createProxy(View_OnClickListener, {
        onClick = function(view)
            print("Button clicked!")
        end
    })
    return luajava.unwrap(proxy)
end, NO_NIL)

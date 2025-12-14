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

print("Java Class Static Method & Field Testing:")
print("Test Class: " .. TestClass)
test("Class.STATIC_FIELD", function()
    return TestClass.STATIC_FIELD
end, 1)

test("Change Class.STATIC_FIELD", function()
    TestClass.STATIC_FIELD = 2
    return TestClass.STATIC_FIELD
end, 2)
pcall(function()
    TestClass.STATIC_FIELD = 1
end)

test("Class.FINAL_STATIC_FIELD", function()
    return TestClass.FINAL_STATIC_FIELD
end, 2)

test("Change Class.FINAL_STATIC_FIELD", function()
    -- should be nil or error
    TestClass.FINAL_STATIC_FIELD = 4
    return TestClass.FINAL_STATIC_FIELD
end, NO_RESULT)

test("Class getA Getter", function()
    return TestClass.a
end, 4)

test("Class setA Setter", function()
    TestClass.a = 5
    return TestClass.a
end, 5)
pcall(function()
    TestClass.a = 4
end)

test("Class getB Getter", function()
    return TestClass.b
end, 5)

test("Class setB Setter", function()
    -- should be nil or error
    TestClass.b = 1
end, NO_RESULT)

test("Class.InnerClass", function()
    return TestClass.InnerClass
end, NO_NIL)

test("Class VarArgs Method", function()
    return TestClass.varMethod(0, "String")
end, NO_NIL)

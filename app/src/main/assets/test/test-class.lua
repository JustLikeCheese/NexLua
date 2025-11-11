local TestClass = luajava.bindClass("com.nexlua.TestClass")
local test = function(name, func, value)
    xpcall(function()
        local result = func()
        print(type(result))
        if result == value then
            print(name .. " ✅" )
        else
            print(name .. " ❌:" .. result)
        end
    end, function(e)
        print(name .. " ❌:\n" .. e)
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

test("Class.FINAL_STATIC_FIELD", function()
    return TestClass.FINAL_STATIC_FIELD
end, 3)

test("Change Class.FINAL_STATIC_FIELD", function()
    TestClass.FINAL_STATIC_FIELD = 4
    return TestClass.FINAL_STATIC_FIELD
end, nil)

test("Class getA Getter", function()
    return TestClass.a
end, 4)

test("Class setA Setter", function()
    TestClass.a = 5
    return TestClass.a
end, 5)

test("Class getB Getter", function()
    return TestClass.b
end, 5)

test("Class setB Setter", function()
    -- should be nil or error
    TestClass.b = 1
end, nil)

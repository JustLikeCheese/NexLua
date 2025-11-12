local TestClass = luajava.bindClass("com.nexlua.TestClass")
local testClass = TestClass()
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

print("Java Object Instance Method & Field Testing:")
print("Test Class: " .. TestClass)
print("Test Object: " .. testClass)

test("Object STATIC_FIELD", function()
    return testClass.STATIC_FIELD
end, 1)

test("Change Object STATIC_FIELD", function()
    testClass.STATIC_FIELD = 2
    return TestClass.STATIC_FIELD
end, 2)
pcall(function()
    TestClass.STATIC_FIELD = 1
end)

test("Object Static FINAL_STATIC_FIELD", function()
    return testClass.FINAL_STATIC_FIELD
end, 2)

test("Change Object Static FINAL_STATIC_FIELD", function()
    -- should be nil or error
    testClass.FINAL_STATIC_FIELD = 4
    return TestClass.FINAL_STATIC_FIELD
end, NO_RESULT)

test("Object Static getA Getter", function()
    return testClass.a
end, 4)

test("Object Static setA Setter", function()
    testClass.a = 5
    return TestClass.a
end, 5)
pcall(function()
    TestClass.a = 4
end)

test("Object Static getB Getter", function()
    return testClass.b
end, 5)

test("Object Static setB Setter", function()
    -- should be nil or error
    TestClass.b = 1
end, NO_RESULT)

test("Object Class InnerClass", function()
    return testClass.InnerClass
end, NO_NIL)

print("\nObject Class OK!")

local TestClass = luajava.bindClass("com.mycompany.TestClass")
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

print("Java Object Static Method & Field Testing:")
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
print("Java Object Method & Field Testing:")

test("Object Field", function()
    return testClass.field
end, 10)

test("Change Object Field", function()
    testClass.field = 20
    return testClass.field
end, 20)
pcall(function()
    testClass.field = 10
end)

test("Object Final Field", function()
    return testClass.finalField
end, 20)

test("Change Object Final Field", function()
    -- should be nil or error
    testClass.finalField = 30
    return testClass.finalField
end, NO_RESULT)

test("Object getX Getter", function()
    return testClass.x
end, 30)

test("Object setX Setter", function()
    testClass.x = 40
    return testClass.x
end, 40)
pcall(function()
    testClass.x = 30
end)

test("Object getY Getter", function()
    return testClass.y
end, 40)

test("Object Static setB Setter", function()
    -- should be nil or error
    testClass.y = 1
end, NO_RESULT)

test("Object Class InnerClass", function()
    return testClass.InnerClass
end, NO_NIL)
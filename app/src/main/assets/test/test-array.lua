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

print("Java Array Testing:")

local int = luajava.bindClass("int")
local intArray = int {1,2,3,4}
test("intArray.length: 4", function()
    return #intArray
end, 4)

test("intArray[0]", function()
    return intArray[0]
end, 1)

test("Check Error intArray[4]", function()
    return intArray[4]
end, NO_RESULT)

test("intArray[0] = 5", function()
    intArray[0] = 5
    return intArray[0]
end, 5)

test("intArray ipairs", function()
    for i, v in ipairs(intArray) do
        print("i=" .. tostring(i) .. ", v=" .. tostring(v))
    end
    return 1
end, NO_NIL)

test("intArray pairs", function()
    for k, v in pairs(intArray) do
        print("k=" .. tostring(k) .. ", v=" .. tostring(v))
    end
    return 1
end, NO_NIL)




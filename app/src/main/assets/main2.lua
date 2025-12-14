function main(...)
    local args = {...}
    if #args == 0 then
        error("You must specify a file to run")
        return
    end
    local path = tostring(args[1])
    local file = io.open(path, "r")
    if not file then
        error("Cannot read from " .. tostring(path))
        return
    end
    local content = file:read("*a")
    file:close()
    local func = load(content)
    if func then
        func()
    else
        error("Cannot load " .. tostring(path))
    end
end
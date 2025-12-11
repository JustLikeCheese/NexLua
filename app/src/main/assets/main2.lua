function main(...)
    local args  = {...}
    load(tostring(arg[0]))()
end
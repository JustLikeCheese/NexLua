#ifndef LUAREG_H
#define LUAREG_H

#include "lua.h"

typedef struct {
    const char *name;
    lua_CFunction func;
} ModuleEntry;

#define REGISTER_MODULE(module_name, module_func) \
    const ModuleEntry __entry_##module_name \
    __attribute__((used, retain, section("extra_modules"), visibility("hidden"))) = { \
        .name = #module_name, \
        .func = module_func \
    }

extern const ModuleEntry __start_extra_modules; // NOLINT(*-reserved-identifier)
extern const ModuleEntry __stop_extra_modules; // NOLINT(*-reserved-identifier)

#endif //LUAREG_H

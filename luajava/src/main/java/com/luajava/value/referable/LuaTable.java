/*
 * Copyright (C) 2025 JustLikeCheese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.luajava.value.referable;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LuaTable extends AbstractLuaRefValue {
    private LuaProxy luaProxy;

    public LuaTable(Lua L) {
        super(L, LuaType.TABLE);
    }

    public LuaTable(Lua L, int index) {
        super(L, LuaType.TABLE, index);
    }

    private LuaTable(int ref, Lua L) {
        super(ref, L, LuaType.TABLE);
    }

    public static LuaTable fromRef(Lua L, int ref) {
        return new LuaTable(ref, L);
    }

    @Override
    public boolean isTable() {
        return true;
    }

    @Override
    public LuaTable checkTable() {
        return this;
    }

    @Override
    public Object toJavaObject() throws LuaException {
        return toJavaMap();
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class || clazz == LuaValue.class || clazz == LuaTable.class
                || clazz.isArray() || List.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)
                || clazz.isInterface();
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaTable.class)
            return this;
        else if (clazz.isArray())
            return toJavaArray();
        else if (clazz.isAssignableFrom(Collection.class))
            return toJavaList();
        else if (clazz == Object.class || Map.class.isAssignableFrom(clazz))
            return toJavaMap();
        else if (clazz.isInterface())
            return LuaProxy.newInstance(this, clazz, Lua.Conversion.SEMI).toProxy();
        return super.toJavaObject(clazz);
    }
}

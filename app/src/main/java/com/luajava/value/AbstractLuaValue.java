package com.luajava.value;

import com.luajava.Lua;
import com.luajava.LuaConsts;
import com.luajava.Nullable;

import java.nio.Buffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLuaValue implements LuaValue {

    protected final Lua L;
    protected final LuaType TYPE;

    public AbstractLuaValue(Lua L, LuaType type) {
        this.L = L;
        this.TYPE = type;
    }

    @Override
    public Lua state() {
        return L;
    }

    @Override
    public void push() {
        push(L);
    }

    @Override
    public abstract void push(Lua L);

    @Override
    public @Nullable LuaValue copyTo(Lua L) {
        return this;
    }

    public LuaType type() {
        return TYPE;
    }

    public String typeName() {
        return TYPE.toString();
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(key, Lua.Conversion.SEMI);
    }

    @Override
    public boolean containsKey(Object key, Lua.Conversion degree) {
        Lua L = state();
        push(L);
        L.push(key, degree);
        L.getTable(-2);
        boolean result = !L.isNil(-1);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue get(Object key) {
        return get(key, Lua.Conversion.SEMI);
    }

    @Override
    public LuaValue get(Object key, Lua.Conversion degree) {
        int top = L.getTop();
        try {
            push();
            L.push(key, degree);
            L.getTable(-2);
            return L.get();
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public void set(Object key, Object value) {
        set(key, value, Lua.Conversion.SEMI);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree) {
        set(key, value, degree, degree);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) {
        int top = L.getTop();
        try {
            push();
            L.push(key, degree1);
            L.push(value, degree2);
            L.setTable(-3);
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public LuaValue rawget(Object key) {
        return rawget(key, Lua.Conversion.SEMI);
    }

    @Override
    public LuaValue rawget(Object key, Lua.Conversion degree) {
        int top = L.getTop();
        try {
            push();
            L.push(key, degree);
            L.rawGet(-2);
            return L.get();
        } finally {
            L.setTop(top);
        }
    }


    @Override
    public void rawset(Object key, Object value) {
        rawset(key, value, Lua.Conversion.SEMI);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree) {
        rawset(key, value, degree, degree);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) {
        int top = L.getTop();
        try {
            push();
            L.push(key, degree1);
            L.push(value, degree2);
            L.rawSet(-3);
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public void pairs(LuaPairsIterator iterator) {
        int top = L.getTop();
        try {
            push();
            L.pushNil();
            while (L.next(-2)) {
                LuaValue key = L.get(-2);
                LuaValue value = L.get(-1);
                boolean shouldContinue = iterator.iterate(key, value);
                L.pop(1);
                if (!shouldContinue) {
                    L.pop(1); // pop key
                    break;
                }
            }
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public void ipairs(LuaIpairsIterator iterator) {
        int top = L.getTop();
        try {
            push();
            long index = 1;
            while (true) {
                L.push(index);
                L.getTable(-2);
                if (L.isNil(-1)) {
                    L.pop(1); // pop nil value
                    break;
                }
                LuaValue value = L.get(-1);
                boolean shouldContinue = iterator.iterate(index, value);
                L.pop(1); // pop value
                if (!shouldContinue) {
                    break;
                }
                index++;
            }
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public Map<LuaValue, LuaValue> toMap() {
        Map<LuaValue, LuaValue> map = new LinkedHashMap<>();
        pairs((key, value) -> {
            map.put(key, value);
            return true; // continue iteration
        });
        return map;
    }

    @Override
    public List<LuaValue> toList() {
        List<LuaValue> list = new java.util.ArrayList<>();
        ipairs((index, value) -> {
            while (list.size() < index) {
                list.add(null);
            }
            list.set((int) (index - 1), value);
            return true;
        });
        return list;
    }

    @Override
    public long length() {
        push();
        long length = L.rawLength(-1);
        L.pop(1);
        return length;
    }

    @Override
    public LuaValue getMetatable() {
        push();
        if (L.getMetatable(-1)) {
            LuaValue value = L.get();
            L.pop(1);
            return value;
        }
        L.pop(1);
        return null;
    }

    @Override
    public void setMetatable(String tname) {
        push();
        L.getRegisteredMetatable(tname);
        L.setMetatable(-2);
        L.pop(1);
    }

    @Override
    public LuaValue callMetatable(String method) {
        push();
        if (L.callMeta(-1, method)) {
            LuaValue value = L.get();
            L.pop(1);
            return value;
        }
        L.pop(1);
        return null;
    }

    @Override
    public boolean rawEqual(Object value) {
        return rawEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean rawEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.rawEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean equal(Object value) {
        return equal(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean equal(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.equal(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThan(Object value) {
        return lessThan(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean lessThan(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.lessThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThanOrEqual(Object value) {
        return lessThanOrEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean lessThanOrEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.lessThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThan(Object value) {
        return greaterThan(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean greaterThan(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.greaterThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThanOrEqual(Object value) {
        return greaterThanOrEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean greaterThanOrEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.greaterThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public LuaValue[] call() {
        return call((Object) null);
    }

    @Override
    public LuaValue[] call(Object... args) {
        return call(Lua.Conversion.SEMI, args);
    }

    @Override
    public LuaValue[] call(Lua.Conversion degree, Object... args) {
        int top = L.getTop();
        try {
            push();
            int argsLength = args == null ? 0 : args.length;
            if (args != null) {
                for (Object arg : args) {
                    L.push(arg, degree);
                }
            }
            int oldTop = L.getTop();
            L.call(argsLength, LuaConsts.LUA_MULTRET);
            int resultLength = L.getTop() - oldTop;
            LuaValue[] result = new LuaValue[argsLength];
            for (int i = 0; i < resultLength; i++) {
                result[i] = L.get();
            }
            return result;
        } finally {
            L.setTop(top);
        }
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public boolean isNil() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isLightUserdata() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isTable() {
        return false;
    }

    @Override
    public boolean isCFunction() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public boolean isUserdata() {
        return false;
    }

    @Override
    public boolean isThread() {
        return false;
    }

    @Override
    public boolean toBoolean() {
        push();
        boolean bool = L.toBoolean(-1);
        L.pop(1);
        return bool;
    }

    @Override
    public long toInteger() {
        push();
        long integer = L.toInteger(-1);
        L.pop(1);
        return integer;
    }

    @Override
    public double toNumber() {
        push();
        double number = L.toNumber(-1);
        L.pop(1);
        return number;
    }

    @Override
    public Object toJavaObject() {
        push();
        Object javaObject = L.toJavaObject(-1);
        L.pop(1);
        return javaObject;
    }

    @Override
    public String toString() {
        push();
        String str = L.toString(-1);
        L.pop(1);
        return str;
    }

    @Override
    public Buffer toBuffer() {
        push();
        Buffer buffer = L.toDirectBuffer(-1);
        L.pop(1);
        return buffer;
    }

    @Override
    public Buffer dump() {
        return null;
    }
}
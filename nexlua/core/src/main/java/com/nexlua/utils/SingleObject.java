package com.nexlua.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleObject<T> {
    private final T object;
    private final AtomicBoolean locked = new AtomicBoolean(false);

    public SingleObject(T object) {
        this.object = object;
    }

    public static @NonNull <T> SingleObject<T> wrap(T object) {
        return new SingleObject<>(object);
    }

    public static @Nullable <T> SingleObject<T> wrapNonNull(T object) {
        if (object == null) {
            return null;
        }
        return new SingleObject<>(object);
    }

    public T get() {
        return object;
    }

    public boolean isLocked() {
        return locked.get();
    }

    public boolean lock() {
        return locked.compareAndSet(false, true);
    }

    public void unlock() {
        locked.set(false);
    }
}
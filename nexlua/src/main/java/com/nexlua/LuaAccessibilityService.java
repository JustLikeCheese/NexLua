package com.nexlua;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.InputMethod;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.luajava.value.referable.LuaFunction;

@SuppressLint("AccessibilityPolicy")
public class LuaAccessibilityService extends AccessibilityService implements LuaContextUtils {
    protected static LuaAccessibilityService instance;
    public static LuaFunction onCreate;
    public static LuaFunction onStartCommand;
    public static LuaFunction onServiceConnected;
    public static LuaFunction onUnbind;
    public static LuaFunction onRebind;
    public static LuaFunction onTaskRemoved;
    public static LuaFunction onDestroy;
    public static LuaFunction onAccessibilityEvent;
    public static LuaFunction onInterrupt;
    public static LuaFunction onKeyEvent;
    public static LuaFunction onGesture;
    public static LuaFunction onGestureId;
    public static LuaFunction onMotionEvent;
    public static LuaFunction onCreateInputMethod;
    public static LuaFunction onSystemActionsChanged;
    public static LuaFunction onConfigurationChanged;
    public static LuaFunction onLowMemory;
    public static LuaFunction onTrimMemory;

    public static LuaAccessibilityService getInstance() {
        return instance;
    }

    // Service Lifecycle
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        onLuaEvent(onCreate);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Integer result = (Integer) onLuaEvent(onStartCommand, Integer.class, intent, flags, startId);
        return result != null ? result : super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        onLuaEvent(onServiceConnected, this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Boolean result = (Boolean) onLuaEvent(onUnbind, Boolean.class, intent);
        return result != null ? result : super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        onLuaEvent(onRebind, intent);
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onLuaEvent(onTaskRemoved, rootIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        instance = null;
        onLuaEvent(onDestroy);
        super.onDestroy();
    }

    // Accessibility Core
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        onLuaEvent(onAccessibilityEvent, event);
    }

    @Override
    public void onInterrupt() {
        onLuaEvent(onInterrupt);
    }

    // Input & Gestures
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Boolean result = (Boolean) onLuaEvent(onKeyEvent, Boolean.class, event);
        return result != null ? result : super.onKeyEvent(event);
    }

    @Override
    public boolean onGesture(@NonNull AccessibilityGestureEvent gestureEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Boolean result = (Boolean) onLuaEvent(onGesture, Boolean.class, gestureEvent.getGestureId());
            return result != null ? result : super.onGesture(gestureEvent);
        }
        return super.onGesture(gestureEvent);
    }

    @Override
    protected boolean onGesture(int gestureId) {
        Boolean result = (Boolean) onLuaEvent(onGestureId, Boolean.class, gestureId);
        return result != null ? result : super.onGesture(gestureId);
    }

    @Override
    public void onMotionEvent(@NonNull MotionEvent event) {
        onLuaEvent(onMotionEvent, event);
        super.onMotionEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public @NonNull InputMethod onCreateInputMethod() {
        InputMethod result = (InputMethod) onLuaEvent(onCreateInputMethod, InputMethod.class);
        return result != null ? result : super.onCreateInputMethod();
    }

    // System & Configuration
    @Override
    public void onSystemActionsChanged() {
        onLuaEvent(onSystemActionsChanged);
        super.onSystemActionsChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        onLuaEvent(onConfigurationChanged, this, newConfig);
    }

    // Memory
    @Override
    public void onLowMemory() {
        onLuaEvent(onLowMemory);
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        onLuaEvent(onTrimMemory, level);
        super.onTrimMemory(level);
    }
}
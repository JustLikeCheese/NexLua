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

@SuppressLint("AccessibilityPolicy")
public class LuaAccessibilityService extends AccessibilityService {
    protected static LuaAccessibilityService instance;
    protected static Callback callback;

    public interface Callback {
        // Service Lifecycle
        void onCreate();

        void onStartCommand(Intent intent, int flags, int startId);

        void onServiceConnected(LuaAccessibilityService service);

        void onUnbind(Intent intent);

        void onRebind(Intent intent);

        void onTaskRemoved(Intent rootIntent);

        void onDestroy();

        // Accessibility Core
        void onAccessibilityEvent(AccessibilityEvent event);

        void onInterrupt();

        // Input & Gestures
        boolean onKeyEvent(KeyEvent event);

        boolean onGesture(int gestureId); // For new API mapping

        boolean onGestureId(int gestureId); // For legacy API mapping

        void onMotionEvent(MotionEvent event);

        InputMethod onCreateInputMethod();

        // System & Configuration
        void onSystemActionsChanged();

        void onConfigurationChanged(LuaAccessibilityService luaAccessibilityService, Configuration newConfig);

        // Memory
        void onLowMemory();

        void onTrimMemory(int level);
    }

    public static LuaAccessibilityService getInstance() {
        return instance;
    }

    public static void setCallback(Callback callback) {
        LuaAccessibilityService.callback = callback;
    }

    // Service Lifecycle
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (callback != null) {
            callback.onCreate();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (callback != null) {
            callback.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (callback != null) {
            callback.onServiceConnected(this);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (callback != null) {
            callback.onUnbind(intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        if (callback != null) {
            callback.onRebind(intent);
        }
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (callback != null) {
            callback.onTaskRemoved(rootIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        instance = null;
        if (callback != null) {
            callback.onDestroy();
        }
        super.onDestroy();
    }

    // Accessibility Core
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (callback != null) {
            callback.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {
        if (callback != null) {
            callback.onInterrupt();
        }
    }

    // Input & Gestures
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (callback != null) {
            return callback.onKeyEvent(event);
        }
        return super.onKeyEvent(event);
    }

    @Override
    public boolean onGesture(AccessibilityGestureEvent gestureEvent) {
        if (callback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return callback.onGesture(gestureEvent.getGestureId());
            }
        }
        return super.onGesture(gestureEvent);
    }

    @Override
    protected boolean onGesture(int gestureId) {
        if (callback != null) {
            return callback.onGestureId(gestureId);
        }
        return super.onGesture(gestureId);
    }

    @Override
    public void onMotionEvent(MotionEvent event) {
        if (callback != null) {
            callback.onMotionEvent(event);
        }
        super.onMotionEvent(event);
    }

    @Override
    public InputMethod onCreateInputMethod() {
        if (callback != null) {
            return callback.onCreateInputMethod();
        }
        return super.onCreateInputMethod();
    }

    // System & Configuration
    @Override
    public void onSystemActionsChanged() {
        if (callback != null) {
            callback.onSystemActionsChanged();
        }
        super.onSystemActionsChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (callback != null) {
            callback.onConfigurationChanged(this, newConfig);
        }
    }

    // Memory
    @Override
    public void onLowMemory() {
        if (callback != null) {
            callback.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (callback != null) {
            callback.onTrimMemory(level);
        }
        super.onTrimMemory(level);
    }
}
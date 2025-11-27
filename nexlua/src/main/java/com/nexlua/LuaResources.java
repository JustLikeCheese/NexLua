package com.nexlua;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LuaResources extends Resources {
    // Skip System ID (0x01) and R.java ID (0x7f)
    private int mIdCounter = 0x7f050000;

    // Use SparseArray for better performance and memory optimization
    private final SparseArray<CharSequence> mTextMap = new SparseArray<>();
    private final SparseArray<Drawable> mDrawableMap = new SparseArray<>();
    private final SparseIntArray mColorMap = new SparseIntArray();
    private final SparseArray<ColorStateList> mColorStateListMap = new SparseArray<>();
    private final SparseArray<String[]> mTextArrayMap = new SparseArray<>();
    private final SparseArray<int[]> mIntArrayMap = new SparseArray<>();
    private final SparseArray<Typeface> mTypefaceMap = new SparseArray<>();
    private final SparseIntArray mIntMap = new SparseIntArray();
    private final SparseArray<Float> mDimensionMap = new SparseArray<>();
    private final SparseBooleanArray mBooleanMap = new SparseBooleanArray();

    private final Map<String, Integer> mKeyToIdMap = new HashMap<>();
    private Resources mSuperResources;

    public LuaResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
    }

    public void setSuperResources(Resources superRes) {
        mSuperResources = superRes;
    }

    public Integer get(String key) {
        synchronized (this) {
            return mKeyToIdMap.get(key);
        }
    }

    public int put(String key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        synchronized (this) {
            int id = mIdCounter++;
            mKeyToIdMap.put(key, id);
            if (value instanceof Drawable) {
                mDrawableMap.put(id, (Drawable) value);
            } else if (value instanceof String) {
                mTextMap.put(id, (String) value);
            } else if (value instanceof CharSequence) {
                mTextMap.put(id, (CharSequence) value);
            } else if (value instanceof Number) {
                int intVal = ((Number) value).intValue();
                float floatVal = ((Number) value).floatValue();
                mColorMap.put(id, intVal);
                mIntMap.put(id, intVal);
                mDimensionMap.put(id, floatVal);
            } else if (value instanceof ColorStateList) {
                mColorStateListMap.put(id, (ColorStateList) value);
            } else if (value instanceof String[]) {
                mTextArrayMap.put(id, (String[]) value);
            } else if (value instanceof int[]) {
                mIntArrayMap.put(id, (int[]) value);
            } else if (value instanceof Boolean) {
                mBooleanMap.put(id, (Boolean) value);
            } else if (value instanceof Typeface) {
                mTypefaceMap.put(id, (Typeface) value);
            } else {
                throw new IllegalArgumentException("Unsupported resource type: " + value.getClass().getName());
            }
            return id;
        }
    }

    public void remove(String key) {
        synchronized (this) {
            Integer id = mKeyToIdMap.remove(key);
            if (id != null) {
                mTextMap.delete(id);
                mDrawableMap.delete(id);
                mColorMap.delete(id);
                mColorStateListMap.delete(id);
                mTextArrayMap.delete(id);
                mIntArrayMap.delete(id);
                mTypefaceMap.delete(id);
                mIntMap.delete(id);
                mDimensionMap.delete(id);
                mBooleanMap.delete(id);
            }
        }
    }

    public void clear() {
        synchronized (this) {
            mTextMap.clear();
            mDrawableMap.clear();
            mColorMap.clear();
            mColorStateListMap.clear();
            mTextArrayMap.clear();
            mIntArrayMap.clear();
            mTypefaceMap.clear();
            mIntMap.clear();
            mDimensionMap.clear();
            mBooleanMap.clear();
            mKeyToIdMap.clear();
        }
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        synchronized (this) {
            CharSequence text = mTextMap.get(id);
            if (text != null) return text;
        }
        return mSuperResources.getText(id);
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        synchronized (this) {
            CharSequence text = mTextMap.get(id);
            if (text != null) return text;
        }
        return mSuperResources.getText(id, def);
    }

    @Override
    public String getString(int id) throws NotFoundException {
        return getText(id).toString();
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        String raw = getString(id);
        return String.format(raw, formatArgs);
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        return mSuperResources.getQuantityText(id, quantity);
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return mSuperResources.getQuantityString(id, quantity);
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        return mSuperResources.getQuantityString(id, quantity, formatArgs);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        synchronized (this) {
            String[] array = mTextArrayMap.get(id);
            if (array != null) return array;
        }
        return mSuperResources.getTextArray(id);
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        synchronized (this) {
            String[] array = mTextArrayMap.get(id);
            if (array != null) return array;
        }
        return mSuperResources.getStringArray(id);
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        synchronized (this) {
            int[] array = mIntArrayMap.get(id);
            if (array != null) return array;
        }
        return mSuperResources.getIntArray(id);
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        return mSuperResources.obtainTypedArray(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        synchronized (this) {
            Float dim = mDimensionMap.get(id);
            if (dim != null) return dim;
        }
        return mSuperResources.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        synchronized (this) {
            Float dim = mDimensionMap.get(id);
            if (dim != null) return dim.intValue();
        }
        return mSuperResources.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        synchronized (this) {
            Float dim = mDimensionMap.get(id);
            if (dim != null) return (int) (dim + 0.5f);
        }
        return mSuperResources.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
        return mSuperResources.getFraction(id, base, pbase);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        synchronized (this) {
            Drawable d = mDrawableMap.get(id);
            if (d != null) return d;
        }
        return mSuperResources.getDrawable(id);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        synchronized (this) {
            Drawable d = mDrawableMap.get(id);
            if (d != null) return d;
        }
        return mSuperResources.getDrawable(id, theme);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return mSuperResources.getDrawableForDensity(id, density);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        return mSuperResources.getDrawableForDensity(id, density, theme);
    }

    @Override
    public Movie getMovie(int id) throws NotFoundException {
        return mSuperResources.getMovie(id);
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        synchronized (this) {
            if (mColorMap.indexOfKey(id) >= 0) {
                return mColorMap.get(id);
            }
        }
        return mSuperResources.getColor(id);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public int getColor(int id, Theme theme) throws NotFoundException {
        synchronized (this) {
            if (mColorMap.indexOfKey(id) >= 0) {
                return mColorMap.get(id);
            }
        }
        return mSuperResources.getColor(id, theme);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        synchronized (this) {
            ColorStateList csl = mColorStateListMap.get(id);
            if (csl != null) return csl;
            if (mColorMap.indexOfKey(id) >= 0) {
                return ColorStateList.valueOf(mColorMap.get(id));
            }
        }
        return mSuperResources.getColorStateList(id);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
        synchronized (this) {
            ColorStateList csl = mColorStateListMap.get(id);
            if (csl != null) return csl;
            if (mColorMap.indexOfKey(id) >= 0) {
                return ColorStateList.valueOf(mColorMap.get(id));
            }
        }
        return mSuperResources.getColorStateList(id, theme);
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        synchronized (this) {
            if (mBooleanMap.indexOfKey(id) >= 0) {
                return mBooleanMap.get(id);
            }
        }
        return mSuperResources.getBoolean(id);
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        synchronized (this) {
            if (mIntMap.indexOfKey(id) >= 0) {
                return mIntMap.get(id);
            }
        }
        return mSuperResources.getInteger(id);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public Typeface getFont(int id) throws NotFoundException {
        synchronized (this) {
            Typeface tf = mTypefaceMap.get(id);
            if (tf != null) return tf;
        }
        return mSuperResources.getFont(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return mSuperResources.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return mSuperResources.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        return mSuperResources.getXml(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        return mSuperResources.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        return mSuperResources.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        return mSuperResources.openRawResourceFd(id);
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        mSuperResources.getValue(id, outValue, resolveRefs);
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        mSuperResources.getValueForDensity(id, density, outValue, resolveRefs);
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        mSuperResources.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return mSuperResources.obtainAttributes(set, attrs);
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
        return mSuperResources.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        return mSuperResources.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        return mSuperResources.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        return mSuperResources.getResourceEntryName(resid);
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        return mSuperResources.getIdentifier(name, defType, defPackage);
    }

    @Override
    public Configuration getConfiguration() {
        return mSuperResources.getConfiguration();
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return mSuperResources.getDisplayMetrics();
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
        mSuperResources.parseBundleExtra(tagName, attrs, outBundle);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle) throws IOException, XmlPullParserException {
        mSuperResources.parseBundleExtras(parser, outBundle);
    }
}
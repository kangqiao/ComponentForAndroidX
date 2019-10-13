package com.zp.androidx.base.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;


import androidx.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by zhaopan on 2019-07-05.
 * SharedPreferences的单例模式，支持不同的命名
 */
public class SPSingleton {
    private static volatile HashMap<String, SPSingleton> instanceMap = new HashMap<>();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //是否是执行apply的模式，false表示为commit保存数据
    private boolean isApplyMode = false;
    private static final String DEFAULT = "default";

    private SPSingleton(Context context, String name) {
        Context ctx = context instanceof Application ? context: context.getApplicationContext();
        if (DEFAULT.equals(name)) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        } else {
            sharedPreferences = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        editor = sharedPreferences.edit();
    }

    public static SPSingleton get(Context context, String name) {
        if (instanceMap.get(name) == null) {
            synchronized (SPSingleton.class) {
                if (instanceMap.get(name) == null) {
                    instanceMap.put(name, new SPSingleton(context, name));
                }
            }
        }
        //这里每次get操作时强制将保存模式改为commit的方式
        instanceMap.get(name).isApplyMode = false;
        return instanceMap.get(name);
    }

    public static SPSingleton get(Context context) {
        return get(context, DEFAULT);
    }

    // 如果用apply模式的话，得要先调用这个方法，
    // 然后链式调用后续的存储方法，最后以commit方法结尾
    public SPSingleton applyMode() {
        isApplyMode = true;
        return this;
    }

    public void commit() {
        editor.commit();
    }

    public SPSingleton putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        save();
        return this;
    }

    private void save() {
        if (isApplyMode) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }


    public SPSingleton putFloat(String key, float value) {
        editor.putFloat(key, value);
        save();
        return this;
    }

    public float getFloat(String key, float defValue){
        return sharedPreferences.getFloat(key, defValue);
    }

    public SPSingleton putLong(String key, long value) {
        editor.putLong(key, value);
        save();
        return this;
    }

    public long getLong(String key, long defValue){
        return sharedPreferences.getLong(key, defValue);
    }

    public SPSingleton putInt(String key, int value) {
        editor.putInt(key, value);
        save();
        return this;
    }

    public SPSingleton putString(String key, String value) {
        editor.putString(key, value);
        save();
        return this;
    }

    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public void delete(String key) {
        editor.remove(key);
        save();
    }

    public void deleteAll() {
        editor.clear();
        save();
    }

    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public void saveKeyBytes(String alias, byte[] content) {
        putString(alias, Base64.encodeToString(content, Base64.DEFAULT));
    }

    @Nullable
    public byte[] getKeyBytes(String alias) {
        String value = getString(alias, "");
        if (!TextUtils.isEmpty(value)) {
            return Base64.decode(value, Base64.DEFAULT);
        }
        return null;
    }

    public boolean containsAlias(String alias) {
        return contains(alias);
    }

    public void remove(String alias) {
        delete(alias);
    }
}
package com.nova.trojan.trojannova.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;


public final class CacheStorage {

    static CacheStorage instance = null;
    private final Context context;

    public static void setupCache(Context context) {
        if (instance == null) {
            instance = new CacheStorage(context);
        }
    }

    public static <E extends Object> E get(String key, Callable<E> func) {
        if (instance == null) return null;

        E value = instance.getObject(key);

        if (value == null)
            try {
                value = func.call();
                instance.setObject(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return value;
    }


    private CacheStorage(Context context) {
        this.context = context;
    }

    private <E extends Object> E getObject(String key) {

        File cacheFile = new File(context.getCacheDir().getAbsolutePath(), key);
        if (!cacheFile.exists()) return null;

        try {
            FileInputStream fis = new FileInputStream(cacheFile.getAbsolutePath());
            ObjectInputStream ois = new ObjectInputStream(fis);
            E obj = (E) ois.readObject();
            ois.close();
            fis.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private <E> void setObject(String key, E object) {
        String filename = new File(context.getCacheDir().getAbsolutePath(), key).getAbsolutePath();
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

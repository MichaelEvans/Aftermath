package org.michaelevans.aftermath;

import android.content.Intent;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Aftermath {

    public static final String SUFFIX = "$$Aftermath";
    public static final String ANDROID_PREFIX = "android.";
    public static final String JAVA_PREFIX = "java.";
    private static final String TAG = "Aftermath";
    private static final IOnActivityForResult<Object> NO_OP = null;
    private static boolean debug = false;
    private static final Map<Class<?>, IOnActivityForResult<Object>> AFTERMATHS = new LinkedHashMap<>();

    public static void onActivityResult(Object target, int requestCode, int resultCode, Intent data) {
        Class<?> targetClass = target.getClass();
        if (debug) {
            Log.d(TAG, "Looking up aftermath for " + targetClass.getName());
        }
        IOnActivityForResult<Object> aftermath = findActivityForResultForClass(targetClass);
        if (aftermath != NO_OP) {
            aftermath.onActivityResult(target, requestCode, resultCode, data);
        }
    }

    /**
     * Searches for $$Aftermath class for the given instance, cached for efficiency.
     *
     * @param cls Source class to find a matching $$Aftermath class for
     * @return $$Aftermath class instance
     */
    private static IOnActivityForResult<Object> findActivityForResultForClass(Class<?> cls) {
        IOnActivityForResult<Object> aftermath = AFTERMATHS.get(cls);
        if (aftermath != null) {
            if (debug) Log.d(TAG, "HIT: Cached in aftermath map.");
            return aftermath;
        }
        String clsName = cls.getName();
        if (clsName.startsWith(ANDROID_PREFIX) || clsName.startsWith(JAVA_PREFIX)) {
            if (debug) {
                Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
            }
            return NO_OP;
        }
        //noinspection TryWithIdenticalCatches
        try {
            Class<?> aftermathClass = Class.forName(clsName + SUFFIX);
            //noinspection unchecked
            aftermath = (IOnActivityForResult<Object>) aftermathClass.newInstance();
            if (debug) {
                Log.d(TAG, "HIT: Class loaded aftermath class.");
            }
        } catch (ClassNotFoundException e) {
            if (debug) {
                Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
            }
            aftermath = findActivityForResultForClass(cls.getSuperclass());
        } catch (InstantiationException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        }
        AFTERMATHS.put(cls, aftermath);
        return aftermath;
    }

    /**
     * DO NOT USE. Exposed for generated classes' use.
     */
    public interface IOnActivityForResult<T> {
        @SuppressWarnings("unused")
        void onActivityResult(final T target, int requestCode, int resultCode, Intent data);
    }
}

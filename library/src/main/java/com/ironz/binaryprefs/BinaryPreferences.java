package com.ironz.binaryprefs;

import com.ironz.binaryprefs.cache.CacheProvider;
import com.ironz.binaryprefs.events.EventBridge;
import com.ironz.binaryprefs.exception.ExceptionHandler;
import com.ironz.binaryprefs.file.FileAdapter;
import com.ironz.binaryprefs.serialization.Serializer;
import com.ironz.binaryprefs.serialization.SerializerFactory;
import com.ironz.binaryprefs.serialization.persistable.Persistable;
import com.ironz.binaryprefs.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BinaryPreferences implements Preferences {

    private final FileAdapter fileAdapter;
    private final ExceptionHandler exceptionHandler;
    private final EventBridge eventsBridge;
    private final CacheProvider cacheProvider;
    private final TaskExecutor taskExecutor;
    private final SerializerFactory serializerFactory;

    private final Class lock = BinaryPreferences.class;

    @SuppressWarnings("WeakerAccess")
    public BinaryPreferences(FileAdapter fileAdapter,
                             ExceptionHandler exceptionHandler,
                             EventBridge eventsBridge,
                             CacheProvider cacheProvider,
                             TaskExecutor taskExecutor,
                             SerializerFactory serializerFactory) {
        this.fileAdapter = fileAdapter;
        this.exceptionHandler = exceptionHandler;
        this.eventsBridge = eventsBridge;
        this.cacheProvider = cacheProvider;
        this.taskExecutor = taskExecutor;
        this.serializerFactory = serializerFactory;
        defineCache();
    }

    private void defineCache() {
        synchronized (lock) {
            for (String name : fileAdapter.names()) {
                try {
                    byte[] bytes = fileAdapter.fetch(name);
                    cacheProvider.put(name, bytes);
                } catch (Exception e) {
                    exceptionHandler.handle(e, name);
                }
            }
        }
    }

    @Override
    public Map<String, Object> getAll() {
        synchronized (lock) {
            try {
                return getAllInternal();
            } catch (Exception e) {
                exceptionHandler.handle(e, "getAll method");
            }
            return new HashMap<>();
        }
    }

    @Override
    public String getString(String key, String defValue) {
        synchronized (lock) {
            try {
                return getStringInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        synchronized (lock) {
            try {
                return getStringSetInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValues;
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        synchronized (lock) {
            try {
                return getIntInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        synchronized (lock) {
            try {
                return getLongInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        synchronized (lock) {
            try {
                return getFloatInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        synchronized (lock) {
            try {
                return getBooleanInternal(key);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public <T extends Persistable> T getPersistable(Class<T> clazz, String key, T defValue) {
        synchronized (lock) {
            try {
                return getPersistableInternal(key, clazz);
            } catch (Exception e) {
                exceptionHandler.handle(e, key);
            }
            return defValue;
        }
    }

    @Override
    public boolean contains(String key) {
        synchronized (lock) {
            return containsInternal(key);
        }
    }

    @Override
    public PreferencesEditor edit() {
        synchronized (lock) {
            return new BinaryPreferencesEditor(this, fileAdapter, exceptionHandler, eventsBridge, taskExecutor, serializerFactory, lock);
        }
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (lock) {
            eventsBridge.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (lock) {
            eventsBridge.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private Map<String, Object> getAllInternal() {
        Map<String, Object> map = new HashMap<>();
        for (String key : cacheProvider.keys()) {
            byte[] bytes = cacheProvider.get(key);
            Serializer<Object> clazz = serializerFactory.getByFlag(bytes[0]);
            map.put(key, clazz.deserialize(bytes));
        }
        return map;
    }

    private String getStringInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(String.class.getName());
        return (String) serializer.deserialize(bytes);
    }

    private Set<String> getStringSetInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Set.class.getName());
        //noinspection unchecked
        return (Set<String>) serializer.deserialize(bytes);
    }

    private int getIntInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Integer.class.getName());
        return (Integer) serializer.deserialize(bytes);
    }

    private long getLongInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Long.class.getName());
        return (Long) serializer.deserialize(bytes);
    }

    private float getFloatInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Float.class.getName());
        return (Float) serializer.deserialize(bytes);
    }

    private boolean getBooleanInternal(String key) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Boolean.class.getName());
        return (Boolean) serializer.deserialize(bytes);
    }

    private <T extends Persistable> T getPersistableInternal(String key, Class<T> clazz) {
        byte[] bytes = cacheProvider.get(key);
        Serializer<Object> serializer = serializerFactory.getByClassType(Persistable.class.getName());
        //noinspection unchecked
        return (T) serializer.deserialize(bytes);
    }

    private boolean containsInternal(String key) {
        return cacheProvider.contains(key);
    }
}
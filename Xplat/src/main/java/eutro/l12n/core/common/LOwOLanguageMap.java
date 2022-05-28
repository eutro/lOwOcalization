package eutro.l12n.core.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eutro.l12n.api.LOwOcalizationAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ExecutionException;

public class LOwOLanguageMap implements Map<String, String> {

    private final Cache<Object, String> cache = CacheBuilder.newBuilder().softValues().build();
    private final Map<String, String> backer;

    private static int globalInvalidations = 0;
    private int localInvalidations = globalInvalidations;

    static {
        LOwOcalizationAPI.addInvalidationHook(() -> ++globalInvalidations);
    }

    public LOwOLanguageMap(Map<String, String> m) {
        backer = m;
    }

    @Override
    public int size() {
        return backer.size();
    }

    @Override
    public boolean isEmpty() {
        return backer.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return backer.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backer.containsValue(value);
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof String)) return null;
        String raw = backer.get(key);
        if (raw == null) return null;

        if (localInvalidations != globalInvalidations) {
            cache.invalidateAll();
            localInvalidations = globalInvalidations;
        }
        try {
            return cache.get(key, () -> LOwOcalizationHooks.onLocalization(raw));
        } catch (ExecutionException e) {
            try {
                throw e.getCause();
            } catch (RuntimeException | Error er) {
                throw er;
            } catch (Throwable throwable) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String put(String key, String value) {
        return backer.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return backer.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        backer.putAll(m);
    }

    @Override
    public void clear() {
        backer.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return backer.keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return backer.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return backer.entrySet();
    }

}
package eutros.lowocalization.core.common;

import eutros.lowocalization.api.LOwOcalizationAPI;
import eutros.lowocalization.core.LOwOcalizationHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class LOwOLanguageMap implements Map<String, String> {

    private final ConcurrentMap<Object, String> cache = new ConcurrentHashMap<>();
    private final Map<String, String> backer;

    private static int globalInvalidations = 0;
    private int localInvalidations = globalInvalidations;

    static {
        LOwOcalizationAPI.addInvalidationHook(() -> ++globalInvalidations);
    }

    private final Function<Object, String> lowocalisedGet;

    public LOwOLanguageMap(Map<String, String> m) {
        backer = m;
        //noinspection SuspiciousMethodCalls
        lowocalisedGet = key -> LOwOcalizationHooks.onLocalization(backer.get(key));
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
        if (localInvalidations != globalInvalidations) {
            cache.clear();
            localInvalidations = globalInvalidations;
        }
        return cache.computeIfAbsent(key, lowocalisedGet);
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

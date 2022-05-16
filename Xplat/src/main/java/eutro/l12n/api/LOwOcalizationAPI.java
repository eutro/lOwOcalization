package eutro.l12n.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LOwOcalizationAPI {

    private static final List<Consumer<LOwOcalizationEvent>> lOwOcalizers = new ArrayList<>();
    private static final List<Runnable> invalidationHooks = new ArrayList<>();

    private LOwOcalizationAPI() {
    }

    public static List<ILOwOConfigurableTransformation> TRANSFORMATIONS = new ArrayList<>();

    public static void registerTransformations(ILOwOConfigurableTransformation... transformations) {
        registerTransformations(Arrays.asList(transformations));
    }

    public static synchronized void registerTransformations(Iterable<ILOwOConfigurableTransformation> transformations) {
        transformations.forEach(TRANSFORMATIONS::add);
    }

    public static Stream<ILOwOTransformation> lookupTransformations(String line) {
        return TRANSFORMATIONS.stream().flatMap(it -> it.getTransformation(line));
    }

    public static void addLOwOcalizer(Consumer<LOwOcalizationEvent> lOwOcalizer) {
        lOwOcalizers.add(lOwOcalizer);
        invalidateCaches();
    }

    public static boolean post(LOwOcalizationEvent event) {
        for(Consumer<LOwOcalizationEvent> consumer : lOwOcalizers) {
            consumer.accept(event);
        }
        return event.isCancelled();
    }

    public static void addInvalidationHook(Runnable invalidationHook) {
        invalidationHooks.add(invalidationHook);
    }

    public static void invalidateCaches() {
        for (Runnable hook : invalidationHooks) {
            hook.run();
        }
    }

}

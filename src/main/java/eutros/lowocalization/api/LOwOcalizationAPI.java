package eutros.lowocalization.api;

import java.util.ArrayList;
import java.util.function.Consumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LOwOcalizationAPI {

    private static final List<Consumer<LOwOcalizationEvent>> lOwOcalizers = new ArrayList<>();
    private static final List<Runnable> invalidationHooks = new ArrayList<>();

    private LOwOcalizationAPI() {
    }

    public static List<ILOwOConfigurableTransformation> TRANSFORMATIONS = new LinkedList<>();

    public static void registerTransformations(ILOwOConfigurableTransformation... transformations) {
        registerTransformations(Arrays.asList(transformations));
    }

    public static void registerTransformations(Collection<ILOwOConfigurableTransformation> transformations) {
        TRANSFORMATIONS.addAll(transformations);
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

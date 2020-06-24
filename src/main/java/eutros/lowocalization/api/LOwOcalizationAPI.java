package eutros.lowocalization.api;

import java.util.ArrayList;
import java.util.function.Consumer;

public class LOwOcalizationAPI {

    private static ArrayList<Consumer<LOwOcalizationEvent>> lOwOcalizers = new ArrayList<>();

    private LOwOcalizationAPI() {
    }

    /**
     * Change this if you actually want to use the event for something useful.
     */
    public static boolean REGISTER_DEFAULTS = true;

    public static void addLOwOcalizer(Consumer<LOwOcalizationEvent> lOwOcalizer) {
        lOwOcalizers.add(lOwOcalizer);
    }

    public static boolean post(LOwOcalizationEvent event) {
        for(Consumer<LOwOcalizationEvent> consumer : lOwOcalizers) {
            consumer.accept(event);
        }
        return event.isCancelled();
    }

}

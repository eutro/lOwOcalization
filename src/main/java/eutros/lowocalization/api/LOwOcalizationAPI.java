package eutros.lowocalization.api;

import eutros.lowocalization.core.LOwOcalizer;

import java.util.List;
import java.util.function.Function;

public class LOwOcalizationAPI {

    private LOwOcalizationAPI() {
    }

    /**
     * Change this if you actually want to use the event for something useful.
     */
    public static boolean REGISTER_DEFAULTS = true;

    /**
     * Gets a mutable list of the default mappers so you can replace or reorder them at will.
     */
    public static List<Function<String, String>> getDefaults() {
        return LOwOcalizer.mappers;
    }

}

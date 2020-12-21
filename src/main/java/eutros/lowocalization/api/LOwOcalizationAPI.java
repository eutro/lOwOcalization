package eutros.lowocalization.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LOwOcalizationAPI {

    private LOwOcalizationAPI() {
    }

    public static List<ILOwOConfigurableTransformation> TRANSFORMATIONS = new LinkedList<>();

    public static void registerTransformations(ILOwOConfigurableTransformation... transformations) {
        registerTransformations(Arrays.asList(transformations));
    }

    public static void registerTransformations(Collection<ILOwOConfigurableTransformation> transformations) {
        TRANSFORMATIONS.addAll(transformations);
    }

}

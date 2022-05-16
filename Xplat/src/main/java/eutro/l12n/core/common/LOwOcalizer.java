package eutro.l12n.core.common;

import eutro.l12n.api.ILOwOTransformation;
import eutro.l12n.api.LOwOcalizationAPI;
import eutro.l12n.api.LOwOcalizationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class LOwOcalizer {

    private static final Logger LOGGER = LogManager.getLogger();
    private List<ILOwOTransformation> transformations = new LinkedList<>();

    static {
        LOwOcalizationAPI.registerTransformations(LOwODefaultTransformations.getAll());
    }

    public static final LOwOcalizer INSTANCE = new LOwOcalizer();

    private LOwOcalizer() {
    }

    public void configChange(List<String> configurations) {
        LinkedList<ILOwOTransformation> transformations = new LinkedList<>();
        for (String configuration : configurations) {
            AtomicBoolean found = new AtomicBoolean(false);
            LOwOcalizationAPI.lookupTransformations(configuration).forEachOrdered(it -> {
                found.set(true);
                transformations.add(it);
            });
            if (!found.get()) {
                LOGGER.warn("No transformations matched for config \"{}\"", configuration);
            }
        }
        // avoid CMEs
        this.transformations = transformations;
        LOwOcalizationAPI.invalidateCaches();
    }

    public void onLOwOcalizationEvent(LOwOcalizationEvent evt) {
        String s = evt.getCurrent();
        for (ILOwOTransformation transformation : transformations) {
            s = transformation.transform(s);
        }
        evt.setCurrent(s);
    }

}
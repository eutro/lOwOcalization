package eutro.l12n.core.common;

import eutro.l12n.api.ILOwOTransformation;
import eutro.l12n.api.LOwOcalizationAPI;
import eutro.l12n.api.LOwOcalizationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class LOwOcalizer {

    private static final Logger LOGGER = LogManager.getLogger();
    private List<TransformWithSource> transformations = new LinkedList<>();

    static {
        LOwOcalizationAPI.registerTransformations(LOwODefaultTransformations.getAll());
    }

    public static final LOwOcalizer INSTANCE = new LOwOcalizer();

    private LOwOcalizer() {
    }

    private record TransformWithSource(ILOwOTransformation transformation, String source) {
    }

    public void configChange(List<String> configurations) {
        LinkedList<TransformWithSource> transformations = new LinkedList<>();
        for (String configuration : configurations) {
            AtomicBoolean found = new AtomicBoolean(false);
            LOwOcalizationAPI.lookupTransformations(configuration).forEachOrdered(it -> {
                found.set(true);
                transformations.add(new TransformWithSource(it, configuration));
            });
            if (!found.get()) {
                LOGGER.warn("No transformations matched for config \"{}\"", configuration);
            }
        }
        // avoid CMEs
        this.transformations = transformations;
        LOwOcalizationAPI.invalidateCaches();
    }

    public final void onLOwOcalizationEvent(LOwOcalizationEvent evt) {
        String s = evt.getCurrent();
        for (Iterator<TransformWithSource> iter = transformations.iterator(); iter.hasNext(); ) {
            TransformWithSource transformation = iter.next();
            try {
                s = transformation.transformation().transform(s);
            } catch (RuntimeException e) {
                LOGGER.warn(() -> "Transformation: \"" + transformation.source() + "\" caused an exception, removing", e);
                iter.remove();
            }
        }
        evt.setCurrent(s);
    }

}
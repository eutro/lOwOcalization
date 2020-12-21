package eutros.lowocalization.core.common;

import eutros.lowocalization.api.ILOwOConfigurableTransformation;
import eutros.lowocalization.api.ILOwOTransformation;
import eutros.lowocalization.api.LOwOcalizationAPI;
import eutros.lowocalization.api.LOwOcalizationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        for(String configuration : configurations) {
            Optional<ILOwOTransformation> transformation = Optional.of(s -> s);
            for(ILOwOConfigurableTransformation configurable : LOwOcalizationAPI.TRANSFORMATIONS) {
                transformation = configurable.getTransformation(configuration);
                if (transformation.isPresent()) {
                    transformations.add(transformation.get());
                    break;
                }
            }
            if (!transformation.isPresent()) {
                LOGGER.warn("No transformation matched for config \"{}\"", configuration);
            }
        }
        // avoid CMEs
        this.transformations = transformations;
    }

    public void onLOwOcalizationEvent(LOwOcalizationEvent evt) {
        String s = evt.getCurrent();
        for(ILOwOTransformation transformation : transformations) {
            s = transformation.transform(s);
        }
        evt.setCurrent(s);
    }

}

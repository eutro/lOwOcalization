package eutro.l12n.core.common;

import eutro.l12n.api.LOwOcalizationAPI;
import eutro.l12n.api.LOwOcalizationEvent;
import org.jetbrains.annotations.Contract;

public class LOwOcalizationHooks {

    @Contract("null -> null; !null -> !null")
    public static String onLocalization(String original) {
        if(original == null)
            return null;

        LOwOcalizationEvent evt = new LOwOcalizationEvent(original);
        if(LOwOcalizationAPI.post(evt)) {
            return original;
        }

        return evt.getCurrent();
    }

}

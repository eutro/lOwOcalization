package eutro.l12n.core.common;

import eutro.l12n.api.LOwOcalizationAPI;
import eutro.l12n.api.LOwOcalizationEvent;

public class LOwOcalizationHooks {

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

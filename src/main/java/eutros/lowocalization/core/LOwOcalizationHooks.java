package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import eutros.lowocalization.api.LOwOcalizationEvent;

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

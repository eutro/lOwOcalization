package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationEvent;
import net.minecraftforge.common.MinecraftForge;

public class LOwOcalizationHooks {

    public static String onLocalization(String original) {
        if(original == null)
            return null;

        LOwOcalizationEvent evt = new LOwOcalizationEvent(original);
        if(MinecraftForge.EVENT_BUS.post(evt))
            return original;

        return evt.getCurrent();
    }

}

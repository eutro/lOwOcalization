package eutros.lowocalization.core.handler;

import eutros.lowocalization.api.LOwOcalizationEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class LOwOcalizationHook {

    @Nonnull
    public static String formatMessage(String localized) {
        LOwOcalizationEvent event = new LOwOcalizationEvent(localized);
        boolean post = MinecraftForge.EVENT_BUS.post(event);
        return post ? localized : event.getCurrent();
    }

}

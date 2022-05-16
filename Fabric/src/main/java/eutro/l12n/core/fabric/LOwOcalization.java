package eutro.l12n.core.fabric;

import eutro.l12n.api.LOwOcalizationAPI;
import eutro.l12n.core.common.LOwOcalizer;
import net.fabricmc.api.ClientModInitializer;

public class LOwOcalization implements ClientModInitializer {

    public static final String MOD_NAME = "lOwOcalizati\u03C9n";

    @Override
    public void onInitializeClient() {
        LOwOcalizationAPI.addLOwOcalizer(LOwOcalizer.INSTANCE::onLOwOcalizationEvent);
        LOwOConfig.init();
    }

}

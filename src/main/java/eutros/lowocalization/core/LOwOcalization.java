package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import eutros.lowocalization.core.common.LOwOcalizer;
import net.fabricmc.api.ClientModInitializer;

public class LOwOcalization implements ClientModInitializer {

    public static final String MOD_NAME = "lOwOcalizati\u03C9n";

    @Override
    public void onInitializeClient() {
        LOwOcalizationAPI.addLOwOcalizer(LOwOcalizer.INSTANCE::onLOwOcalizationEvent);
        LOwOConfig.init();
    }

}

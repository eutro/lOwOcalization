package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import net.fabricmc.api.ClientModInitializer;

public class LOwOcalization implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LOwOcalizationAPI.addLOwOcalizer(LOwOcalizer.INSTANCE::onLOwOcalizationEvent);
    }

}

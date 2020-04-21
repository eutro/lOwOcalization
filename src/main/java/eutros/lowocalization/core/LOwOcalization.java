package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LOwOcalization.MOD_ID)
public class LOwOcalization {

    public static final String MOD_ID = "lowocalization";

    public LOwOcalization() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent evt) {
        if(LOwOcalizationAPI.REGISTER_DEFAULTS) {
            MinecraftForge.EVENT_BUS.addListener(LOwOcalizer::onLOwOcalizationEvent);
        }
    }

}

package eutros.lowocalization.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LOwOcalization.MOD_ID)
public class LOwOcalization {

    public static final String MOD_ID = "lowocalization";
    public static final String NAME = "lOwOcalisati\u03C9n";

    public LOwOcalization() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::clientSetup);
        bus.addListener(LOwOcalizer.INSTANCE::configChange);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LOwOConfig.CLIENT_SPEC);
    }

    private void clientSetup(FMLClientSetupEvent evt) {
        MinecraftForge.EVENT_BUS.register(LOwOcalizer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new LOwOMapInjector());
    }

}

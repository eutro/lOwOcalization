package eutros.lowocalization.core;

import eutros.lowocalization.core.common.LOwOcalizer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LOwOcalization.MOD_ID)
public class LOwOcalization {

    public static final String MOD_ID = "lowocalization";
    public static final String NAME = "lOwOcalisati\u03C9n";

    public LOwOcalization() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LOwOConfig::onChange);
        MinecraftForge.EVENT_BUS.addListener(LOwOcalizer.INSTANCE::onLOwOcalizationEvent);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LOwOConfig.CLIENT_SPEC);
    }

}

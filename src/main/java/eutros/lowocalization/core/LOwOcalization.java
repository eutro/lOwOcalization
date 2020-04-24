package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
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

        MinecraftForge.EVENT_BUS.register(LOwOcalizer.INSTANCE); // just to generate the comment
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LOwOConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.unregister(LOwOcalizer.INSTANCE);
    }

    private void clientSetup(FMLClientSetupEvent evt) {
        LanguageManager.CURRENT_LOCALE = new LOwOcale(LanguageManager.CURRENT_LOCALE);
        LanguageMap.getInstance().languageList = new LOwOLanguageMap(LanguageMap.getInstance().languageList);
        TranslationTextComponent.FALLBACK_LANGUAGE.languageList = new LOwOLanguageMap(TranslationTextComponent.FALLBACK_LANGUAGE.languageList);
        new LanguageManager("OwO");
        Minecraft.getInstance().getLanguageManager().onResourceManagerReload(Minecraft.getInstance().getResourceManager());

        if(LOwOcalizationAPI.REGISTER_DEFAULTS) {
            MinecraftForge.EVENT_BUS.register(LOwOcalizer.INSTANCE);
        }
    }

}

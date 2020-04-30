package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LOwOcalization.MOD_ID,
     name = LOwOcalization.NAME,
     version = LOwOcalization.VERSION,
     clientSideOnly = true,
     guiFactory = "eutros.lowocalization.core.LOwOGuiFactory")
public class LOwOcalization {

    public static final String MOD_ID = "lowocalization";
    public static final String NAME = "lOwOcalisati\u03C9n";
    public static final String VERSION = "2.0.0";

    public LOwOcalization() {
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        LOwOConfig.registerConfig(evt);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        LanguageManager.CURRENT_LOCALE = new LOwOcale(LanguageManager.CURRENT_LOCALE);
        LanguageMap.getInstance().languageList = new LOwOLanguageMap(LanguageMap.getInstance().languageList);
        new LanguageManager(null, "OwO");
        Minecraft.getMinecraft().getLanguageManager().onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());

        if(LOwOcalizationAPI.REGISTER_DEFAULTS) {
            MinecraftForge.EVENT_BUS.register(LOwOcalizer.INSTANCE);
        }
    }

}
